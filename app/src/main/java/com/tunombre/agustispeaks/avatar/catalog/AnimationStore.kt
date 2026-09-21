package com.tunombre.agustispeaks.avatar.catalog

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Un registro de animación guardado en el dispositivo.
 *
 * El registro se guarda **como su propio JSON**, no desarmado en tablas de
 * fotogramas y disparadores. Una animación es un documento que se edita entera,
 * y la única búsqueda que hace la app es por id: no hay nada que ganar
 * normalizándola, y sí bastante que perder. Desarmada, el formato quedaría
 * definido en dos lugares —el parser y el esquema— y agregar un campo obligaría
 * a migrar la base además de tocar el parser. Así, el parser es la única
 * definición del formato y la base no se entera cuando el formato crece.
 */
@Entity(tableName = "animaciones")
data class AnimationEntity(
    @PrimaryKey val id: String,
    val json: String,
    val actualizado: Long
)

@Dao
interface AnimationDao {

    /** Ordenadas por id para que la fusión sea siempre la misma. */
    @Query("SELECT * FROM animaciones ORDER BY id")
    suspend fun todas(): List<AnimationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(registro: AnimationEntity)

    @Query("DELETE FROM animaciones WHERE id = :id")
    suspend fun borrar(id: String)

    @Query("SELECT COUNT(*) FROM animaciones")
    suspend fun cuantas(): Int
}

@Database(entities = [AnimationEntity::class], version = 1, exportSchema = false)
abstract class AnimationDatabase : RoomDatabase() {

    abstract fun animaciones(): AnimationDao

    companion object {
        @Volatile
        private var instancia: AnimationDatabase? = null

        fun get(context: Context): AnimationDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AnimationDatabase::class.java,
                    "animaciones.db"
                ).build().also { instancia = it }
            }
    }
}
