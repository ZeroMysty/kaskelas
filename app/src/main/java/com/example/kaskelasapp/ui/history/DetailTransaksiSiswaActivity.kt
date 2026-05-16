package com.example.kaskelasapp.ui.history

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.utils.BackgroundHelper

class DetailTransaksiSiswaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_transaksi_siswa)
        BackgroundHelper.applyAnimatedBackground(this)

        val tvAmount = findViewById<android.widget.TextView>(R.id.tvDetailAmountSiswa)
        val tvJenis = findViewById<android.widget.TextView>(R.id.tvDetailJenisSiswa)
        val tvTanggal = findViewById<android.widget.TextView>(R.id.tvDetailTanggalSiswa)
        val btnClose = findViewById<android.widget.Button>(R.id.btnCloseDetailSiswa)

        val transaksiId = intent.getIntExtra("TRANSAKSI_ID", -1)
        
        if (transaksiId != -1) {
            val db = com.example.kaskelasapp.data.DatabaseHelper(this)
            val transaksi = db.getTransaksiById(transaksiId)
            
            transaksi?.let {
                val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
                tvAmount.text = formatter.format(it.jumlah.toLongOrNull() ?: 0L).replace("Rp", "Rp ")
                tvJenis.text = it.nama
                tvTanggal.text = it.tanggal
            }
        }

        btnClose.setOnClickListener {
            finish()
        }
    }
}
