package com.example.kaskelasapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [AnggotaEntity::class, TransaksiEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun anggotaDao(): AnggotaDao
    abstract fun transaksiDao(): TransaksiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration dari versi 1 → 2: tambah kolom tanggal_bergabung ke tabel anggota
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE anggota ADD COLUMN tanggal_bergabung TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "uangkas_room.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
