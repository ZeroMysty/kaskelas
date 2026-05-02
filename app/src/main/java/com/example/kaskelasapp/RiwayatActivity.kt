package com.example.kaskelasapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream

class RiwayatActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var rvRiwayat: RecyclerView
    private lateinit var adapter: RiwayatAdapter
    private var fullList: List<Transaksi> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        db = DatabaseHelper(this)
        rvRiwayat = findViewById(R.id.rvRiwayat)
        rvRiwayat.layoutManager = LinearLayoutManager(this)

        loadTransaksi()
        updateTotals()

        // --- FITUR PENCARIAN ---
        val etSearch = findViewById<EditText>(R.id.etSearchRiwayat)
        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // --- FITUR EKSPOR ---
        findViewById<ImageButton>(R.id.btnExportRiwayat).setOnClickListener {
            exportToCSV()
        }

        setupBottomNav()
        BackgroundHelper.applyAnimatedBackground(this)
    }

    private fun filterData(query: String) {
        val filtered = fullList.filter {
            it.nama.contains(query, ignoreCase = true) || 
            it.tanggal.contains(query, ignoreCase = true) ||
            it.keterangan.contains(query, ignoreCase = true)
        }
        adapter.updateData(filtered)
    }

    private fun exportToCSV() {
        if (fullList.isEmpty()) {
            Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val fileName = "Laporan_Kas_${System.currentTimeMillis()}.csv"
            val file = File(cacheDir, fileName)
            val out = FileOutputStream(file)
            
            // Perhitungan Total untuk Ringkasan
            var totalMasuk = 0L
            var totalKeluar = 0L

            // Header & Judul Laporan
            val title = "LAPORAN KAS KELAS\n"
            val timestamp = "Diekspor pada: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n\n"
            val header = "No,Tanggal,Nama/Keterangan,Pemasukan (Rp),Pengeluaran (Rp),Total (Rp)\n"
            
            out.write(title.toByteArray())
            out.write(timestamp.toByteArray())
            out.write(header.toByteArray())

            // Isi Data
            fullList.reversed().forEachIndexed { index, it ->
                val jumlahVal = it.jumlah.replace(".", "").toLongOrNull() ?: 0
                val masuk = if (it.tipe == "MASUK") {
                    totalMasuk += jumlahVal
                    jumlahVal.toString()
                } else ""
                val keluar = if (it.tipe == "KELUAR") {
                    totalKeluar += jumlahVal
                    jumlahVal.toString()
                } else ""

                // Kita bungkus teks dengan tanda kutip agar aman jika ada koma
                val line = "${index + 1},\"${it.tanggal}\",\"${it.nama} - ${it.keterangan}\",${masuk},${keluar},${totalMasuk - totalKeluar}\n"
                out.write(line.toByteArray())
            }

            // Bagian Ringkasan di Bawah
            val summary = "\n" +
                    ",,TOTAL PEMASUKAN,Rp ${formatRupiah(totalMasuk)},,\n" +
                    ",,TOTAL PENGELUARAN,Rp ${formatRupiah(totalKeluar)},,\n" +
                    ",,SALDO AKHIR,,Rp ${formatRupiah(totalMasuk - totalKeluar)},\n"
            out.write(summary.toByteArray())
            
            out.close()

            // Bagikan File
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/csv"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "Bagikan Laporan Kas"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal mengekspor data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupBottomNav() {
        BottomNavHelper.setupBottomNav(this)
    }

    override fun onResume() {
        super.onResume()
        loadTransaksi()
        updateTotals()
    }

    private fun formatRupiah(number: Long): String {
        val localeID = java.util.Locale.Builder().setLanguage("id").setRegion("ID").build()
        return java.text.NumberFormat.getNumberInstance(localeID).format(number)
    }

    private fun updateTotals() {
        var totalMasuk = 0L
        var totalKeluar = 0L
        
        fullList.forEach {
            val jumlah = it.jumlah.replace(".", "").toLongOrNull() ?: 0
            if (it.tipe == "MASUK") {
                totalMasuk += jumlah
            } else {
                totalKeluar += jumlah
            }
        }
        val totalBalance = totalMasuk - totalKeluar
        
        findViewById<TextView>(R.id.tvTotalBalanceRiwayat)?.text = "Rp ${formatRupiah(totalBalance)}"
        findViewById<TextView>(R.id.tvTotalMasukRiwayat)?.text = "Rp ${formatRupiah(totalMasuk)}"
        findViewById<TextView>(R.id.tvTotalKeluarRiwayat)?.text = "Rp ${formatRupiah(totalKeluar)}"
    }

    private fun loadTransaksi() {
        fullList = db.getAllTransaksi()
        adapter = RiwayatAdapter(fullList) { transaksi ->
            val intent = if (transaksi.tipe == "MASUK") {
                Intent(this, DetailPemasukanActivity::class.java)
            } else {
                Intent(this, DetailPengeluaranActivity::class.java)
            }
            intent.putExtra("TRANSAKSI_ID", transaksi.id)
            startActivity(intent)
        }
        rvRiwayat.adapter = adapter
    }
}
