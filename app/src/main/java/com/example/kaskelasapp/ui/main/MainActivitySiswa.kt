package com.example.kaskelasapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.ui.auth.RoleSelectionActivity
import com.example.kaskelasapp.utils.BackgroundHelper
import androidx.cardview.widget.CardView

class MainActivitySiswa : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main_siswa)
            // BackgroundHelper.applyAnimatedBackground(this)

        val btnProfileSiswa = findViewById<ImageView>(R.id.btnProfileSiswa)
        val btnBayarKas = findViewById<CardView>(R.id.btnBayarKas)
        val btnRiwayatSiswa = findViewById<CardView>(R.id.btnRiwayatSiswa)

        val tvNamaSiswa = findViewById<android.widget.TextView>(R.id.tvSubGreetingSiswa)
        val tvStatusKas = findViewById<android.widget.TextView>(R.id.tvStatusKasSiswa)
        val tvTotalKasKelas = findViewById<android.widget.TextView>(R.id.tvTotalKasKelasTransparansi)
        val tvTabungan = findViewById<android.widget.TextView>(R.id.tvTabunganSiswa)
        val rvRiwayat = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvRiwayatSiswa)

        val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", "") ?: ""
        val userNama = sharedPref.getString("user_nama", "Siswa")

        tvNamaSiswa.text = "Halo, $userNama"

        rvRiwayat?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        
        loadStudentData()
        
        btnProfileSiswa.setOnClickListener {
            // Intent to profile or logout
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadStudentData() {
        try {
            val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
            val userId = sharedPref.getString("user_id", "") ?: ""
            
            val tvStatus = findViewById<android.widget.TextView>(R.id.tvStatusKasSiswa)
            val tvTotalKelas = findViewById<android.widget.TextView>(R.id.tvTotalKasKelasTransparansi)
            val tvTabungan = findViewById<android.widget.TextView>(R.id.tvTabunganSiswa)
            val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvRiwayatSiswa)
            
            val db = com.example.kaskelasapp.data.DatabaseHelper(this)
            
            // 1. Total Tabungan Saya
            val saldo = db.getSaldoBySiswa(userId)
            val localeID = java.util.Locale.forLanguageTag("id-ID")
            val formatter = java.text.NumberFormat.getCurrencyInstance(localeID).apply {
                maximumFractionDigits = 0
            }
            
            tvTabungan?.text = formatter.format(saldo)
            
            // 2. Total Kas Kelas
            val totalKelas = db.hitungTotalSaldo()
            tvTotalKelas?.text = formatter.format(totalKelas)
            
            // 3. Status (Simple logic)
            if (saldo > 0) {
                tvStatus?.text = "AKTIF"
                tvStatus?.setTextColor(android.graphics.Color.WHITE)
            } else {
                tvStatus?.text = "MENUNGGAK"
                tvStatus?.setTextColor(android.graphics.Color.WHITE)
            }
            
            // 4. Riwayat Singkat (Limit 3)
            val list = db.getTransaksiByAnggota(userId).take(3)
            if (rv != null) {
                val adapter = com.example.kaskelasapp.ui.history.RiwayatAdapter(list) { transaksi ->
                     val intent = Intent(this, com.example.kaskelasapp.ui.history.DetailTransaksiSiswaActivity::class.java)
                     intent.putExtra("TRANSAKSI_ID", transaksi.id)
                     startActivity(intent)
                }
                rv.adapter = adapter
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("MainActivitySiswa", "Error loading student data: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        loadStudentData()
    }
}
