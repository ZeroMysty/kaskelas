package com.example.kaskelasapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Button
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
                findViewById<TextView>(R.id.tvDetailNamaPe).text = transaksi.nama
                findViewById<TextView>(R.id.tvDetailJumlahPe).text = "Rp ${transaksi.jumlah}"
                findViewById<TextView>(R.id.tvDetailTanggalPe).text = transaksi.tanggal
                findViewById<TextView>(R.id.tvDetailKeteranganPe).text = transaksi.keterangan
            }
        }

        findViewById<ImageButton>(R.id.btnBackDetailPe).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSelesaiDetailPe).setOnClickListener { finish() }

        BackgroundHelper.applyAnimatedBackground(this)
        
        // Quick Entrance Anim
        findViewById<View>(R.id.tvDetailJumlahPe)?.parent?.let { parent ->
            if (parent is View) {
                parent.alpha = 0f
                parent.translationY = 50f
                parent.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(100).start()
            }
        }
    }
}
