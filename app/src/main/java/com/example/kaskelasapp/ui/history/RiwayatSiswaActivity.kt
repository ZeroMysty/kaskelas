package com.example.kaskelasapp.ui.history

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.utils.BackgroundHelper

class RiwayatSiswaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_siswa)
        BackgroundHelper.applyAnimatedBackground(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBackRiwayatSiswa)
        btnBack.setOnClickListener {
            finish()
        }

        // TODO: Load history logic
    }
}
