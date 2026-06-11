package com.example.kaskelasapp.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.kaskelasapp.KasKelasApp
import com.example.kaskelasapp.data.AnggotaEntity
import com.example.kaskelasapp.data.TransaksiEntity
import com.example.kaskelasapp.repository.KasRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KasViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: KasViewModel
    private lateinit var repository: KasRepository
    private lateinit var app: KasKelasApp

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        app = mockk()
        every { app.repository } returns repository

        viewModel = KasViewModel(app)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAllAnggota mengupdate anggotaList StateFlow`() = runTest {
        val mockList = listOf(AnggotaEntity("1", "Test", "123", "01 Jan 2024"))
        coEvery { repository.getAllAnggota() } returns mockList

        viewModel.loadAllAnggota()
        testDispatcher.scheduler.advanceUntilIdle() // Tunggu coroutine selesai

        assertEquals(1, viewModel.anggotaList.value.size)
        assertEquals("Test", viewModel.anggotaList.value[0].nama)
    }

    @Test
    fun `loadTotalSaldo mengupdate totalSaldo StateFlow`() = runTest {
        coEvery { repository.hitungTotalSaldo() } returns 150000L

        viewModel.loadTotalSaldo()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(150000L, viewModel.totalSaldo.value)
    }

    @Test
    fun `insertTransaksi trigger reload data`() = runTest {
        val transaksi = TransaksiEntity(1, "Beli", "500", "01 Jan", "KELUAR", "", null, null)
        coEvery { repository.insertTransaksi(any()) } returns 1L
        coEvery { repository.getAllTransaksi() } returns listOf(transaksi)
        coEvery { repository.hitungTotalSaldo() } returns -500L

        var callbackCalled = false

        viewModel.insertTransaksi(transaksi) {
            callbackCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.insertTransaksi(transaksi) }
        coVerify(exactly = 1) { repository.getAllTransaksi() }
        coVerify(exactly = 1) { repository.hitungTotalSaldo() }
        assertEquals(true, callbackCalled)
    }
}
