package com.tunombre.agustispeaks.avatar

import java.io.OutputStream

/**
 * Codificador GIF89a animado, mínimo y sin dependencias (LZW propio).
 * Pensado para el avatar: pocas frames, paleta chica y plana.
 */
object GifEncoder {

    /**
     * @param frames cada una es un IntArray ARGB row-major de tamaño width*height.
     * @param delayCs retardo entre frames en centésimas de segundo.
     */
    fun encode(
        out: OutputStream,
        width: Int,
        height: Int,
        frames: List<IntArray>,
        delayCs: Int
    ) {
        val (palette, indexOf) = buildPalette(frames)

        out.write("GIF89a".toByteArray(Charsets.US_ASCII))
        out.le16(width); out.le16(height)
        out.write(0xF7)  // tabla global de color, 256 entradas
        out.write(0x00)  // color de fondo
        out.write(0x00)  // aspect ratio

        for (c in palette) {
            out.write((c ushr 16) and 0xFF)
            out.write((c ushr 8) and 0xFF)
            out.write(c and 0xFF)
        }

        // Loop infinito (NETSCAPE2.0)
        out.write(0x21); out.write(0xFF); out.write(0x0B)
        out.write("NETSCAPE2.0".toByteArray(Charsets.US_ASCII))
        out.write(0x03); out.write(0x01); out.write(0x00); out.write(0x00); out.write(0x00)

        val delay = delayCs.coerceAtLeast(2)
        for (frame in frames) {
            out.write(0x21); out.write(0xF9); out.write(0x04)
            out.write(0x00)              // sin transparencia ni disposal
            out.le16(delay)
            out.write(0x00); out.write(0x00)

            out.write(0x2C)
            out.le16(0); out.le16(0)
            out.le16(width); out.le16(height)
            out.write(0x00)             // sin tabla local, no entrelazado

            val indices = ByteArray(frame.size) { indexOf(frame[it]).toByte() }
            lzw(out, indices, 8)
        }

        out.write(0x3B) // fin
    }

    private fun OutputStream.le16(v: Int) {
        write(v and 0xFF)
        write((v ushr 8) and 0xFF)
    }

    private fun buildPalette(frames: List<IntArray>): Pair<IntArray, (Int) -> Int> {
        val seen = LinkedHashSet<Int>()
        var overflow = false
        loop@ for (f in frames) {
            for (p in f) {
                seen.add(p and 0xFFFFFF)
                if (seen.size > 256) { overflow = true; break@loop }
            }
        }
        return if (!overflow) {
            val list = seen.toList()
            val idx = HashMap<Int, Int>(list.size * 2)
            list.forEachIndexed { i, c -> idx[c] = i }
            val pal = IntArray(256) { if (it < list.size) list[it] else 0 }
            pal to { c: Int -> idx[c and 0xFFFFFF] ?: 0 }
        } else {
            val pal = IntArray(256) { i ->
                val r = ((i ushr 5) and 7) * 255 / 7
                val g = ((i ushr 2) and 7) * 255 / 7
                val b = (i and 3) * 255 / 3
                (r shl 16) or (g shl 8) or b
            }
            pal to { c: Int ->
                val r = (c ushr 16) and 0xFF
                val g = (c ushr 8) and 0xFF
                val b = c and 0xFF
                ((r * 7 / 255) shl 5) or ((g * 7 / 255) shl 2) or (b * 3 / 255)
            }
        }
    }

    private fun lzw(out: OutputStream, pixels: ByteArray, minCodeSize: Int) {
        val clearCode = 1 shl minCodeSize
        val eoiCode = clearCode + 1

        var codeSize = minCodeSize + 1
        var nextCode = eoiCode + 1
        val table = HashMap<Int, Int>()

        out.write(minCodeSize)

        val block = ByteArray(255)
        var blockLen = 0
        var bitBuf = 0
        var bitCnt = 0

        fun flushBlock() {
            if (blockLen > 0) {
                out.write(blockLen)
                out.write(block, 0, blockLen)
                blockLen = 0
            }
        }

        fun emit(code: Int) {
            bitBuf = bitBuf or (code shl bitCnt)
            bitCnt += codeSize
            while (bitCnt >= 8) {
                block[blockLen++] = (bitBuf and 0xFF).toByte()
                if (blockLen == 255) flushBlock()
                bitBuf = bitBuf ushr 8
                bitCnt -= 8
            }
        }

        emit(clearCode)

        if (pixels.isNotEmpty()) {
            var prefix = pixels[0].toInt() and 0xFF
            for (i in 1 until pixels.size) {
                val k = pixels[i].toInt() and 0xFF
                val key = (prefix shl 8) or k
                val existing = table[key]
                if (existing != null) {
                    prefix = existing
                } else {
                    emit(prefix)
                    table[key] = nextCode
                    nextCode++
                    if (nextCode == (1 shl codeSize) && codeSize < 12) {
                        codeSize++
                    }
                    if (nextCode == 4096) {
                        emit(clearCode)
                        table.clear()
                        nextCode = eoiCode + 1
                        codeSize = minCodeSize + 1
                    }
                    prefix = k
                }
            }
            emit(prefix)
        }

        emit(eoiCode)

        if (bitCnt > 0) {
            block[blockLen++] = (bitBuf and 0xFF).toByte()
            if (blockLen == 255) flushBlock()
        }
        flushBlock()
        out.write(0x00) // terminador de datos de imagen
    }
}
