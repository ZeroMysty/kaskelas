package com.example.kaskelasapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
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

        // Hide keyboard when clicking outside EditText/keyboard area
        window.decorView.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }

        // Navigasi
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
            // Navigate to pembayaran anggota
            val intent = Intent(this, TambahPemasukanActivity::class.java)
            intent.putExtra("ANGGOTA_ID", anggota.id)
            intent.putExtra("ANGGOTA_NAMA", anggota.nama)
            startActivity(intent)
        }
        rvAnggotaBeranda.adapter = adapter
    }

    private fun updateSaldo() {
        val total = db.hitungTotalSaldo()
        findViewById<android.widget.TextView>(R.id.tvTotalUangkas).text = "Rp. $total"
    }

    private fun loadChart() {
        val chart = findViewById<LineChart>(R.id.ivGrafikDummy)
        if (chart == null) return

        // Ambil data transaksi
        val transaksi = db.getAllTransaksi()
        
        // Group transaksi by date
        val masukanByDate = mutableMapOf<String, Long>()
        val keluarByDate = mutableMapOf<String, Long>()
        
        transaksi.forEach {
            val jumlah = it.jumlah.toLongOrNull() ?: 0
            val date = it.tanggal // Format: "dd/MM/yyyy"
            
            if (it.tipe == "MASUK") {
                masukanByDate[date] = (masukanByDate[date] ?: 0) + jumlah
            } else {
                keluarByDate[date] = (keluarByDate[date] ?: 0) + jumlah
            }
        }
        
        // Get unique dates sorted
        val allDates = (masukanByDate.keys + keluarByDate.keys).distinct().sorted()
        
        // Create entries for chart
        val entriesMasuk = mutableListOf<Entry>()
        val entriesKeluar = mutableListOf<Entry>()
        
        allDates.forEachIndexed { index, date ->
            entriesMasuk.add(Entry(index.toFloat(), (masukanByDate[date] ?: 0L).toFloat()))
            entriesKeluar.add(Entry(index.toFloat(), (keluarByDate[date] ?: 0L).toFloat()))
        }
        
        // If no data, show empty chart
        if (entriesMasuk.isEmpty()) {
            entriesMasuk.add(Entry(0f, 0f))
            entriesKeluar.add(Entry(0f, 0f))
        }
        
        val dataSetMasuk = LineDataSet(entriesMasuk, "Pemasukan").apply {
            color = android.graphics.Color.GREEN
            setCircleColor(android.graphics.Color.GREEN)
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(false)
            setDrawValues(false)
        }
        
        val dataSetKeluar = LineDataSet(entriesKeluar, "Pengeluaran").apply {
            color = android.graphics.Color.RED
            setCircleColor(android.graphics.Color.RED)
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(false)
            setDrawValues(false)
        }
        
        val lineData = LineData(dataSetMasuk, dataSetKeluar)
        chart.data = lineData
        chart.xAxis.labelCount = allDates.size
        chart.invalidate()
    }
}
