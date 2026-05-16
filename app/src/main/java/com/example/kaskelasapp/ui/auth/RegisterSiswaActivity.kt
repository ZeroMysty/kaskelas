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
        try {
            setContentView(R.layout.activity_register_siswa)
            // BackgroundHelper.applyAnimatedBackground(this)

            val btnBack = findViewById<android.widget.TextView>(R.id.btnBack)
            val btnRegister = findViewById<android.widget.Button>(R.id.btnRegister)

            btnBack?.setOnClickListener {
                finish()
            }

            // Registration logic
            btnRegister?.setOnClickListener {
                try {
                    val nama = findViewById<android.widget.EditText>(R.id.etNama).text.toString()
                    val nis = findViewById<android.widget.EditText>(R.id.etNis).text.toString()
                    
                    if (nama.isNotEmpty() && nis.isNotEmpty()) {
                        val db = com.example.kaskelasapp.data.DatabaseHelper(this)
                        
                        // Check if already exists
                        if (db.findAnggotaByNis(nis) == null) {
                            val id = "SISWA_" + System.currentTimeMillis()
                            val result = db.insertAnggota(id, nama, nis)
                            
                            if (result != -1L) {
                                // Save session
                                val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putString("user_role", "siswa")
                                    putString("user_id", id)
                                    putString("user_nama", nama)
                                    putString("user_nis", nis)
                                    apply()
                                }
                                
                                android.widget.Toast.makeText(this, "Registrasi berhasil!", android.widget.Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivitySiswa::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                android.widget.Toast.makeText(this, "Registrasi gagal! Coba lagi.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            android.widget.Toast.makeText(this, "NIS sudah terdaftar!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.widget.Toast.makeText(this, "Harap isi semua bidang!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(this, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }

            // Navigate to Login Activity
            val tvToLogin = findViewById<android.widget.TextView>(R.id.tvToLogin)
            tvToLogin?.setOnClickListener {
                val intent = Intent(this, LoginSiswaActivity::class.java)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
