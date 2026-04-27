package com.example.kaskelasapp

import androidx.core.content.ContextCompat
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.core.graphics.toColorInt

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

        val entries = mutableListOf<Entry>()
        var saldo = 0f

        // 🔥 SORT LEBIH AMAN (opsional tapi disarankan)
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        val sorted = transaksi.sortedBy {
            try {
                sdf.parse(it.tanggal)
            } catch (e: Exception) {
                java.util.Date(0)
            }
        }

        // 🔥 TITIK AWAL DARI 0
        entries.add(Entry(0f, 0f))

        sorted.forEachIndexed { index, trx ->
            val cleanJumlah = trx.jumlah.replace(".", "").replace(",", "")
            val jumlah = cleanJumlah.toFloatOrNull() ?: 0f

            if (trx.tipe == "MASUK") {
                saldo += jumlah
            } else {
                saldo -= jumlah
            }

            // 🔥 index + 1 biar geser dari titik awal
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
            circleRadius = 6f
            setDrawCircleHole(true)
            circleHoleRadius = 3f
            circleHoleColor = android.graphics.Color.WHITE

            setDrawValues(false)

            setDrawFilled(true)
            fillDrawable =
                ContextCompat.getDrawable(this@ChartDetailActivity, R.drawable.chart_gradient)

            enableDashedHighlightLine(10f, 5f, 0f)
            highLightColor = mainColor
        }

        chart.apply {
            data = LineData(lineDataSet)

            description.text = "Grafik Kas (Per Input)"
            legend.isEnabled = false

            axisRight.isEnabled = false
            xAxis.setDrawGridLines(false)

            axisLeft.apply {
                setDrawGridLines(true)
                "#E0E0E0".toColorInt().also { gridColor = it }
                gridLineWidth = 0.5f
                setDrawAxisLine(false)
            }

            // 🔥 BIAR GAK KEPOTONG KIRI
            xAxis.axisMinimum = 0f

            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            animateX(800)

            invalidate()
        }
    }
}