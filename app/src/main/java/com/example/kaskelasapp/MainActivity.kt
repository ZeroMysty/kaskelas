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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
        val ivGrafikDummy = findViewById<View>(R.id.barChart)
        val openChartDetail = {
            startActivity(Intent(this, ChartDetailActivity::class.java))
        }
        findViewById<View>(R.id.cardGrafik)?.setOnClickListener { openChartDetail() }
        ivGrafikDummy?.setOnClickListener { openChartDetail() }

        BottomNavHelper.setupBottomNav(this)
        BackgroundHelper.applyAnimatedBackground(this)

        // Tampilkan tutorial jika baru pertama kali install
        findViewById<View>(R.id.tutorialRoot).post {
            checkTutorial()
        }
    }

    private fun checkTutorial() {
        val sharedPref = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
        val isTutorialDone = sharedPref.getBoolean("tutorial_done", false)
        if (!isTutorialDone) {
            startTutorial()
        }
    }

    private fun startTutorial() {
        val tutorialRoot = findViewById<View>(R.id.tutorialRoot)
        val spotlight = findViewById<SpotlightView>(R.id.spotlightView)
        val tvTitle = findViewById<android.widget.TextView>(R.id.tvTutorialTitle)
        val tvDesc = findViewById<android.widget.TextView>(R.id.tvTutorialDesc)
        val btnNext = findViewById<android.widget.Button>(R.id.btnNextTutorial)
        
        tutorialRoot.visibility = View.VISIBLE
        
        var step = 0
        val steps = listOf(
            Triple(findViewById<View>(R.id.cardBalance), "Dashboard Saldo", "Ini adalah ringkasan keuangan Anda. Semua total uang kas masuk dan keluar akan terakumulasi di sini."),
            Triple(findViewById<View>(R.id.cardGrafik), "Statistik Kas", "Pantau naik-turunnya saldo kas Anda melalui grafik interaktif ini secara real-time."),
            Triple(findViewById<View>(R.id.btnNavPemasukan), "Input Pemasukan", "Klik di sini untuk mencatat iuran yang masuk. Pastikan nominal sudah benar!"),
            Triple(findViewById<View>(R.id.btnNavPengeluaran), "Input Pengeluaran", "Gunakan ini untuk mencatat pengeluaran kelas. Transparan dan akurat."),
            Triple(findViewById<View>(R.id.rvAnggotaBeranda), "Daftar Anggota", "Daftar siswa di kelas Anda. Klik pada nama siswa untuk melihat riwayat pembayaran pribadi mereka."),
            Triple(findViewById<View>(R.id.navSettings), "Menu Pengaturan", "Di sini Anda dapat mengatur nominal kas mingguan atau melakukan reset data jika diperlukan di masa mendatang.")
        )
        
        fun showStep(i: Int) {
            val current = steps[i]
            val targetView = current.first
            
            // Beri tinggi ekstra untuk RecyclerView agar highlight LEBIH BESAR
            if (current.second == "Daftar Anggota") {
                targetView.minimumHeight = 1000
            }

            val centerY = spotlight.setTarget(targetView)
            
            val card = findViewById<androidx.cardview.widget.CardView>(R.id.cardTutorialInfo)
            val params = card.layoutParams as android.widget.FrameLayout.LayoutParams
            
            // Khusus langkah pertama (Saldo), paksa kartu di bawah
            if (i == 0) {
                params.gravity = android.view.Gravity.BOTTOM
                card.translationY = -80f
            } else if (centerY > spotlight.height * 0.45) {
                params.gravity = android.view.Gravity.TOP
                card.translationY = 80f
            } else {
                params.gravity = android.view.Gravity.BOTTOM
                card.translationY = -80f
            }
            card.layoutParams = params
            
            tvTitle.text = current.second
            tvDesc.text = current.third
            if (i == steps.size - 1) btnNext.text = "Selesai" else btnNext.text = "Lanjut"
        }
        
        showStep(0)
        
        btnNext.setOnClickListener {
            step++
            if (step < steps.size) {
                showStep(step)
            } else {
                finishTutorial()
            }
        }
        
        findViewById<android.view.View>(R.id.btnSkipTutorial).setOnClickListener {
            finishTutorial()
        }
    }

    private fun finishTutorial() {
        val tutorialRoot = findViewById<android.view.View>(R.id.tutorialRoot)
        tutorialRoot.visibility = android.view.View.GONE
        getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE)
            .edit().putBoolean("tutorial_done", true).apply()
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
        val chart = findViewById<BarChart>(R.id.barChart) ?: return

        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val days = mutableListOf<String>()
        val entriesMasuk = mutableListOf<BarEntry>()
        val entriesKeluar = mutableListOf<BarEntry>()

        val allTransaksi = db.getAllTransaksi()

        // Ambil data 7 hari terakhir (Kronologis: Masa Lalu -> Hari Ini)
        for (i in 6 downTo 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(calendar.time)
            
            // Label sumbu X
            val label = if (i == 0) "Hari Ini" else java.text.SimpleDateFormat("dd/MM", Locale.getDefault()).format(calendar.time)
            days.add(label)

            val totalMasuk = allTransaksi.filter { it.tanggal == dateStr && it.tipe == "MASUK" }
                .sumOf { it.jumlah.replace(".", "").replace(",", "").toLongOrNull() ?: 0L }.toFloat()
            
            val totalKeluar = allTransaksi.filter { it.tanggal == dateStr && it.tipe == "KELUAR" }
                .sumOf { it.jumlah.replace(".", "").replace(",", "").toLongOrNull() ?: 0L }.toFloat()

            // Index 0 = 6 hari lalu, Index 6 = Hari Ini
            val xPos = (6 - i).toFloat()
            entriesMasuk.add(BarEntry(xPos, totalMasuk))
            entriesKeluar.add(BarEntry(xPos, totalKeluar))
        }

        val dataSetMasuk = BarDataSet(entriesMasuk, "Masuk").apply {
            color = Color.parseColor("#16A34A")
            setDrawValues(false)
        }
        val dataSetKeluar = BarDataSet(entriesKeluar, "Keluar").apply {
            color = Color.parseColor("#DC2626")
            setDrawValues(false)
        }

        val barData = BarData(dataSetMasuk, dataSetKeluar)
        val groupSpace = 0.35f
        val barSpace = 0.05f
        val barWidth = 0.275f 

        barData.barWidth = barWidth
        
        chart.apply {
            data = barData
            groupBars(0f, groupSpace, barSpace)
            
            description.isEnabled = false
            legend.isEnabled = true
            legend.textColor = Color.parseColor("#64748B")
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
            
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(days)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setCenterAxisLabels(true)
                axisMinimum = 0f
                axisMaximum = days.size.toFloat()
                setDrawGridLines(false)
                textColor = Color.parseColor("#64748B")
                axisLineColor = Color.TRANSPARENT
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#10000000")
                textColor = Color.parseColor("#64748B")
                axisLineColor = Color.TRANSPARENT
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value >= 1000 || value <= -1000) "${(value / 1000).toInt()}k" else value.toInt().toString()
                    }
                }
            }

            axisRight.isEnabled = false
            setTouchEnabled(true)
            animateY(1000)
            invalidate()
        }
    }
}