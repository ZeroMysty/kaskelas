package com.example.kaskelasapp

import android.app.Application
import com.example.kaskelasapp.data.AppDatabase
import com.example.kaskelasapp.repository.KasRepository

/**
 * Application class yang menyimpan singleton AppDatabase dan KasRepository.
 * Dengan ini, semua Activity bisa mengakses repository yang sama tanpa
 * membuat instance baru di setiap onCreate().
 */
class KasKelasApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val repository: KasRepository by lazy {
        KasRepository(database.anggotaDao(), database.transaksiDao())
    }
}
