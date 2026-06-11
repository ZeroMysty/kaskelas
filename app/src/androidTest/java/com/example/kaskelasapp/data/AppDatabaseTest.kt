package com.example.kaskelasapp.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var anggotaDao: AnggotaDao
    private lateinit var transaksiDao: TransaksiDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries().build()
        anggotaDao = db.anggotaDao()
        transaksiDao = db.transaksiDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadAnggota() = runBlocking {
        val anggota = AnggotaEntity(id = "1", nama = "Budi", nis = "101", tanggalBergabung = "01 Jan 2024")
        anggotaDao.insertAnggota(anggota)

        val list = anggotaDao.getAllAnggota()
        assertEquals(1, list.size)
        assertEquals("Budi", list[0].nama)
        assertEquals("01 Jan 2024", list[0].tanggalBergabung)
    }

    @Test
    fun writeAndReadTransaksi() = runBlocking {
        val anggotaId = "1"
        val anggota = AnggotaEntity(id = anggotaId, nama = "Budi", nis = "101", tanggalBergabung = "01 Jan 2024")
        anggotaDao.insertAnggota(anggota)

        val transaksi = TransaksiEntity(
            nama = "Bayar Kas",
            jumlah = "2000",
            tanggal = "01 Jan 2024",
            tipe = "MASUK",
            keterangan = "Lunas",
            anggota_id = anggotaId,
            buktiFoto = null
        )
        val id = transaksiDao.insertTransaksi(transaksi)

        val itemFromDb = transaksiDao.getTransaksiById(id.toInt())
        assertEquals("Bayar Kas", itemFromDb?.nama)
        assertEquals("MASUK", itemFromDb?.tipe)
    }

    @Test
    fun calculateTotalSaldo() = runBlocking {
        val masuk = TransaksiEntity(nama = "Masuk", jumlah = "10000", tanggal = "01 Jan", tipe = "MASUK", keterangan = "", anggota_id = null, buktiFoto = null)
        val keluar = TransaksiEntity(nama = "Keluar", jumlah = "3000", tanggal = "01 Jan", tipe = "KELUAR", keterangan = "", anggota_id = null, buktiFoto = null)

        transaksiDao.insertTransaksi(masuk)
        transaksiDao.insertTransaksi(keluar)

        val totalMasuk = transaksiDao.getTotalMasuk() ?: 0L
        val totalKeluar = transaksiDao.getTotalKeluar() ?: 0L

        assertEquals(10000L, totalMasuk)
        assertEquals(3000L, totalKeluar)
    }
}
