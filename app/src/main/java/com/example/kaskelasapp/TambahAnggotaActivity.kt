package com.example.kaskelasapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import androidx.lifecycle.ViewModelProvider
import com.example.kaskelasapp.data.AppDatabase
import com.example.kaskelasapp.repository.KasRepository
import com.example.kaskelasapp.viewmodel.KasViewModel
import com.example.kaskelasapp.viewmodel.KasViewModelFactory

class TambahAnggotaActivity : AppCompatActivity() {
    private lateinit var viewModel: KasViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_anggota)
        BackgroundHelper.applyAnimatedBackground(this)

        val database = AppDatabase.getDatabase(this)
        val repository = KasRepository(database.anggotaDao(), database.transaksiDao())
        val factory = KasViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[KasViewModel::class.java]

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
                val anggota = Anggota(id, nama, nis)
                viewModel.insertAnggota(anggota)
                Toast.makeText(this, "Anggota Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
