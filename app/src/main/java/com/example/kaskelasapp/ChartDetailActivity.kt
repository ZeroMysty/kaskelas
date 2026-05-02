package com.example.kaskelasapp

import androidx.core.content.ContextCompat
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.core.graphics.toColorInt
import java.text.SimpleDateFormat
import java.util.*

class ChartDetailActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private val filterOptions = arrayOf("Semua", "Bulan Ini", "7 Hari Terakhir")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_detail)

        db = DatabaseHelper(this)

        findViewById<ImageButton>(R.id.btnBackChart).setOnClickListener {
            supportFinishAfterTransition()
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
        val chart = findViewById<LineChart>(R.id.fullLineChart)
        val allTransaksi = db.getAllTransaksi()

        if (allTransaksi.isEmpty()) {
            chart.clear()
            return
        }

        val mainColor = "#2196F3".toColorInt()
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val now = Calendar.getInstance()

        // 🔥 FILTER DATA
        val filteredTransaksi = allTransaksi.filter { trx ->
            when (filter) {
                "Bulan Ini" -> {
                    val cal = Calendar.getInstance()
                    try {
                        cal.time = sdf.parse(trx.tanggal) ?: Date()
                        cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                                cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    } catch (e: Exception) { false }
                }
                "7 Hari Terakhir" -> {
                    val cal = Calendar.getInstance()
                    try {
                        cal.time = sdf.parse(trx.tanggal) ?: Date()
                        val diff = now.timeInMillis - cal.timeInMillis
                        diff <= 7 * 24 * 60 * 60 * 1000L && diff >= 0
                    } catch (e: Exception) { false }
                }
                else -> true // Semua
            }
        }

        // 🔥 SORTING (Tetap stabil ASC)
        val sorted = filteredTransaksi.sortedWith(compareBy({
            try { sdf.parse(it.tanggal) } catch (e: Exception) { java.util.Date(0) }
        }, { it.id }))

        val entries = mutableListOf<Entry>()
        var saldo = 0f

        // Titik awal 0
        entries.add(Entry(0f, 0f))

        sorted.forEachIndexed { index, trx ->
            val cleanJumlah = trx.jumlah.replace(".", "").replace(",", "")
            val jumlah = cleanJumlah.toFloatOrNull() ?: 0f

            if (trx.tipe == "MASUK") {
                saldo += jumlah
            } else {
                saldo -= jumlah
            }
            entries.add(Entry((index + 1).toFloat(), saldo))
        }

        if (entries.size == 1) {
            entries.add(Entry(1f, 0f))
        }

        val lineDataSet = LineDataSet(entries, "Saldo").apply {
            color = mainColor
            lineWidth = 3f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.15f
            setDrawCircles(true)
            setCircleColor(mainColor)
            circleRadius = 5f
            setDrawCircleHole(true)
            circleHoleRadius = 2.5f
            circleHoleColor = android.graphics.Color.WHITE
            setDrawValues(false)
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(this@ChartDetailActivity, R.drawable.chart_gradient)
            highLightColor = mainColor
        }

        chart.apply {
            data = LineData(lineDataSet)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            xAxis.apply {
                setDrawGridLines(false)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawAxisLine(true)
                axisMinimum = 0f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = "#E0E0E0".toColorInt()
                setDrawAxisLine(false)
                spaceTop = 20f
                spaceBottom = 20f
            }
            setTouchEnabled(true)
            animateX(1000)
            invalidate()
        }
    }
}