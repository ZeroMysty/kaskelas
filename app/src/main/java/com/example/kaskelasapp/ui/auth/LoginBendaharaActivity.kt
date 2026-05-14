package com.example.kaskelasapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.ui.main.MainActivity
import com.example.kaskelasapp.utils.BackgroundHelper
import com.google.android.material.button.MaterialButton

class LoginBendaharaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_bendahara)
        BackgroundHelper.applyAnimatedBackground(this)

        val btnBack = findViewById<TextView>(R.id.btnBackLogin)
        val btnLogin = findViewById<android.widget.Button>(R.id.btnLogin)
        val tvToRegister = findViewById<TextView>(R.id.tvToRegister)

        btnBack.setOnClickListener {
            finish()
        }

        // Mock Navigation to Main Dashboard
        btnLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Navigate to Register Activity
        tvToRegister.setOnClickListener {
            val intent = Intent(this, RegisterBendaharaActivity::class.java)
            startActivity(intent)
            finish() // finish login so it's not kept in backstack redundantly
        }
    }
}
