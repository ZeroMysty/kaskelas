package com.example.kaskelasapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.ui.main.MainActivity
import com.google.android.material.button.MaterialButton

class RegisterBendaharaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_bendahara)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val btnGoogle = findViewById<MaterialButton>(R.id.btnGoogle)

        btnBack.setOnClickListener {
            finish()
        }

        // Mock Navigation to Main Dashboard
        btnRegister.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // Clear task so user can't press back to registration
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        
        btnGoogle.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Navigate to Login Activity
        val tvToLogin = findViewById<android.widget.TextView>(R.id.tvToLogin)
        tvToLogin?.setOnClickListener {
            val intent = Intent(this, LoginBendaharaActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
