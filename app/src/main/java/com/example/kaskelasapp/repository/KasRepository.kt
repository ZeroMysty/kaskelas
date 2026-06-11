package com.example.kaskelasapp.repository

import com.example.kaskelasapp.data.AnggotaDao
import com.example.kaskelasapp.data.AnggotaEntity
import com.example.kaskelasapp.data.TransaksiDao
import com.example.kaskelasapp.data.TransaksiEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KasRepository(
    private val anggotaDao: AnggotaDao,
    private val transaksiDao: TransaksiDao
) {
    suspend fun getAllAnggota(): List<AnggotaEntity> = withContext(Dispatchers.IO) {
        anggotaDao.getAllAnggota()
    }

    suspend fun insertAnggota(anggota: AnggotaEntity) = withContext(Dispatchers.IO) {
        anggotaDao.insertAnggota(anggota)
    }

    suspend fun updateAnggota(anggota: AnggotaEntity) = withContext(Dispatchers.IO) {
        anggotaDao.updateAnggota(anggota)
    }

    suspend fun deleteAnggota(id: String) = withContext(Dispatchers.IO) {
        anggotaDao.deleteAnggotaById(id)
    }

    suspend fun getAllTransaksi(): List<TransaksiEntity> = withContext(Dispatchers.IO) {
        transaksiDao.getAllTransaksi()
    }

    suspend fun getTransaksiByAnggota(anggotaId: String): List<TransaksiEntity> = withContext(Dispatchers.IO) {
        transaksiDao.getTransaksiByAnggota(anggotaId)
    }

    suspend fun getTransaksiById(id: Int): TransaksiEntity? = withContext(Dispatchers.IO) {
        transaksiDao.getTransaksiById(id)
    }

    suspend fun insertTransaksi(transaksi: TransaksiEntity): Long = withContext(Dispatchers.IO) {
        transaksiDao.insertTransaksi(transaksi)
    }

    suspend fun hitungTotalSaldo(): Long = withContext(Dispatchers.IO) {
        val totalMasuk = transaksiDao.getTotalMasuk() ?: 0L
        val totalKeluar = transaksiDao.getTotalKeluar() ?: 0L
        totalMasuk - totalKeluar
    }

    suspend fun resetDatabase() = withContext(Dispatchers.IO) {
        transaksiDao.deleteAllTransaksi()
        anggotaDao.deleteAllAnggota()
    }
}
