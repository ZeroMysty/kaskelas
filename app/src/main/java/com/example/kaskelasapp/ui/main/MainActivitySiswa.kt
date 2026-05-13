package com.example.kaskelasapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.ui.auth.RoleSelectionActivity
import com.example.kaskelasapp.utils.BackgroundHelper
import com.google.android.material.card.MaterialCardView

class MainActivitySiswa : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_siswa)
        BackgroundHelper.applyAnimatedBackground(this)

        val btnProfileSiswa = findViewById<ImageView>(R.id.btnProfileSiswa)
        val btnBayarKas = findViewById<MaterialCardView>(R.id.btnBayarKas)
        val btnRiwayatSiswa = findViewById<MaterialCardView>(R.id.btnRiwayatSiswa)

        // TODO: Create Profile Siswa Activity and Bayar Kas Activity
        
        btnProfileSiswa.setOnClickListener {
            val intent = Intent(this, ProfileSiswaActivity::class.java)
            startActivity(intent)
        }

        btnBayarKas.setOnClickListener {
            val intent = Intent(this, BayarKasSiswaActivity::class.java)
            startActivity(intent)
        }

        btnRiwayatSiswa.setOnClickListener {
            val intent = Intent(this, com.example.kaskelasapp.ui.history.RiwayatSiswaActivity::class.java)
            startActivity(intent)
        }
    }
}
