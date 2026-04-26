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

        setupBottomNav()
    }

    private fun setupBottomNav() {
        findViewById<android.view.View>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        findViewById<android.view.View>(R.id.navHistory).setOnClickListener {
            // Already in history
        }
        findViewById<android.view.View>(R.id.navProfile).setOnClickListener {
            // To be implemented
        }
        findViewById<android.view.View>(R.id.navSettings).setOnClickListener {
            // To be implemented
        }
    }

    override fun onResume() {
        super.onResume()
        loadTransaksi()
        updateTotals()
    }

    private fun formatRupiah(number: Long): String {
        val localeID = java.util.Locale.Builder().setLanguage("id").setRegion("ID").build()
        return java.text.NumberFormat.getNumberInstance(localeID).format(number)
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
        val totalBalance = totalMasuk - totalKeluar
        
        findViewById<TextView>(R.id.tvTotalBalanceRiwayat).text = "Rp ${formatRupiah(totalBalance)}"
        findViewById<TextView>(R.id.tvTotalPemasukanRiwayat).text = "Income: Rp ${formatRupiah(totalMasuk)}"
        findViewById<TextView>(R.id.tvTotalPengeluaranRiwayat).text = "Expense: Rp ${formatRupiah(totalKeluar)}"
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
