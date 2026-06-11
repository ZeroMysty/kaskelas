package com.example.kaskelasapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AnggotaDao {
    @Query("SELECT * FROM anggota")
    fun getAllAnggota(): List<AnggotaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAnggota(anggota: AnggotaEntity)

    @Update
    fun updateAnggota(anggota: AnggotaEntity)

    @Query("DELETE FROM anggota WHERE id = :id")
    fun deleteAnggotaById(id: String)

    @Query("DELETE FROM anggota")
    fun deleteAllAnggota()
}
