package com.example.kaskelasapp.ui.main

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.utils.BackgroundHelper

class BayarKasSiswaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bayar_kas_siswa)
        BackgroundHelper.applyAnimatedBackground(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBackBayarSiswa)
        btnBack.setOnClickListener {
            finish()
        }

        // TODO: Logic for switching Tunai/E-Wallet UI
        // TODO: Logic for uploading image and saving payment
    }
}
