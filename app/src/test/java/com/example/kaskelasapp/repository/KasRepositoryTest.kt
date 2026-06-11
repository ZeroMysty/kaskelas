package com.example.kaskelasapp.repository

import com.example.kaskelasapp.data.AnggotaDao
import com.example.kaskelasapp.data.AnggotaEntity
import com.example.kaskelasapp.data.TransaksiDao
import com.example.kaskelasapp.data.TransaksiEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class KasRepositoryTest {

    private lateinit var repository: KasRepository
    private lateinit var anggotaDao: AnggotaDao
    private lateinit var transaksiDao: TransaksiDao

    @Before
    fun setup() {
        anggotaDao = mockk()
        transaksiDao = mockk()
        repository = KasRepository(anggotaDao, transaksiDao)
    }

    @Test
    fun `getAllAnggota mengembalikan list dari dao`() = runBlocking {
        val mockList = listOf(
            AnggotaEntity("1", "Ali", "101", "01 Jan 2024"),
            AnggotaEntity("2", "Budi", "102", "02 Jan 2024")
        )
        coEvery { anggotaDao.getAllAnggota() } returns mockList

        val result = repository.getAllAnggota()

        assertEquals(2, result.size)
        assertEquals("Ali", result[0].nama)
        coVerify(exactly = 1) { anggotaDao.getAllAnggota() }
    }

    @Test
    fun `hitungTotalSaldo mengkalkulasi selisih masuk dan keluar dengan benar`() = runBlocking {
        coEvery { transaksiDao.getTotalMasuk() } returns 50000L
        coEvery { transaksiDao.getTotalKeluar() } returns 20000L

        val saldo = repository.hitungTotalSaldo()

        assertEquals(30000L, saldo)
    }

    @Test
    fun `hitungTotalSaldo menangani null dengan fallback 0`() = runBlocking {
        coEvery { transaksiDao.getTotalMasuk() } returns null
        coEvery { transaksiDao.getTotalKeluar() } returns null

        val saldo = repository.hitungTotalSaldo()

        assertEquals(0L, saldo)
    }

    @Test
    fun `batchRestore menghapus semua lalu insert`() = runBlocking {
        coEvery { transaksiDao.deleteAllTransaksi() } returns Unit
        coEvery { anggotaDao.deleteAllAnggota() } returns Unit
        coEvery { anggotaDao.insertAnggota(any()) } returns Unit
        coEvery { transaksiDao.insertTransaksi(any()) } returns 0L

        val anggotaList = listOf(AnggotaEntity("1", "A", "1", "01 Jan 2024"))
        val transaksiList = listOf(TransaksiEntity(1, "T", "100", "01 Jan", "MASUK", "", "1", null))

        repository.batchRestore(anggotaList, transaksiList)

        coVerify(exactly = 1) { transaksiDao.deleteAllTransaksi() }
        coVerify(exactly = 1) { anggotaDao.deleteAllAnggota() }
        coVerify(exactly = 1) { anggotaDao.insertAnggota(any()) }
        coVerify(exactly = 1) { transaksiDao.insertTransaksi(any()) }
    }
}
