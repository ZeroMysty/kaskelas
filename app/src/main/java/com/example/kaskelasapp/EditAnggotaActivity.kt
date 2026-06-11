package com.example.kaskelasapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.kaskelasapp.viewmodel.KasViewModel
import com.example.kaskelasapp.viewmodel.KasViewModelFactory

class EditAnggotaActivity : AppCompatActivity() {
    private lateinit var viewModel: KasViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_anggota)
        BackgroundHelper.applyAnimatedBackground(this)

        viewModel = ViewModelProvider(
            this,
            KasViewModelFactory(application)
        )[KasViewModel::class.java]

        val anggotaId = intent.getStringExtra("ANGGOTA_ID") ?: ""
        val anggotaNama = intent.getStringExtra("ANGGOTA_NAMA") ?: ""
        val anggotaNis = intent.getStringExtra("ANGGOTA_NIS") ?: ""
        val tanggalBergabung = intent.getStringExtra("ANGGOTA_TANGGAL_BERGABUNG") ?: ""

        val etNama = findViewById<EditText>(R.id.etEditNama)
        val etNis = findViewById<EditText>(R.id.etEditNis)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanEdit)
        val btnHapus = findViewById<Button>(R.id.btnHapusAnggota)
        val btnBack = findViewById<ImageView>(R.id.btnBackEdit)

        etNama.setText(anggotaNama)
        etNis.setText(anggotaNis)

        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            val newNama = etNama.text.toString()
            val newNis = etNis.text.toString()

            if (newNama.isNotEmpty() && newNis.isNotEmpty()) {
                val anggota = Anggota(
                    id = anggotaId,
                    nama = newNama,
                    nis = newNis,
                    tanggalBergabung = tanggalBergabung
                )
                viewModel.updateAnggota(anggota)
                Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnHapus.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Hapus Anggota")
                .setMessage("Apakah Anda yakin ingin menghapus $anggotaNama?")
                .setPositiveButton("Ya") { _, _ ->
                    viewModel.deleteAnggota(anggotaId)
                    Toast.makeText(this, "Anggota dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Tidak", null)
                .show()
        }
    }
}