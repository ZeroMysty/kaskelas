package com.example.kaskelasapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditAnggotaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_anggota)
        val db = DatabaseHelper(this)

        val btnHapus = findViewById<Button>(R.id.btnHapusAnggota)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanEdit)
        val btnBack = findViewById<ImageView>(R.id.btnBackEdit)

        val etNama = findViewById<EditText>(R.id.etEditNama)
        val etId = findViewById<EditText>(R.id.etEditID)
        val etNis = findViewById<EditText>(R.id.etEditNIS)

        val anggotaId = intent.getStringExtra("ANGGOTA_ID")
        val anggotaNama = intent.getStringExtra("ANGGOTA_NAMA")
        val anggotaNis = intent.getStringExtra("ANGGOTA_NIS")

        if (anggotaId == null) {
            Toast.makeText(this, "Data anggota tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Isi field dengan data sebelumnya
        etId.setText(anggotaId)
        etId.isEnabled = false
        etNama.setText(anggotaNama ?: "")
        etNis.setText(anggotaNis ?: "")

        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            val newNama = etNama.text.toString().trim()
            val newNis = etNis.text.toString().trim()

            if (newNama.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rows = db.updateAnggota(anggotaId, newNama, newNis)
            if (rows > 0) {
                Toast.makeText(this, "Berhasil diperbarui", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal memperbarui", Toast.LENGTH_SHORT).show()
            }
            finish()
        }

        btnHapus.setOnClickListener {
            val deleted = db.deleteAnggota(anggotaId)
            if (deleted > 0) {
                Toast.makeText(this, "Anggota dihapus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menghapus anggota", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}