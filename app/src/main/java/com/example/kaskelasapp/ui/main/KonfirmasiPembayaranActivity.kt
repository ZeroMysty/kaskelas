package com.example.kaskelasapp.ui.main

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.kaskelasapp.R
import com.example.kaskelasapp.utils.BackgroundHelper

class KonfirmasiPembayaranActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konfirmasi_pembayaran)
        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvKonfirmasi)
        val tvEmpty = findViewById<android.widget.TextView>(R.id.tvEmptyKonfirmasi)
        
        rv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        
        loadPending(rv, tvEmpty)
        
        findViewById<android.widget.ImageButton>(R.id.btnBackKonfirmasi).setOnClickListener { finish() }
    }

    private fun loadPending(rv: androidx.recyclerview.widget.RecyclerView, tvEmpty: android.widget.TextView) {
        val db = com.example.kaskelasapp.data.DatabaseHelper(this)
        val list = db.getPendingTransaksi()
        
        if (list.isEmpty()) {
            tvEmpty.visibility = android.view.View.VISIBLE
            rv.visibility = android.view.View.GONE
        } else {
            tvEmpty.visibility = android.view.View.GONE
            rv.visibility = android.view.View.VISIBLE
            
            val adapter = KonfirmasiAdapter(
                list,
                onApprove = { transaksi ->
                    db.updateTransaksiStatus(transaksi.id, "SUCCESS")
                    android.widget.Toast.makeText(this, "Pembayaran disetujui!", android.widget.Toast.LENGTH_SHORT).show()
                    loadPending(rv, tvEmpty) // Refresh
                },
                onReject = { transaksi ->
                    db.updateTransaksiStatus(transaksi.id, "REJECTED")
                    android.widget.Toast.makeText(this, "Pembayaran ditolak!", android.widget.Toast.LENGTH_SHORT).show()
                    loadPending(rv, tvEmpty) // Refresh
                }
            )
            rv.adapter = adapter
        }
    }
}
