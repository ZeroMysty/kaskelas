package com.example.kaskelasapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class ChartDetailActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private val filterOptions = arrayOf("7 Hari Terakhir", "Bulan Ini", "Tahun Ini", "Semua Data")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_detail)

        BackgroundHelper.applyAnimatedBackground(this)
        db = DatabaseHelper(this)

        findViewById<ImageView>(R.id.btnBackChart).setOnClickListener {
            finish()
        }

        setupSpinner()
    }

    private fun setupSpinner() {
        val spinner = findViewById<Spinner>(R.id.spinnerFilterWaktu)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setupFullChart(filterOptions[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFullChart(filter: String) {
        val chart = findViewById<BarChart>(R.id.fullBarChart)
        val allTransaksi = db.getAllTransaksi()

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val days = mutableListOf<String>()
        val entriesMasuk = mutableListOf<BarEntry>()
        val entriesKeluar = mutableListOf<BarEntry>()

        // Tentukan jumlah hari berdasarkan filter
        val range = when (filter) {
            "7 Hari Terakhir" -> 6
            "Bulan Ini" -> Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1
            "Tahun Ini" -> Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - 1
            else -> 29 // Default Semua tampilkan 30 hari terakhir
        }

        // Kronologis: Masa Lalu -> Hari Ini
        for (i in range downTo 0) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(calendar.time)
            
            val label = if (i == 0) "Hari Ini" else SimpleDateFormat("dd/MM", Locale.getDefault()).format(calendar.time)
            days.add(label)

            val totalMasuk = allTransaksi.filter { it.tanggal == dateStr && it.tipe == "MASUK" }
                .sumOf { it.jumlah.replace(".", "").replace(",", "").toLongOrNull() ?: 0L }.toFloat()
            
            val totalKeluar = allTransaksi.filter { it.tanggal == dateStr && it.tipe == "KELUAR" }
                .sumOf { it.jumlah.replace(".", "").replace(",", "").toLongOrNull() ?: 0L }.toFloat()

            val xPos = (range - i).toFloat()
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
            
            setExtraOffsets(10f, 20f, 10f, 10f)

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