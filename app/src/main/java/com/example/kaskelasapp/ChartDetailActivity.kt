package com.example.kaskelasapp

import androidx.core.content.ContextCompat
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class ChartDetailActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_detail)

        db = DatabaseHelper(this)

        findViewById<ImageButton>(R.id.btnBackChart).setOnClickListener {
            supportFinishAfterTransition()
        }

        setupFullChart()
    }

    private fun setupFullChart() {
        val chart = findViewById<LineChart>(R.id.fullLineChart)
        val transaksi = db.getAllTransaksi()

        if (transaksi.isEmpty()) return

        val mainColor = ContextCompat.getColor(this, R.color.blue_premium)
        val black = ContextCompat.getColor(this, R.color.black)

        val entries = mutableListOf<Entry>()
        var saldo = 0f

        // urutkan sesuai input
        val sorted = transaksi.sortedBy { it.id }

        sorted.forEachIndexed { index, trx ->
            // Bersihkan titik format ribuan agar angka valid
            val cleanJumlah = trx.jumlah.replace(".", "").replace(",", "")
            val jumlah = cleanJumlah.toFloatOrNull() ?: 0f

            if (trx.tipe == "MASUK") {
                saldo += jumlah
            } else {
                saldo -= jumlah
            }

            entries.add(Entry(index.toFloat(), saldo))
        }

        if (entries.size < 2) {
            entries.add(Entry(entries.size.toFloat(), saldo))
        }

        // 🔥 SATU DATASET UTAMA (Garis + Area)
        val lineDataSet = LineDataSet(entries, "Saldo").apply {
            color = mainColor
            lineWidth = 3f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.15f

            setDrawCircles(true)
            setCircleColor(mainColor)
            circleRadius = 6f
            setDrawCircleHole(true)
            circleHoleRadius = 3f
            circleHoleColor = android.graphics.Color.WHITE

            setDrawValues(false)

            // Area Fill
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(this@ChartDetailActivity, R.drawable.chart_gradient)

            // Highlighting
            enableDashedHighlightLine(10f, 5f, 0f)
            highLightColor = mainColor
        }

        val dataSets = listOf(lineDataSet)

        chart.apply {
            data = LineData(dataSets)

            description.text = "Grafik Kas (Per Input)"
            legend.isEnabled = false

            axisRight.isEnabled = false
            xAxis.setDrawGridLines(false)
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = android.graphics.Color.parseColor("#E0E0E0")
                gridLineWidth = 0.5f
                setDrawAxisLine(false)
            }

            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            animateX(800)

            invalidate()
        }
    }
}