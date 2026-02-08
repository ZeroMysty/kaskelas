package com.example.kaskelasapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class PemasukanAnggotaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pemasukan_anggota)

        val db = DatabaseHelper(this)
        val anggotaId = intent.getStringExtra("ANGGOTA_ID") ?: ""
        val anggotaNama = intent.getStringExtra("ANGGOTA_NAMA") ?: ""

        val etNama = findViewById<EditText>(R.id.etNamaBayar)
        val etJumlah = findViewById<EditText>(R.id.etJumlahBayar)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanBayar)

        etNama.setText(anggotaNama)

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString()
            val jumlah = etJumlah.text.toString()
            val tanggal = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

            if (nama.isNotEmpty() && jumlah.isNotEmpty()) {
                db.insertTransaksi(nama, jumlah, tanggal, "MASUK", "Pembayaran dari $anggotaNama")
                Toast.makeText(this, "Pemasukan disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.btnBackBayar).setOnClickListener { finish() }
    }
}
