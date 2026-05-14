package com.example.kaskelasapp.ui.members

import com.example.kaskelasapp.R
import com.example.kaskelasapp.data.DatabaseHelper
import com.example.kaskelasapp.utils.BackgroundHelper
import com.example.kaskelasapp.ui.history.RiwayatAdapter

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.example.kaskelasapp.ui.transactions.DetailPemasukanActivity
import com.example.kaskelasapp.ui.transactions.DetailPengeluaranActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetailAnggotaActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: RiwayatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_anggota)
        BackgroundHelper.applyAnimatedBackground(this)


        db = DatabaseHelper(this)

        val anggotaId = intent.getStringExtra("ANGGOTA_ID") ?: ""
        val anggotaNama = intent.getStringExtra("ANGGOTA_NAMA") ?: "Nama Tidak Diketahui"
        val anggotaNis = intent.getStringExtra("ANGGOTA_NIS") ?: "-"

        findViewById<TextView>(R.id.tvNamaDetail).text = anggotaNama
        findViewById<TextView>(R.id.tvNisDetail).text = "NIS: $anggotaNis"

        findViewById<ImageView>(R.id.btnBackDetail).setOnClickListener { finish() }

        val rvRiwayatAnggota = findViewById<RecyclerView>(R.id.rvRiwayatAnggota)
        rvRiwayatAnggota.layoutManager = LinearLayoutManager(this)

        if (anggotaId.isNotEmpty()) {
            val historyList = db.getTransaksiByAnggota(anggotaId)
            adapter = RiwayatAdapter(historyList) { transaksi ->
                val intent = if (transaksi.tipe == "MASUK") {
                    android.content.Intent(this, DetailPemasukanActivity::class.java)
                } else {
                    android.content.Intent(this, DetailPengeluaranActivity::class.java)
                }
                intent.putExtra("TRANSAKSI_ID", transaksi.id)
                startActivity(intent)
            }
            rvRiwayatAnggota.adapter = adapter
        }
    }
}

