package com.example.kaskelasapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kaskelasapp.viewmodel.KasViewModel
import com.example.kaskelasapp.viewmodel.KasViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RiwayatActivity : AppCompatActivity() {
    private lateinit var viewModel: KasViewModel
    private lateinit var rvRiwayat: RecyclerView
    private lateinit var adapter: RiwayatAdapter
    private var fullList: List<Transaksi> = listOf()
    private var filteredList: List<Transaksi> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        viewModel = ViewModelProvider(
            this,
            KasViewModelFactory(application)
        )[KasViewModel::class.java]

        rvRiwayat = findViewById(R.id.rvRiwayat)
        rvRiwayat.layoutManager = LinearLayoutManager(this)

        adapter = RiwayatAdapter(emptyList()) { transaksi ->
            val intent = if (transaksi.tipe == "MASUK") {
                Intent(this, DetailPemasukanActivity::class.java)
            } else {
                Intent(this, DetailPengeluaranActivity::class.java)
            }
            intent.putExtra("TRANSAKSI_ID", transaksi.id)
            startActivity(intent)
        }
        rvRiwayat.adapter = adapter

        observeViewModel()

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

        // --- FITUR FILTER TANGGAL ---
        findViewById<ImageButton>(R.id.btnFilterTanggal).setOnClickListener {
            showDateRangePicker()
        }

        // --- TOMBOL RESET FILTER ---
        findViewById<ImageButton>(R.id.btnResetFilter)?.setOnClickListener {
            resetFilter()
        }

        setupBottomNav()
        BackgroundHelper.applyAnimatedBackground(this)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.transaksiList.collect { list ->
                fullList = list
                updateTotals()

                val etSearch = findViewById<EditText>(R.id.etSearchRiwayat)
                filterData(etSearch?.text.toString())

                // FIX #14: Tampilkan empty state jika tidak ada transaksi
                val tvEmpty = findViewById<TextView>(R.id.tvEmptyStateText)
                val emptyContainer = findViewById<View>(R.id.emptyStateContainerRiwayat)
                tvEmpty?.text = "Belum ada transaksi"
                emptyContainer?.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                rvRiwayat.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private var filterStartDate: Long? = null
    private var filterEndDate: Long? = null

    private fun filterData(query: String) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        filteredList = fullList.filter {
            val matchesSearch = it.nama.contains(query, ignoreCase = true) ||
                                it.tanggal.contains(query, ignoreCase = true) ||
                                it.keterangan.contains(query, ignoreCase = true)

            var matchesDate = true
            if (filterStartDate != null && filterEndDate != null) {
                try {
                    val date = sdf.parse(it.tanggal)
                    if (date != null) {
                        matchesDate = date.time in filterStartDate!!..filterEndDate!!
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            matchesSearch && matchesDate
        }
        adapter.updateData(filteredList)
        updateTotalsFiltered(filteredList)
    }

    private fun showDateRangePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("Pilih Rentang Tanggal")
        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { selection ->
            filterStartDate = selection.first
            filterEndDate = selection.second

            // Tampilkan tombol reset filter
            updateResetFilterVisibility()

            val etSearch = findViewById<EditText>(R.id.etSearchRiwayat)
            filterData(etSearch?.text.toString())
        }

        picker.show(supportFragmentManager, picker.toString())
    }

    private fun resetFilter() {
        filterStartDate = null
        filterEndDate = null
        updateResetFilterVisibility()
        val etSearch = findViewById<EditText>(R.id.etSearchRiwayat)
        filterData(etSearch?.text.toString())
        Toast.makeText(this, "Filter tanggal dihapus", Toast.LENGTH_SHORT).show()
    }

    private fun updateResetFilterVisibility() {
        val btnReset = findViewById<View>(R.id.btnResetFilter)
        btnReset?.visibility = if (filterStartDate != null && filterEndDate != null) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun exportToCSV() {
        // Ekspor daftar yang sedang ditampilkan (sudah difilter), bukan fullList
        val exportList = filteredList.ifEmpty { fullList }

        if (exportList.isEmpty()) {
            Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val fileName = "Laporan_Kas_${System.currentTimeMillis()}.csv"
            val file = File(cacheDir, fileName)
            val out = FileOutputStream(file)

            var totalMasuk = 0L
            var totalKeluar = 0L

            val filterInfo = if (filterStartDate != null && filterEndDate != null) {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                "Filter: ${sdf.format(Date(filterStartDate!!))} - ${sdf.format(Date(filterEndDate!!))}\n"
            } else ""

            // FIX #9: Tulis BOM UTF-8 agar Excel Windows tidak salah encoding
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            val title = "LAPORAN KAS KELAS\n"
            val timestamp = "Diekspor pada: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n"
            val header = "No,Tanggal,Nama/Keterangan,Pemasukan (Rp),Pengeluaran (Rp),Total (Rp)\n"

            out.write(title.toByteArray(Charsets.UTF_8))
            out.write(timestamp.toByteArray(Charsets.UTF_8))
            if (filterInfo.isNotEmpty()) out.write(filterInfo.toByteArray(Charsets.UTF_8))
            out.write("\n".toByteArray(Charsets.UTF_8))
            out.write(header.toByteArray(Charsets.UTF_8))

            exportList.reversed().forEachIndexed { index, it ->
                val jumlahVal = it.jumlah.replace(".", "").replace(",", "").toLongOrNull() ?: 0
                val masuk = if (it.tipe == "MASUK") {
                    totalMasuk += jumlahVal
                    jumlahVal.toString()
                } else ""
                val keluar = if (it.tipe == "KELUAR") {
                    totalKeluar += jumlahVal
                    jumlahVal.toString()
                } else ""

                val line = "${index + 1},\"${it.tanggal}\",\"${it.nama} - ${it.keterangan}\",$masuk,$keluar,${totalMasuk - totalKeluar}\n"
                out.write(line.toByteArray(Charsets.UTF_8))
            }

            val summary = "\n" +
                    ",,TOTAL PEMASUKAN,Rp ${formatRupiah(totalMasuk)},,\n" +
                    ",,TOTAL PENGELUARAN,Rp ${formatRupiah(totalKeluar)},,\n" +
                    ",,SALDO AKHIR,,Rp ${formatRupiah(totalMasuk - totalKeluar)},\n"
            out.write(summary.toByteArray(Charsets.UTF_8))

            out.close()

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
        viewModel.loadAllTransaksi()
    }

    private fun formatRupiah(number: Long): String {
        val localeID = java.util.Locale.Builder().setLanguage("id").setRegion("ID").build()
        return java.text.NumberFormat.getNumberInstance(localeID).format(number)
    }

    private fun updateTotalsFiltered(list: List<Transaksi>) {
        var totalMasuk = 0L
        var totalKeluar = 0L

        list.forEach {
            val jumlah = it.jumlah.replace(".", "").replace(",", "").toLongOrNull() ?: 0
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

    private fun updateTotals() {
        updateTotalsFiltered(fullList)
    }
}
