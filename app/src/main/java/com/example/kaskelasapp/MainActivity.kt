package com.example.kaskelasapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var rvAnggotaBeranda: RecyclerView
    private lateinit var adapter: AnggotaBayarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

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
            startActivity(Intent(this, RiwayatActivity::class.java))
        }
        findViewById<View>(R.id.fabTambahAnggota).setOnClickListener {
            startActivity(Intent(this, AnggotaActivity::class.java))
        }

        // --- NAVIGASI EXPAND CHART (YANG DIPERBAIKI) ---

        // 1. Klik Tombol Expand
        findViewById<View>(R.id.btnExpandChart).setOnClickListener {
            val intent = Intent(this, ChartDetailActivity::class.java)
            startActivity(intent)
        }

        // 2. Klik Area Grafik (Opsional: Agar user lebih mudah expand)
        findViewById<View>(R.id.ivGrafikDummy).setOnClickListener {
            val intent = Intent(this, ChartDetailActivity::class.java)
            startActivity(intent)
        }
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
        adapter = AnggotaBayarAdapter(daftarAnggota) { anggota ->
            val intent = Intent(this, TambahPemasukanActivity::class.java)
            intent.putExtra("ANGGOTA_ID", anggota.id)
            intent.putExtra("ANGGOTA_NAMA", anggota.nama)
            startActivity(intent)
        }
        rvAnggotaBeranda.adapter = adapter
    }

    private fun updateSaldo() {
        val total = db.hitungTotalSaldo()
        val formatRupiah = java.text.NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
        findViewById<android.widget.TextView>(R.id.tvTotalUangkas).text = formatRupiah.format(total)
    }
    private fun loadChart() {
        val chart = findViewById<LineChart>(R.id.ivGrafikDummy)
        if (chart == null) return

        val transaksi = db.getAllTransaksi()

        val masukanByDate = mutableMapOf<String, Long>()
        val keluarByDate = mutableMapOf<String, Long>()

        transaksi.forEach {
            val jumlah = it.jumlah.toLongOrNull() ?: 0
            val date = it.tanggal

            if (it.tipe == "MASUK") {
                masukanByDate[date] = (masukanByDate[date] ?: 0) + jumlah
            } else {
                keluarByDate[date] = (keluarByDate[date] ?: 0) + jumlah
            }
        }

        val allDates = (masukanByDate.keys + keluarByDate.keys)
            .distinct()
            .sorted()

        // 🔥 BUAT SALDO (AKUMULASI)
        val entries = mutableListOf<Entry>()
        var saldo = 0f

        allDates.forEachIndexed { index, date ->
            val masuk = masukanByDate[date] ?: 0L
            val keluar = keluarByDate[date] ?: 0L

            saldo += (masuk - keluar).toFloat()
            entries.add(Entry(index.toFloat(), saldo))
        }

        if (entries.size < 2) {
            entries.add(Entry(0f, 0f))
            entries.add(Entry(1f, 0f))
        }

        // 🔥 BUAT SEGMENT (PER GARIS)
        val dataSets = mutableListOf<LineDataSet>()

        for (i in 1 until entries.size) {
            val prev = entries[i - 1]
            val curr = entries[i]

            val segment = listOf(prev, curr)

            val isNaik = curr.y >= prev.y

            val dataSet = LineDataSet(segment, "").apply {
                color = if (isNaik)
                    android.graphics.Color.GREEN
                else
                    android.graphics.Color.RED

                lineWidth = 3f

                setDrawCircles(false)
                setDrawValues(false)

                mode = LineDataSet.Mode.LINEAR
            }

            dataSets.add(dataSet)
        }

        chart.apply {
            data = LineData(dataSets as List<com.github.mikephil.charting.interfaces.datasets.ILineDataSet>)

            // 🔥 STYLE KAYAK SAHAM
            description.isEnabled = false
            legend.isEnabled = false

            axisRight.isEnabled = false

            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)

            // 🔥 ZOOM & DRAG
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            // 🔥 ANIMASI
            animateX(800)

            invalidate()
        }
    }
}