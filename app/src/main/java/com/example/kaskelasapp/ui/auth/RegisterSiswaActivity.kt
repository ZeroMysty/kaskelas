package com.example.kaskelasapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.ui.main.MainActivitySiswa
import com.example.kaskelasapp.utils.BackgroundHelper
import com.google.android.material.button.MaterialButton

class RegisterSiswaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_siswa)
        BackgroundHelper.applyAnimatedBackground(this)

        val btnBack = findViewById<android.widget.TextView>(R.id.btnBack)
        val btnRegister = findViewById<android.widget.Button>(R.id.btnRegister)

        btnBack.setOnClickListener {
            finish()
        }

        // Mock Navigation to Main Dashboard Siswa
        btnRegister.setOnClickListener {
            val intent = Intent(this, MainActivitySiswa::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Navigate to Login Activity
        val tvToLogin = findViewById<android.widget.TextView>(R.id.tvToLogin)
        tvToLogin?.setOnClickListener {
            val intent = Intent(this, LoginSiswaActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
