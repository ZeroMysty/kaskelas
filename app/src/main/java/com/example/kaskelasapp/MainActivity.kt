package com.example.kaskelasapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity

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
import androidx.core.graphics.toColorInt
import android.graphics.Color

class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var rvAnggotaBeranda: RecyclerView
    private lateinit var adapter: AnggotaBayarAdapter

    @SuppressLint("ClickableViewAccessibility")
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
            false}

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
        }
        val ivGrafikDummy = findViewById<View>(R.id.lineChart)
        val openChartDetail = {
            startActivity(Intent(this, ChartDetailActivity::class.java))
        }
        findViewById<View>(R.id.cardGrafik)?.setOnClickListener { openChartDetail() }
        ivGrafikDummy?.setOnClickListener { openChartDetail() }

        BottomNavHelper.setupBottomNav(this)
        BackgroundHelper.applyAnimatedBackground(this)
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
        val formatRupiah = java.text.NumberFormat.getNumberInstance(localeID)
            .format(nominalDefault.toLongOrNull() ?: 0)

        android.app.AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pembayaran")
            .setMessage("Apakah Anda akan melanjutkan pembayaran kas sebesar Rp$formatRupiah untuk ${anggota.nama}?")
            .setPositiveButton("Lanjut") { _, _ ->
                val tgl = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date())
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
                android.widget.Toast.makeText(
                    this,
                    "Pembayaran berhasil!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateSaldo() {
        val total = db.hitungTotalSaldo()
        val formatRupiah =
            java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
                maximumFractionDigits = 0
                minimumFractionDigits = 0
            }
        findViewById<android.widget.TextView>(R.id.tvTotalSaldo).text = formatRupiah.format(total)
    }

    private fun loadChart() {
        val chart = findViewById<LineChart>(R.id.lineChart) ?: return

        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        // 🔥 FIX SORTING: Urutkan berdasarkan Tanggal (ASC) dan ID (ASC)
        // Agar transaksi di hari yang sama tidak berantakan di grafik
        val transaksi = db.getAllTransaksi().sortedWith(compareBy({
            try {
                sdf.parse(it.tanggal)
            } catch (e: Exception) {
                Date(0)
            }
        }, { it.id }))

        val entries = mutableListOf<Entry>()
        var saldo = 0f

        // Titik awal
        entries.add(Entry(0f, 0f))

        transaksi.forEachIndexed { index, it ->
            val cleanJumlah = it.jumlah.replace(".", "").replace(",", "")
            val jumlah = cleanJumlah.toFloatOrNull() ?: 0f

            if (it.tipe == "MASUK") {
                saldo += jumlah
            } else {
                saldo -= jumlah
            }

            entries.add(Entry((index + 1).toFloat(), saldo))
        }

        if (entries.size == 1) {
            entries.add(Entry(1f, 0f))
        }

        val dataSet = LineDataSet(entries, "Saldo").apply {
            color = Color.parseColor("#2563EB") // Biru yang lebih vibrant
            lineWidth = 3.5f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            setDrawCircles(true)
            setCircleColor(Color.parseColor("#2563EB"))
            circleRadius = 4f
            circleHoleRadius = 2f
            setDrawCircleHole(true)
            setDrawValues(false)
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(this@MainActivity, R.drawable.chart_gradient)
            
            // Highlight styling
            setDrawHorizontalHighlightIndicator(false)
            setDrawVerticalHighlightIndicator(true)
            highLightColor = Color.parseColor("#2563EB")
            highlightLineWidth = 1.5f
        }

        chart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false

            // Styling Axis
            axisRight.isEnabled = false
            
            axisLeft.apply {
                isEnabled = true
                setDrawGridLines(true)
                gridColor = Color.parseColor("#10000000") // Grid tipis
                textColor = Color.parseColor("#64748B")
                textSize = 9f
                setLabelCount(4, true)
                axisLineColor = Color.TRANSPARENT
                // Format ribuan
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value >= 1000 || value <= -1000) "${(value / 1000).toInt()}k" else value.toInt().toString()
                    }
                }
            }

            xAxis.apply {
                isEnabled = true
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.parseColor("#64748B")
                textSize = 9f
                axisLineColor = Color.TRANSPARENT
                setLabelCount(5, false)
            }

            setDrawBorders(false)
            setDrawGridBackground(false)
            
            // Atur padding agar label terlihat
            setExtraOffsets(10f, 0f, 10f, 10f)

            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(false)
            setPinchZoom(false)
            
            animateX(1200)
            invalidate()
        }
    }
}