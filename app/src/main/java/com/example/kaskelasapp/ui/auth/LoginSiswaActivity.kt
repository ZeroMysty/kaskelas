package com.example.kaskelasapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.ui.main.MainActivitySiswa
import com.example.kaskelasapp.utils.BackgroundHelper
import com.google.android.material.button.MaterialButton

class LoginSiswaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_login_siswa)
            // BackgroundHelper.applyAnimatedBackground(this)

            val btnBack = findViewById<TextView>(R.id.btnBackLogin)
            val btnLogin = findViewById<android.widget.Button>(R.id.btnLogin)
            val tvToRegister = findViewById<TextView>(R.id.tvToRegister)

            btnBack?.setOnClickListener {
                finish()
            }

            // Login logic using NIS
            btnLogin?.setOnClickListener {
                try {
                    val nis = findViewById<android.widget.EditText>(R.id.etUsernameLogin).text.toString()
                    if (nis.isNotEmpty()) {
                        val db = com.example.kaskelasapp.data.DatabaseHelper(this)
                        val anggota = db.findAnggotaByNis(nis)
                        
                        if (anggota != null) {
                            // Save session
                            val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("user_role", "siswa")
                                putString("user_id", anggota.id)
                                putString("user_nama", anggota.nama)
                                putString("user_nis", anggota.nis)
                                apply()
                            }
                            
                            val intent = Intent(this, MainActivitySiswa::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            android.widget.Toast.makeText(this, "NIS tidak ditemukan! Silakan daftar terlebih dahulu.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.widget.Toast.makeText(this, "Harap masukkan NIS!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(this, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }

            // Navigate to Register Activity
            tvToRegister?.setOnClickListener {
                val intent = Intent(this, RegisterSiswaActivity::class.java)
                startActivity(intent)
                finish() 
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
