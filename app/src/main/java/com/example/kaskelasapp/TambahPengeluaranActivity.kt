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

class TambahPengeluaranActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pengeluaran)

        val db = DatabaseHelper(this)

        val etNama = findViewById<EditText>(R.id.etNamaPengeluaran)
        val etJumlah = findViewById<EditText>(R.id.etJumlahPengeluaran)
        val etKet = findViewById<EditText>(R.id.etKeteranganPengeluaran)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanPengeluaran)

        // 🔥 FORMAT RUPIAH (AMAN)
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

        // 🔥 BUTTON SIMPAN → ALERT PREVIEW
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
                Yakin ingin menyimpan pengeluaran?

                Nama       : $nama
                Jumlah     : Rp $jumlahRaw
                Tanggal    : $tanggal
                Keterangan : ${if (ket.isEmpty()) "-" else ket}
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Pengeluaran")
                .setMessage(message)
                .setPositiveButton("Ya") { _, _ ->
                    try {
                        val result = db.insertTransaksi(
                            nama,
                            jumlahBersih,
                            tanggal,
                            "KELUAR",
                            ket
                        )

                        if (result == -1L) {
                            Toast.makeText(this, "Gagal menyimpan ke database", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Pengeluaran disimpan!", Toast.LENGTH_SHORT).show()
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

        // 🔙 BACK
        findViewById<ImageView>(R.id.btnBackPengeluaran).setOnClickListener {
            finish()
        }
    }
}