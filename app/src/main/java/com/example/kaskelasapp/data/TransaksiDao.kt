package com.example.kaskelasapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransaksiDao {
    @Query("SELECT * FROM transaksi ORDER BY id DESC")
    fun getAllTransaksi(): List<TransaksiEntity>

    @Query("SELECT * FROM transaksi WHERE anggota_id = :anggotaId ORDER BY id DESC")
    fun getTransaksiByAnggota(anggotaId: String): List<TransaksiEntity>

    @Query("SELECT * FROM transaksi WHERE id = :id LIMIT 1")
    fun getTransaksiById(id: Int): TransaksiEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaksi(transaksi: TransaksiEntity): Long

    @Query("SELECT SUM(CAST(jumlah AS INTEGER)) FROM transaksi WHERE jenis='MASUK'")
    fun getTotalMasuk(): Long?

    @Query("SELECT SUM(CAST(jumlah AS INTEGER)) FROM transaksi WHERE jenis='KELUAR'")
    fun getTotalKeluar(): Long?

    @Query("DELETE FROM transaksi")
    fun deleteAllTransaksi()
}
