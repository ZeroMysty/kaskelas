package com.example.kaskelasapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.ui.main.MainActivitySiswa
import com.google.android.material.button.MaterialButton

class LoginSiswaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_siswa)

        val btnBack = findViewById<ImageView>(R.id.btnBackLogin)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val btnGoogleLogin = findViewById<MaterialButton>(R.id.btnGoogleLogin)
        val tvToRegister = findViewById<TextView>(R.id.tvToRegister)

        btnBack.setOnClickListener {
            finish()
        }

        // Mock Navigation to Main Dashboard Siswa
        btnLogin.setOnClickListener {
            val intent = Intent(this, MainActivitySiswa::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        
        btnGoogleLogin.setOnClickListener {
            val intent = Intent(this, MainActivitySiswa::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Navigate to Register Activity
        tvToRegister.setOnClickListener {
            val intent = Intent(this, RegisterSiswaActivity::class.java)
            startActivity(intent)
            finish() // finish login so it's not kept in backstack redundantly
        }
    }
}
