package com.example.kaskelasapp.ui.main

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.utils.BackgroundHelper

class BayarKasSiswaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bayar_kas_siswa)
        BackgroundHelper.applyAnimatedBackground(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBackBayarSiswa)
        btnBack.setOnClickListener {
            finish()
        }

        val etAmount = findViewById<android.widget.EditText>(R.id.etAmountBayarSiswa)
        val etNotes = findViewById<android.widget.EditText>(R.id.etNotesBayarSiswa)
        val btnSubmit = findViewById<android.widget.Button>(R.id.btnSubmitBayarSiswa)

        val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", "") ?: ""
        val userNama = sharedPref.getString("user_nama", "Siswa")

        btnSubmit.setOnClickListener {
            val amountStr = etAmount.text.toString()
            val notes = etNotes.text.toString()
            
            if (amountStr.isNotEmpty()) {
                val db = com.example.kaskelasapp.data.DatabaseHelper(this)
                val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                val currentDate = sdf.format(java.util.Date())
                
                val result = db.insertTransaksi(
                    judul = "Kas: $userNama",
                    jumlah = amountStr,
                    tanggal = currentDate,
                    jenis = "MASUK",
                    keterangan = if (notes.isNotEmpty()) notes else "Pembayaran Kas oleh Siswa",
                    anggotaId = userId,
                    metode = "E-WALLET", // Default for student payment through app
                    status = "PENDING"  // Need verification from Bendahara
                )
                
                if (result != -1L) {
                    android.widget.Toast.makeText(this, "Laporan pembayaran berhasil dikirim!", android.widget.Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    android.widget.Toast.makeText(this, "Gagal mengirim laporan!", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                android.widget.Toast.makeText(this, "Harap isi jumlah pembayaran!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
