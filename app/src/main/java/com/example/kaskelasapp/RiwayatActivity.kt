package com.example.kaskelasapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RiwayatActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var rvRiwayat: RecyclerView
    private lateinit var adapter: RiwayatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        db = DatabaseHelper(this)
        rvRiwayat = findViewById(R.id.rvRiwayat)
        rvRiwayat.layoutManager = LinearLayoutManager(this)

        loadTransaksi()
        updateTotals()

        findViewById<ImageView>(R.id.btnBackRiwayat).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadTransaksi()
        updateTotals()
    }

    private fun updateTotals() {
        val daftarTransaksi = db.getAllTransaksi()
        
        var totalMasuk = 0L
        var totalKeluar = 0L
        
        daftarTransaksi.forEach {
            val jumlah = it.jumlah.toLongOrNull() ?: 0
            if (it.tipe == "MASUK") {
                totalMasuk += jumlah
            } else {
                totalKeluar += jumlah
            }
        }
        
        findViewById<TextView>(R.id.tvTotalPemasukanRiwayat).text = "Rp $totalMasuk"
        findViewById<TextView>(R.id.tvTotalPengeluaranRiwayat).text = "Rp $totalKeluar"
    }

    private fun loadTransaksi() {
        val daftarTransaksi = db.getAllTransaksi()
        adapter = RiwayatAdapter(daftarTransaksi) { transaksi ->
            val intent = if (transaksi.tipe == "MASUK") {
                Intent(this, DetailPemasukanActivity::class.java)
            } else {
                Intent(this, DetailPengeluaranActivity::class.java)
            }
            intent.putExtra("TRANSAKSI_ID", transaksi.id)
            startActivity(intent)
        }
        rvRiwayat.adapter = adapter
    }
}
