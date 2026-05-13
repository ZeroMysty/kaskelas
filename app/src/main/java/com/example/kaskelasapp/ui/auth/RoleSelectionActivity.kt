package com.example.kaskelasapp.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.google.android.material.card.MaterialCardView

class RoleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        val cardBendahara = findViewById<MaterialCardView>(R.id.cardBendahara)
        val cardSiswa = findViewById<MaterialCardView>(R.id.cardSiswa)

        cardBendahara.setOnClickListener {
            val intent = Intent(this, LoginBendaharaActivity::class.java)
            startActivity(intent)
        }

        cardSiswa.setOnClickListener {
            val intent = Intent(this, RegisterSiswaActivity::class.java)
            startActivity(intent)
        }
    }
}
