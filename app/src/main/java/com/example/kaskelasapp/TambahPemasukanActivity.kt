package com.example.kaskelasapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class TambahPemasukanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pemasukan)

        val db = DatabaseHelper(this)
        val etNama = findViewById<EditText>(R.id.etNamaPembayaran)
        val etJumlah = findViewById<EditText>(R.id.etJumlahPemasukan)
        val etKet = findViewById<EditText>(R.id.etKeteranganPemasukan)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanPemasukan)

        // Check if this is from anggota payment
        val anggotaId = intent.getStringExtra("ANGGOTA_ID")
        val anggotaNama = intent.getStringExtra("ANGGOTA_NAMA")
        
        // If from anggota, pre-fill with anggota name and disable editing
        if (anggotaNama != null) {
            etNama.setText(anggotaNama)
            etNama.isEnabled = false
        }

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString()
            val jumlah = etJumlah.text.toString()
            val ket = etKet.text.toString()
            val tanggal = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

            if (nama.isNotEmpty() && jumlah.isNotEmpty()) {
                db.insertTransaksi(nama, jumlah, tanggal, "MASUK", ket)
                Toast.makeText(this, "Pemasukan disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.btnBackPemasukan).setOnClickListener { finish() }
    }
}