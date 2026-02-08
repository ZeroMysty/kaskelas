package com.example.kaskelasapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailPemasukanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pemasukan)

        val db = DatabaseHelper(this)
        val transaksiId = intent.getIntExtra("TRANSAKSI_ID", -1)

        if (transaksiId != -1) {
            val transaksi = db.getTransaksiById(transaksiId)
            if (transaksi != null) {
                findViewById<TextView>(R.id.tvDetailNamaP).text = transaksi.nama
                findViewById<TextView>(R.id.tvDetailJumlahP).text = "Rp. ${transaksi.jumlah}"
                findViewById<TextView>(R.id.tvDetailTanggalP).text = transaksi.tanggal
                findViewById<TextView>(R.id.tvDetailKeteranganP).text = transaksi.keterangan
            }
        }

        findViewById<android.widget.ImageView>(R.id.btnBackDetailP).setOnClickListener { finish() }
        findViewById<android.widget.Button>(R.id.btnSelesaiDetailP).setOnClickListener { finish() }
    }
}