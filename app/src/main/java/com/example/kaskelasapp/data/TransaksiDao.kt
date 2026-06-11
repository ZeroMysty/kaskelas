package com.example.kaskelasapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransaksiDao {
    @Query("SELECT * FROM transaksi ORDER BY id DESC")
    suspend fun getAllTransaksi(): List<TransaksiEntity>

    @Query("SELECT * FROM transaksi WHERE anggota_id = :anggotaId ORDER BY id DESC")
    suspend fun getTransaksiByAnggota(anggotaId: String): List<TransaksiEntity>

    @Query("SELECT * FROM transaksi WHERE id = :id LIMIT 1")
    suspend fun getTransaksiById(id: Int): TransaksiEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaksi(transaksi: TransaksiEntity): Long

    @androidx.room.Update
    suspend fun updateTransaksi(transaksi: TransaksiEntity)

    @Query("SELECT SUM(CAST(jumlah AS INTEGER)) FROM transaksi WHERE jenis='MASUK'")
    suspend fun getTotalMasuk(): Long?

    @Query("SELECT SUM(CAST(jumlah AS INTEGER)) FROM transaksi WHERE jenis='KELUAR'")
    suspend fun getTotalKeluar(): Long?

    @Query("DELETE FROM transaksi WHERE id = :id")
    suspend fun deleteTransaksiById(id: Int)

    @Query("DELETE FROM transaksi")
    suspend fun deleteAllTransaksi()
}
