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
import java.text.SimpleDateFormat
import java.util.*

class TambahAnggotaActivity : AppCompatActivity() {
    private lateinit var viewModel: KasViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_anggota)
        BackgroundHelper.applyAnimatedBackground(this)

        viewModel = ViewModelProvider(
            this,
            KasViewModelFactory(application)
        )[KasViewModel::class.java]

        val etNama = findViewById<EditText>(R.id.etNamaAnggota)
        val etID = findViewById<EditText>(R.id.etIdAnggota)
        val etNIS = findViewById<EditText>(R.id.etNisAnggota)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanAnggota)
        val btnBack = findViewById<ImageView>(R.id.btnBackTambahAnggota)

        btnBack.setOnClickListener { finish() }
        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString()
            val id = etID.text.toString()
            val nis = etNIS.text.toString()

            if (nama.isNotEmpty() && id.isNotEmpty() && nis.isNotEmpty()) {
                val tanggalHariIni = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                val anggota = Anggota(
                    id = id,
                    nama = nama,
                    nis = nis,
                    tanggalBergabung = tanggalHariIni
                )
                viewModel.insertAnggota(anggota)
                Toast.makeText(this, "Anggota Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
