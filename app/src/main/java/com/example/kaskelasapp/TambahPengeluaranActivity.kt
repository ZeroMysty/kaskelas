package com.example.kaskelasapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class TambahPengeluaranActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pengeluaran)

        val db = DatabaseHelper(this)
        val etNama = findViewById<EditText>(R.id.etNamaPengeluaran)
        val etJumlah = findViewById<EditText>(R.id.etJumlahPengeluaran)
        val etKet = findViewById<EditText>(R.id.etKeteranganPengeluaran)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanPengeluaran)

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString()
            val jumlah = etJumlah.text.toString()
            val ket = etKet.text.toString()
            val tanggal = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

            if (nama.isNotEmpty() && jumlah.isNotEmpty()) {
                db.insertTransaksi(nama, jumlah, tanggal, "KELUAR", ket)
                Toast.makeText(this, "Pengeluaran disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.btnBackPengeluaran).setOnClickListener { finish() }
    }
}
