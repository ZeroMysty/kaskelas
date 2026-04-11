package com.example.kaskelasapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
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

        etJumlah.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    etJumlah.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = DecimalFormat("#,###").format(parsed).replace(",", ".")

                        current = formatted
                        etJumlah.setText(formatted)
                        etJumlah.setSelection(formatted.length)
                    } else {
                        current = ""
                        etJumlah.setText("")
                    }
                    etJumlah.addTextChangedListener(this)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val anggotaId = intent.getStringExtra("ANGGOTA_ID")
        val anggotaNama = intent.getStringExtra("ANGGOTA_NAMA")

        if (anggotaNama != null) {
            etNama.setText(anggotaNama)
            etNama.isEnabled = false
        }

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString()

            val jumlahRaw = etJumlah.text.toString()
            val jumlahBersih = jumlahRaw.replace(".", "")

            val ket = etKet.text.toString()
            val tanggal = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

            if (nama.isNotEmpty() && jumlahBersih.isNotEmpty()) {
                db.insertTransaksi(nama, jumlahBersih, tanggal, "MASUK", ket)
                Toast.makeText(this, "Pemasukan disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.btnBackPemasukan).setOnClickListener { finish() }
    }
}