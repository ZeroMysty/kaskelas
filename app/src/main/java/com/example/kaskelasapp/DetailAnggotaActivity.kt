package com.example.kaskelasapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kaskelasapp.data.TransaksiEntity
import com.example.kaskelasapp.viewmodel.KasViewModel
import com.example.kaskelasapp.viewmodel.KasViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetailAnggotaActivity : AppCompatActivity() {

    private lateinit var viewModel: KasViewModel
    private lateinit var adapter: RiwayatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_anggota)
        BackgroundHelper.applyAnimatedBackground(this)

        viewModel = ViewModelProvider(
            this,
            KasViewModelFactory(application)
        )[KasViewModel::class.java]

        val anggotaId = intent.getStringExtra("ANGGOTA_ID") ?: ""
        val anggotaNama = intent.getStringExtra("ANGGOTA_NAMA") ?: "Nama Tidak Diketahui"
        val anggotaNis = intent.getStringExtra("ANGGOTA_NIS") ?: "-"
        // FIX #6: Ambil tanggal bergabung anggota untuk kalkulasi tunggakan yang akurat
        val tanggalBergabung = intent.getStringExtra("ANGGOTA_TANGGAL_BERGABUNG") ?: ""

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
                    
                    // FIX #6: Gunakan tanggalBergabung sebagai basis kalkulasi
                    kalkulasiTagihan(historyList, tanggalBergabung, anggotaNama)
                }
            }
        }
    }

    // FIX #6: Kalkulasi yang lebih akurat — menggunakan tanggal bergabung anggota,
    // bukan tanggal transaksi pertama (yang bisa bias jika ada transaksi pengeluaran)
    private fun kalkulasiTagihan(historyList: List<TransaksiEntity>, tanggalBergabung: String, anggotaNama: String) {
        val sharedPref = getSharedPreferences("SettingsKas", android.content.Context.MODE_PRIVATE)
        val nominalDefault = (sharedPref.getString("nominal_kas", "2000") ?: "2000").toLongOrNull() ?: 2000L

        var totalDibayar = 0L
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        historyList.forEach {
            if (it.tipe == "MASUK") {
                val jumlah = it.jumlah.replace(".", "").replace(",", "").toLongOrNull() ?: 0L
                totalDibayar += jumlah
            }
        }

        val localeID = java.util.Locale.Builder().setLanguage("id").setRegion("ID").build()
        val formatter = java.text.NumberFormat.getNumberInstance(localeID)

        val tvStatus = findViewById<TextView>(R.id.tvStatusTunggakan)

        // Tentukan tanggal basis: tanggalBergabung dari entity, fallback ke hari ini
        val basisDate: Date = if (tanggalBergabung.isNotEmpty()) {
            try {
                sdf.parse(tanggalBergabung) ?: Date()
            } catch (e: Exception) {
                Date()
            }
        } else {
            Date()
        }

        // Hitung minggu dari tanggal bergabung hingga sekarang
        val diffInMillies = Math.abs(Date().time - basisDate.time)
        val diffInWeeks = maxOf(1L, diffInMillies / (1000L * 60 * 60 * 24 * 7))

        val targetTotal = diffInWeeks * nominalDefault

        val btnTagihWa = findViewById<android.widget.Button>(R.id.btnTagihWa)

        if (diffInWeeks <= 1L && totalDibayar == 0L && tanggalBergabung.isEmpty()) {
            // Jika benar-benar baru bergabung dan belum ada transaksi
            findViewById<TextView>(R.id.tvTotalDibayar).text = "Rp ${formatter.format(0L)}"
            findViewById<TextView>(R.id.tvTargetKas).text = "Belum ada data"
            tvStatus.text = "BELUM ADA DATA"
            tvStatus.setTextColor(android.graphics.Color.parseColor("#64748B"))
            btnTagihWa.visibility = android.view.View.GONE
            return
        }

        findViewById<TextView>(R.id.tvTotalDibayar).text = "Rp ${formatter.format(totalDibayar)}"
        findViewById<TextView>(R.id.tvTargetKas).text = "Rp ${formatter.format(targetTotal)}"

        if (totalDibayar >= targetTotal) {
            tvStatus.text = "LUNAS"
            tvStatus.setTextColor(android.graphics.Color.parseColor("#16A34A"))
            btnTagihWa.visibility = android.view.View.GONE
        } else {
            val tunggakan = targetTotal - totalDibayar
            val formattedTunggakan = formatter.format(tunggakan)
            tvStatus.text = "MENUNGGAK Rp $formattedTunggakan"
            tvStatus.setTextColor(android.graphics.Color.parseColor("#DC2626"))
            
            btnTagihWa.visibility = android.view.View.VISIBLE
            btnTagihWa.setOnClickListener {
                val message = "Halo $anggotaNama, mengingatkan bahwa kamu masih memiliki tunggakan uang kas sebesar Rp $formattedTunggakan. Mohon segera dilunasi ya, terima kasih!"
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse("https://api.whatsapp.com/send?text=${java.net.URLEncoder.encode(message, "UTF-8")}")
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "WhatsApp tidak terinstal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
