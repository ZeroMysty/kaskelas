package com.example.kaskelasapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TambahPemasukanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pemasukan)

        val db = DatabaseHelper(this)

        // 🔥 Ambil dari XML (HARUS SESUAI ID)
        val etNama = findViewById<EditText>(R.id.etNamaPembayaran)
        val etJumlah = findViewById<EditText>(R.id.etJumlahPemasukan)
        val etKet = findViewById<EditText>(R.id.etKeteranganPemasukan)
        val btnSimpan = findViewById<Button >(R.id.btnSimpanPemasukan)

        // 🔥 Ambil anggotaId dari intent
        val anggotaId = intent.getStringExtra("ANGGOTA_ID")

        // 🔥 FORMAT RUPIAH
        etJumlah.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    etJumlah.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDoubleOrNull() ?: 0.0
                        val formatted = DecimalFormat("#,###")
                            .format(parsed)
                            .replace(",", ".")

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

        // 🔥 BUTTON SIMPAN → ALERT DULU
        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString()
            val jumlahRaw = etJumlah.text.toString()
            val jumlahBersih = jumlahRaw.replace(".", "")
            val ket = etKet.text.toString()
            val tanggal = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

            if (nama.isEmpty() || jumlahBersih.isEmpty()) {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val message = """
                Yakin ingin menyimpan pemasukan?

                Nama       : $nama
                Jumlah     : Rp $jumlahRaw
                Tanggal    : $tanggal
                Keterangan : ${if (ket.isEmpty()) "-" else ket}
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Pemasukan")
                .setMessage(message)
                .setPositiveButton("Ya") { _, _ ->
                    try {
                        val result = db.insertTransaksi(
                            nama,
                            jumlahBersih,
                            tanggal,
                            "MASUK",
                            ket,
                            anggotaId // 🔥 FIX UTAMA
                        )

                        if (result == -1L) {
                            Toast.makeText(this, "Gagal menyimpan ke database", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Pemasukan disimpan!", Toast.LENGTH_SHORT).show()
                            finish()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }

        // 🔙 tombol back
        findViewById<ImageView>(R.id.btnBackPemasukan).setOnClickListener {
            finish()
        }
    }
}