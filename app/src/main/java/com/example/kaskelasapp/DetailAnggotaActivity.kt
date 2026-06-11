package com.example.kaskelasapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.kaskelasapp.data.AppDatabase
import com.example.kaskelasapp.repository.KasRepository
import com.example.kaskelasapp.viewmodel.KasViewModel
import com.example.kaskelasapp.viewmodel.KasViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import android.graphics.Color

class DetailAnggotaActivity : AppCompatActivity() {

    private lateinit var viewModel: KasViewModel
    private lateinit var adapter: RiwayatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_anggota)
        BackgroundHelper.applyAnimatedBackground(this)

        val database = AppDatabase.getDatabase(this)
        val repository = KasRepository(database.anggotaDao(), database.transaksiDao())
        val factory = KasViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[KasViewModel::class.java]

        val anggotaId = intent.getStringExtra("ANGGOTA_ID") ?: ""
        val anggotaNama = intent.getStringExtra("ANGGOTA_NAMA") ?: "Nama Tidak Diketahui"
        val anggotaNis = intent.getStringExtra("ANGGOTA_NIS") ?: "-"

        findViewById<TextView>(R.id.tvNamaDetail).text = anggotaNama
        findViewById<TextView>(R.id.tvNisDetail).text = "NIS: $anggotaNis"

        findViewById<ImageView>(R.id.btnBackDetail).setOnClickListener { finish() }

        val rvRiwayatAnggota = findViewById<RecyclerView>(R.id.rvRiwayatAnggota)
        rvRiwayatAnggota.layoutManager = LinearLayoutManager(this)

        if (anggotaId.isNotEmpty()) {
            viewModel.loadTransaksiByAnggota(anggotaId)
            lifecycleScope.launch {
                viewModel.transaksiList.collect { historyList ->
                    adapter = RiwayatAdapter(historyList) { transaksi ->
                        val intent = if (transaksi.tipe == "MASUK") {
                            android.content.Intent(this@DetailAnggotaActivity, DetailPemasukanActivity::class.java)
                        } else {
                            android.content.Intent(this@DetailAnggotaActivity, DetailPengeluaranActivity::class.java)
                        }
                        intent.putExtra("TRANSAKSI_ID", transaksi.id)
                        startActivity(intent)
                    }
                    rvRiwayatAnggota.adapter = adapter
                    
                    kalkulasiTagihan(historyList)
                }
            }
        }
    }

    private fun kalkulasiTagihan(historyList: List<com.example.kaskelasapp.data.TransaksiEntity>) {
        val sharedPref = getSharedPreferences("SettingsKas", android.content.Context.MODE_PRIVATE)
        val nominalDefault = (sharedPref.getString("nominal_kas", "2000") ?: "2000").toLongOrNull() ?: 2000L

        var totalDibayar = 0L
        var earliestDate = Date()
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        historyList.forEach {
            if (it.tipe == "MASUK") {
                val jumlah = it.jumlah.replace(".", "").toLongOrNull() ?: 0L
                totalDibayar += jumlah
            }
            try {
                val d = sdf.parse(it.tanggal)
                if (d != null && d.before(earliestDate)) {
                    earliestDate = d
                }
            } catch (e: Exception) {}
        }

        // Asumsi 1 minggu = 1 target. Jika belum ada histori, target = 1 minggu.
        val diffInMillies = Math.abs(Date().time - earliestDate.time)
        val diffInWeeks = max(1L, (diffInMillies / (1000 * 60 * 60 * 24 * 7)))
        
        val targetTotal = diffInWeeks * nominalDefault

        val localeID = java.util.Locale.Builder().setLanguage("id").setRegion("ID").build()
        val formatter = java.text.NumberFormat.getNumberInstance(localeID)

        findViewById<TextView>(R.id.tvTotalDibayar).text = "Rp ${formatter.format(totalDibayar)}"
        findViewById<TextView>(R.id.tvTargetKas).text = "Rp ${formatter.format(targetTotal)}"

        val tvStatus = findViewById<TextView>(R.id.tvStatusTunggakan)
        if (totalDibayar >= targetTotal) {
            tvStatus.text = "LUNAS"
            tvStatus.setTextColor(Color.parseColor("#16A34A")) // Green
        } else {
            val tunggakan = targetTotal - totalDibayar
            tvStatus.text = "MENUNGGAK Rp ${formatter.format(tunggakan)}"
            tvStatus.setTextColor(Color.parseColor("#DC2626")) // Red
        }
    }
}
