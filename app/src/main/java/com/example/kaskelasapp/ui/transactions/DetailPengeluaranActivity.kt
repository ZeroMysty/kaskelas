package com.example.kaskelasapp.ui.transactions

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.data.DatabaseHelper
import com.example.kaskelasapp.utils.BackgroundHelper

class DetailPengeluaranActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pengeluaran)

        val db = DatabaseHelper(this)
        val transaksiId = intent.getIntExtra("TRANSAKSI_ID", -1)

        if (transaksiId != -1) {
            val transaksi = db.getTransaksiById(transaksiId)
            if (transaksi != null) {
                findViewById<TextView>(R.id.tvDetailNamaP).text = transaksi.nama
                findViewById<TextView>(R.id.tvDetailJumlahP).text = "Rp ${transaksi.jumlah}"
                findViewById<TextView>(R.id.tvDetailTanggalP).text = transaksi.tanggal
            }
        }

        findViewById<ImageButton>(R.id.btnBackDetailP).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSelesaiDetailP).setOnClickListener { finish() }

        BackgroundHelper.applyAnimatedBackground(this)
        
        // Quick Entrance Anim
        findViewById<View>(R.id.tvDetailJumlahP)?.parent?.let { parent ->
            if (parent is View) {
                parent.alpha = 0f
                parent.translationY = 50f
                parent.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(100).start()
            }
        }
    }
}

