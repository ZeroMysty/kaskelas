package com.example.kaskelasapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*

class ChartDetailActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private val filterOptions = arrayOf("Semua", "Bulan Ini", "7 Hari")

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
        val chart = findViewById<LineChart>(R.id.fullLineChart)
        val allTransaksi = db.getAllTransaksi()

        if (allTransaksi.isEmpty()) {
            chart.clear()
            return
        }

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val now = Calendar.getInstance()

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
                "7 Hari" -> {
                    val cal = Calendar.getInstance()
                    try {
                        cal.time = sdf.parse(trx.tanggal) ?: Date()
                        val diff = now.timeInMillis - cal.timeInMillis
                        diff <= 7 * 24 * 60 * 60 * 1000L && diff >= 0
                    } catch (e: Exception) { false }
                }
                else -> true
            }
        }

        val sorted = filteredTransaksi.sortedWith(compareBy({
            try { sdf.parse(it.tanggal) } catch (e: Exception) { Date(0) }
        }, { it.id }))

        val entries = mutableListOf<Entry>()
        var saldo = 0f
        entries.add(Entry(0f, 0f))

        sorted.forEachIndexed { index, trx ->
            val cleanJumlah = trx.jumlah.replace(".", "").replace(",", "")
            val jumlah = cleanJumlah.toFloatOrNull() ?: 0f
            if (trx.tipe == "MASUK") saldo += jumlah else saldo -= jumlah
            entries.add(Entry((index + 1).toFloat(), saldo))
        }

        val lineDataSet = LineDataSet(entries, "Saldo").apply {
            color = Color.parseColor("#2563EB")
            lineWidth = 3.5f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f // Bikin lekukan lebih smooth
            setDrawCircles(true)
            setCircleColor(Color.parseColor("#2563EB"))
            circleRadius = 4f
            setDrawCircleHole(true)
            circleHoleColor = Color.WHITE
            setDrawValues(false)
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(this@ChartDetailActivity, R.drawable.chart_gradient)
        }

        chart.apply {
            data = LineData(lineDataSet)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false

            // Tambahkan Extra Offsets (Kiri, Atas, Kanan, Bawah)
            // Sebagai pengganti padding CardView yang dibuang
            setExtraOffsets(24f, 32f, 24f, 16f)

            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.parseColor("#64748B")
                axisLineColor = Color.TRANSPARENT
                granularity = 1f
                setAvoidFirstLastClipping(true)
                spaceMin = 0.15f
                spaceMax = 0.15f
            }

            axisLeft.apply {
                // Biarkan grid agar tetap mudah dibaca
                setDrawGridLines(true)
                gridColor = Color.parseColor("#12000000")
                textColor = Color.parseColor("#64748B")
                axisLineColor = Color.TRANSPARENT
                setLabelCount(6, false)

                axisMinimum = 0f
                spaceTop = 35f

                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when {
                            value >= 1000000 -> "${(value / 1000000).toInt()}M"
                            value >= 1000 -> "${(value / 1000).toInt()}k"
                            else -> value.toInt().toString()
                        }
                    }
                }
            }

            animateX(800)
            invalidate()
        }
    }
}