package com.example.kaskelasapp.repository

import com.example.kaskelasapp.data.AnggotaDao
import com.example.kaskelasapp.data.AnggotaEntity
import com.example.kaskelasapp.data.TransaksiDao
import com.example.kaskelasapp.data.TransaksiEntity

// Repository tidak perlu lagi withContext(Dispatchers.IO) karena DAO sudah suspend
// dan Room secara otomatis menjalankan suspend DAO di IO dispatcher-nya sendiri.
class KasRepository(
    private val anggotaDao: AnggotaDao,
    private val transaksiDao: TransaksiDao
) {
    suspend fun getAllAnggota(): List<AnggotaEntity> = anggotaDao.getAllAnggota()

    suspend fun insertAnggota(anggota: AnggotaEntity) = anggotaDao.insertAnggota(anggota)

    suspend fun updateAnggota(anggota: AnggotaEntity) = anggotaDao.updateAnggota(anggota)

    suspend fun deleteAnggota(id: String) = anggotaDao.deleteAnggotaById(id)

    suspend fun getAllTransaksi(): List<TransaksiEntity> = transaksiDao.getAllTransaksi()

    suspend fun getTransaksiByAnggota(anggotaId: String): List<TransaksiEntity> =
        transaksiDao.getTransaksiByAnggota(anggotaId)

    suspend fun getTransaksiById(id: Int): TransaksiEntity? = transaksiDao.getTransaksiById(id)

    suspend fun insertTransaksi(transaksi: TransaksiEntity): Long =
        transaksiDao.insertTransaksi(transaksi)

    suspend fun updateTransaksi(transaksi: TransaksiEntity) = transaksiDao.updateTransaksi(transaksi)

    suspend fun deleteTransaksiById(id: Int) = transaksiDao.deleteTransaksiById(id)

    suspend fun hitungTotalSaldo(): Long {
        val totalMasuk = transaksiDao.getTotalMasuk() ?: 0L
        val totalKeluar = transaksiDao.getTotalKeluar() ?: 0L
        return totalMasuk - totalKeluar
    }

    suspend fun resetDatabase() {
        transaksiDao.deleteAllTransaksi()
        anggotaDao.deleteAllAnggota()
    }

    // Batch restore — insert semua data sekaligus, hanya 1x trigger loadAll di akhir
    suspend fun batchRestore(
        anggotaList: List<AnggotaEntity>,
        transaksiList: List<TransaksiEntity>
    ) {
        transaksiDao.deleteAllTransaksi()
        anggotaDao.deleteAllAnggota()
        anggotaList.forEach { anggotaDao.insertAnggota(it) }
        transaksiList.forEach { transaksiDao.insertTransaksi(it) }
    }
}
