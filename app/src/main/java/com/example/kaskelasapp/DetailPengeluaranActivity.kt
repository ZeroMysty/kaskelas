package com.example.kaskelasapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailPengeluaranActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pengeluaran)

        val db = DatabaseHelper(this)
        val transaksiId = intent.getIntExtra("TRANSAKSI_ID", -1)

        if (transaksiId != -1) {
            val transaksi = db.getTransaksiById(transaksiId)
            if (transaksi != null) {
                findViewById<TextView>(R.id.tvDetailNamaK).text = transaksi.nama
                findViewById<TextView>(R.id.tvDetailJumlahK).text = "Rp ${transaksi.jumlah}"
                findViewById<TextView>(R.id.tvDetailTanggalK).text = transaksi.tanggal
                findViewById<TextView>(R.id.tvDetailKeteranganK).text = transaksi.keterangan
            }
        }

        findViewById<ImageView>(R.id.btnBackDetailK).setOnClickListener { finish() }
    }
}
