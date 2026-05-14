package com.example.kaskelasapp.ui.main

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.utils.BackgroundHelper

class DetailKonfirmasiPembayaranActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_konfirmasi_pembayaran)
        BackgroundHelper.applyAnimatedBackground(this)

        findViewById<ImageButton>(R.id.btnBackDetailKonfirmasi).setOnClickListener { finish() }
    }
}
