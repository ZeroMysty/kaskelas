package com.example.kaskelasapp.ui.history

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.utils.BackgroundHelper

class RiwayatSiswaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_siswa)
        BackgroundHelper.applyAnimatedBackground(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBackRiwayatSiswa)
        btnBack.setOnClickListener {
            finish()
        }

        val rvRiwayat = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvRiwayatSiswaPage)
        val tvTotalDibayar = findViewById<android.widget.TextView>(R.id.tvTotalDibayarSiswa)
        val tvStatus = findViewById<android.widget.TextView>(R.id.tvStatusSiswaRiwayat)
        
        rvRiwayat.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        
        val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", "") ?: ""
        
        loadHistory(userId, rvRiwayat, tvTotalDibayar, tvStatus)
    }

    private fun loadHistory(userId: String, rv: androidx.recyclerview.widget.RecyclerView, tvTotal: android.widget.TextView, tvStatus: android.widget.TextView) {
        try {
            val db = com.example.kaskelasapp.data.DatabaseHelper(this)
            val list = db.getTransaksiByAnggota(userId)
            
            val adapter = RiwayatAdapter(list) { transaksi ->
                val intent = android.content.Intent(this, DetailTransaksiSiswaActivity::class.java)
                intent.putExtra("TRANSAKSI_ID", transaksi.id)
                startActivity(intent)
            }
            rv.adapter = adapter
            
            // Calculate Total Paid
            val total = list.filter { it.tipe == "MASUK" }.sumOf { it.jumlah.toLongOrNull() ?: 0L }
            val localeID = java.util.Locale.forLanguageTag("id-ID")
            val formatter = java.text.NumberFormat.getCurrencyInstance(localeID).apply {
                maximumFractionDigits = 0
            }
            tvTotal.text = formatter.format(total)
            
            // Update Status (Simple logic for now)
            if (total > 0) {
                tvStatus.text = "AKTIF"
            } else {
                tvStatus.text = "BELUM BAYAR"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
