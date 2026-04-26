package com.example.kaskelasapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var rvAnggotaBeranda: RecyclerView
    private lateinit var adapter: AnggotaBayarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) 

        db = DatabaseHelper(this)

        rvAnggotaBeranda = findViewById(R.id.rvAnggotaBeranda)
        rvAnggotaBeranda.layoutManager = LinearLayoutManager(this)

        updateSaldo()
        loadChart()
        loadAnggotaBayar()

        // Hide keyboard when clicking outside
        window.decorView.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }

        // --- NAVIGASI TOMBOL ---
        findViewById<View>(R.id.btnNavPemasukan).setOnClickListener {
            startActivity(Intent(this, TambahPemasukanActivity::class.java))
        }
        findViewById<View>(R.id.btnNavPengeluaran).setOnClickListener {
            startActivity(Intent(this, TambahPengeluaranActivity::class.java))
        }
        findViewById<View>(R.id.btnHistoryMain).setOnClickListener {
            val intent = Intent(this, RiwayatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        // --- NAVIGASI EXPAND CHART DENGAN TRANSISI ---
        val cardGrafik = findViewById<View>(R.id.cardGrafik)
        val openChartDetail = {
            val intent = Intent(this, ChartDetailActivity::class.java)
            val options = androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, cardGrafik, "chart_transition"
            )
            startActivity(intent, options.toBundle())
        }
        cardGrafik?.setOnClickListener { openChartDetail() }
        findViewById<View>(R.id.ivGrafikDummy)?.setOnClickListener { openChartDetail() }

        // --- ANIMASI LAYOUT MASUK ---
        val rootLayout = findViewById<View>(android.R.id.content)
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.item_animation_fall_down)
        rootLayout.startAnimation(animation)

        BottomNavHelper.setupBottomNav(this)
    }

    override fun onResume() {
        super.onResume()
        updateSaldo()
        loadChart()
        loadAnggotaBayar()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    private fun loadAnggotaBayar() {
        val daftarAnggota = db.getAllAnggota()
        adapter = AnggotaBayarAdapter(
            list = daftarAnggota,
            onPayClick = { anggota ->
                tampilkanDialogBayar(anggota)
            },
            onItemClick = { anggota ->
                val intent = Intent(this, DetailAnggotaActivity::class.java)
                intent.putExtra("ANGGOTA_ID", anggota.id)
                intent.putExtra("ANGGOTA_NAMA", anggota.nama)
                intent.putExtra("ANGGOTA_NIS", anggota.nis)
                startActivity(intent)
            }
        )
        rvAnggotaBeranda.adapter = adapter
    }

    private fun tampilkanDialogBayar(anggota: Anggota) {
        val sharedPref = getSharedPreferences("SettingsKas", android.content.Context.MODE_PRIVATE)
        val nominalDefault = sharedPref.getString("nominal_kas", "2000") ?: "2000"

        val localeID = java.util.Locale.Builder().setLanguage("id").setRegion("ID").build()
        val formatRupiah = java.text.NumberFormat.getNumberInstance(localeID).format(nominalDefault.toLongOrNull() ?: 0)

        android.app.AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pembayaran")
            .setMessage("Apakah Anda akan melanjutkan pembayaran kas sebesar Rp$formatRupiah untuk ${anggota.nama}?")
            .setPositiveButton("Lanjut") { _, _ ->
                val tgl = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                db.insertTransaksi(
                    judul = "Bayar kas",
                    jumlah = nominalDefault,
                    tanggal = tgl,
                    jenis = "MASUK",
                    keterangan = "Pembayaran dari ${anggota.nama}",
                    anggotaId = anggota.id
                )
                updateSaldo()
                loadChart()
                android.widget.Toast.makeText(this, "Pembayaran berhasil!", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateSaldo() {
        val total = db.hitungTotalSaldo()
        val formatRupiah = java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
        findViewById<android.widget.TextView>(R.id.tvTotalUangkas).text = formatRupiah.format(total)
    }
    private fun loadChart() {
        val chart = findViewById<LineChart>(R.id.ivGrafikDummy)
        if (chart == null) return

        val transaksi = db.getAllTransaksi().sortedBy { it.id }

        val entries = mutableListOf<Entry>()
        var saldo = 0f

        transaksi.forEachIndexed { index, it ->
            // Bersihkan titik agar tidak salah hitung (TradingView style fix)
            val cleanJumlah = it.jumlah.replace(".", "").replace(",", "")
            val jumlah = cleanJumlah.toFloatOrNull() ?: 0f

            if (it.tipe == "MASUK") {
                saldo += jumlah
            } else {
                saldo -= jumlah
            }
            entries.add(Entry(index.toFloat(), saldo))
        }

        if (entries.isEmpty()) {
            entries.add(Entry(0f, 0f))
            entries.add(Entry(1f, 0f))
        }

        // 🔥 BUAT SATU DATASET UNTUK GRADIENT (TradingView Style)
        val dataSet = LineDataSet(entries, "Saldo").apply {
            // Warna garis biru premium
            color = android.graphics.Color.parseColor("#2196F3")
            lineWidth = 2.5f

            // Smooth curves (dibuat lebih tajam/dempet)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.08f

            setDrawCircles(false)
            setDrawValues(false)

            // 🔥 GRADIENT FILL
            setDrawFilled(true)
            val drawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.chart_gradient)
            fillDrawable = drawable

            // Hilangkan garis bantu saat disentuh
            setDrawHorizontalHighlightIndicator(false)
            highLightColor = android.graphics.Color.parseColor("#2196F3")
        }

        val dataSets = listOf(dataSet)

        chart.apply {
            data = LineData(dataSets as List<ILineDataSet>)

            // 🔥 STYLE MINIMALIS (TRADING VIEW)
            description.isEnabled = false
            legend.isEnabled = false

            axisRight.isEnabled = false
            axisLeft.isEnabled = false
            xAxis.isEnabled = false

            setDrawBorders(false)
            setDrawGridBackground(false)

            // 🔥 FORCE BERDEMPETAN (Edge to Edge)
            setExtraOffsets(0f, 0f, 0f, 0f)
            setViewPortOffsets(0f, 0f, 0f, 0f)

            // 🔥 PREVIEW MODE
            setTouchEnabled(false)
            animateY(1000)

            invalidate()
        }

    }
}