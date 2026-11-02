<#
Lee una copia del DataStore de AgustiSpeaks (agusti_settings.preferences_pb)
y escribe una linea por clave, ordenadas: "clave = valor   [tipo]".

El archivo es un PreferenceMap de androidx.datastore.preferences, en protobuf:
un mapa de clave -> Value, donde Value es uno de: boolean (1), float (2),
integer (3), long (4), string (5), string_set (6), double (7) o bytes (8).

La huella de la contrasena del modo edicion (edit_password) no se escribe.

Uso:
  powershell -NoProfile -ExecutionPolicy Bypass -File leer-datastore.ps1 -Archivo copia.pb
#>
param([Parameter(Mandatory = $true)][string]$Archivo)

$ErrorActionPreference = 'Stop'
# Salida en UTF-8: los nombres de los pictogramas llevan tildes, y quien lee
# esta salida (Out-File en el protocolo) la espera en UTF-8.
[Console]::OutputEncoding = [Text.Encoding]::UTF8
$datos = [IO.File]::ReadAllBytes((Resolve-Path $Archivo))
$utf8 = New-Object Text.UTF8Encoding $false
$inv = [Globalization.CultureInfo]::InvariantCulture

function Leer-Varint([byte[]]$b, [ref]$p) {
    [UInt64]$v = 0
    $corrimiento = 0
    while ($true) {
        $x = $b[$p.Value]
        $p.Value = $p.Value + 1
        $v = $v -bor ([UInt64]($x -band 0x7F) -shl $corrimiento)
        if (($x -band 0x80) -eq 0) { return $v }
        $corrimiento += 7
    }
}

function Saltear([byte[]]$b, [ref]$p, [int]$tipoCable) {
    switch ($tipoCable) {
        0 { [void](Leer-Varint $b $p) }
        1 { $p.Value = $p.Value + 8 }
        2 { $n = [int](Leer-Varint $b $p); $p.Value = $p.Value + $n }
        5 { $p.Value = $p.Value + 4 }
        default { throw "Tipo de cable desconocido: $tipoCable" }
    }
}

function Leer-Valor([byte[]]$b, [int]$inicio, [int]$fin) {
    $p = $inicio
    $resultado = '(vacio)', 'desconocido'
    while ($p -lt $fin) {
        $tag = Leer-Varint $b ([ref]$p)
        $campo = [int]($tag -shr 3)
        $cable = [int]($tag -band 7)
        switch ($campo) {
            1 { $resultado = @(([bool](Leer-Varint $b ([ref]$p))).ToString().ToLower(), 'boolean') }
            2 { $resultado = @([BitConverter]::ToSingle($b, $p).ToString('R', $inv), 'float'); $p += 4 }
            3 { $resultado = @([string](Leer-Varint $b ([ref]$p)), 'integer') }
            4 { $resultado = @([string](Leer-Varint $b ([ref]$p)), 'long') }
            5 {
                $n = [int](Leer-Varint $b ([ref]$p))
                $resultado = @($utf8.GetString($b, $p, $n), 'string')
                $p += $n
            }
            6 {
                $n = [int](Leer-Varint $b ([ref]$p))
                $finSet = $p + $n
                $items = @()
                while ($p -lt $finSet) {
                    $t = Leer-Varint $b ([ref]$p)
                    if (($t -shr 3) -eq 1 -and ($t -band 7) -eq 2) {
                        $m = [int](Leer-Varint $b ([ref]$p))
                        $items += $utf8.GetString($b, $p, $m)
                        $p += $m
                    } else {
                        Saltear $b ([ref]$p) ([int]($t -band 7))
                    }
                }
                $resultado = @(('{' + ($items -join ', ') + '}'), 'string_set')
            }
            7 { $resultado = @([BitConverter]::ToDouble($b, $p).ToString('R', $inv), 'double'); $p += 8 }
            8 {
                $n = [int](Leer-Varint $b ([ref]$p))
                $resultado = @((($b[$p..($p + $n - 1)] | ForEach-Object { $_.ToString('x2') }) -join ''), 'bytes')
                $p += $n
            }
            default { Saltear $b ([ref]$p) $cable }
        }
    }
    return , $resultado
}

$filas = @()
$pos = 0
while ($pos -lt $datos.Length) {
    $tag = Leer-Varint $datos ([ref]$pos)
    $campo = [int]($tag -shr 3)
    $cable = [int]($tag -band 7)
    if ($campo -ne 1 -or $cable -ne 2) { Saltear $datos ([ref]$pos) $cable; continue }

    $largo = [int](Leer-Varint $datos ([ref]$pos))
    $finEntrada = $pos + $largo
    $clave = $null
    $valor = '(sin valor)', 'desconocido'
    while ($pos -lt $finEntrada) {
        $t = Leer-Varint $datos ([ref]$pos)
        $c = [int]($t -shr 3)
        $w = [int]($t -band 7)
        if ($c -eq 1 -and $w -eq 2) {
            $n = [int](Leer-Varint $datos ([ref]$pos))
            $clave = $utf8.GetString($datos, $pos, $n)
            $pos += $n
        } elseif ($c -eq 2 -and $w -eq 2) {
            $n = [int](Leer-Varint $datos ([ref]$pos))
            $valor = Leer-Valor $datos $pos ($pos + $n)
            $pos += $n
        } else {
            Saltear $datos ([ref]$pos) $w
        }
    }
    if ($clave -eq 'edit_password') { $valor = '(huella de la contrasena: no se muestra)', $valor[1] }
    $filas += [pscustomobject]@{ Clave = $clave; Valor = $valor[0]; Tipo = $valor[1] }
}

"$($filas.Count) claves en $([IO.Path]::GetFileName($Archivo)) ($($datos.Length) bytes)"
$filas | Sort-Object Clave | ForEach-Object { "{0} = {1}   [{2}]" -f $_.Clave, $_.Valor, $_.Tipo }
