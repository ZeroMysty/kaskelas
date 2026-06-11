package com.example.kaskelasapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AnggotaDao {
    @Query("SELECT * FROM anggota ORDER BY nama ASC")
    suspend fun getAllAnggota(): List<AnggotaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnggota(anggota: AnggotaEntity)

    @Update
    suspend fun updateAnggota(anggota: AnggotaEntity)

    @Query("DELETE FROM anggota WHERE id = :id")
    suspend fun deleteAnggotaById(id: String)

    @Query("DELETE FROM anggota")
    suspend fun deleteAllAnggota()
}
