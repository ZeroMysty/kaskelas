package com.example.kaskelasapp

import android.os.Bundle
import android.widget.*
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
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



        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString()
            val jumlah = etJumlah.text.toString()
            val jumlahRaw = etJumlah.text.toString()
            val jumlahBersih = jumlahRaw.replace(".", "")
            val ket = etKet.text.toString()
            val tanggal = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

            if (nama.isNotEmpty() && jumlahBersih.isNotEmpty()) {
                db.insertTransaksi(nama, jumlahBersih, tanggal, "KELUAR", ket)
                Toast.makeText(this, "PENGELUARAN DI SIMPAN!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.btnBackPengeluaran).setOnClickListener { finish() }
    }
}
