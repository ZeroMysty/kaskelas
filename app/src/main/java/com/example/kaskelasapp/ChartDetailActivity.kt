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
            finish()
        }

        setupFullChart()
    }

    private fun setupFullChart() {
        val chart = findViewById<LineChart>(R.id.fullLineChart)
        val transaksi = db.getAllTransaksi()

        if (transaksi.isEmpty()) return

        // 🔥 ambil warna SEKALI (biar efisien)
        val HEJO = ContextCompat.getColor(this, R.color.HEJO)
        val BEREUM = ContextCompat.getColor(this, R.color.BEREUM)
        val black = ContextCompat.getColor(this, R.color.black)

        val entries = mutableListOf<Entry>()
        var saldo = 0f

        // urutkan sesuai input
        val sorted = transaksi.sortedBy { it.id }

        sorted.forEachIndexed { index, trx ->
            val jumlah = trx.jumlah.toFloatOrNull() ?: 0f

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

        val dataSets = mutableListOf<ILineDataSet>()

        for (i in 1 until entries.size) {
            val prev = entries[i - 1]
            val curr = entries[i]

            val isNaik = curr.y >= prev.y
            val diff = curr.y - prev.y

            // 🔥 GARIS
            val lineDataSet = LineDataSet(listOf(prev, curr), "").apply {
                color = if (isNaik) HEJO else BEREUM
                lineWidth = 4f

                setDrawCircles(false)
                setDrawValues(false)
            }

            // 🔥 TITIK TENGAH
            val midX = (prev.x + curr.x) / 2f
            val midY = (prev.y + curr.y) / 2f
            val midEntry = Entry(midX, midY)

            // 🔥 LABEL + / -
            val labelDataSet = LineDataSet(listOf(midEntry), "").apply {
                setDrawCircles(false)
                setDrawValues(true)

                valueTextSize = 10f
                valueTextColor = if (isNaik) black else black

                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getPointLabel(entry: Entry?): String {
                        return if (diff >= 0) "+${diff.toInt()}" else "${diff.toInt()}"
                    }
                }
            }

            dataSets.add(lineDataSet)
            dataSets.add(labelDataSet)
        }

        chart.apply {
            data = LineData(dataSets)

            description.text = "Grafik Kas (Per Input)"
            legend.isEnabled = false

            axisRight.isEnabled = false
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)

            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            animateX(800)

            invalidate()
        }
    }
}