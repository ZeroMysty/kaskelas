package com.example.kaskelasapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.example.kaskelasapp.viewmodel.KasViewModel
import com.example.kaskelasapp.viewmodel.KasViewModelFactory
import kotlinx.coroutines.launch

class AnggotaActivity : AppCompatActivity() {
    private lateinit var viewModel: KasViewModel
    private lateinit var rvDaftarAnggota: RecyclerView
    private lateinit var adapter: AnggotaAdapter
    private var daftarAnggotaFull = listOf<Anggota>()
    private lateinit var tvEmptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anggota)

        viewModel = ViewModelProvider(
            this,
            KasViewModelFactory(application)
        )[KasViewModel::class.java]

        rvDaftarAnggota = findViewById(R.id.rvDaftarAnggota)
        rvDaftarAnggota.layoutManager = LinearLayoutManager(this)
        tvEmptyState = findViewById(R.id.tvEmptyStateText)

        // Inisialisasi adapter sekali — update via updateData(), bukan buat adapter baru tiap filter
        adapter = AnggotaAdapter(emptyList(), R.layout.item_anggota_edit) { anggota: Anggota ->
            val intent = Intent(this, EditAnggotaActivity::class.java)
            intent.putExtra("ANGGOTA_ID", anggota.id)
            intent.putExtra("ANGGOTA_NAMA", anggota.nama)
            intent.putExtra("ANGGOTA_NIS", anggota.nis)
            startActivity(intent)
        }
        rvDaftarAnggota.adapter = adapter

        observeViewModel()

        findViewById<ExtendedFloatingActionButton>(R.id.btnTambahAnggotaBaru).setOnClickListener {
            startActivity(Intent(this, TambahAnggotaActivity::class.java))
        }

        // Setup search dengan IME action untuk close keyboard
        val etSearch = findViewById<EditText>(R.id.etSearchAnggota)
        etSearch.setImeActionLabel("Cari", EditorInfo.IME_ACTION_SEARCH)
        etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard(etSearch)
                return@OnEditorActionListener true
            }
            false
        })

        // Real-time search dengan TextWatcher
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    filterAnggota(s.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        BottomNavHelper.setupBottomNav(this)
        BackgroundHelper.applyAnimatedBackground(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAllAnggota()
    }

    private fun hideKeyboard(view: EditText) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.anggotaList.collect { list ->
                daftarAnggotaFull = list
                findViewById<TextView>(R.id.tvTotalAnggota).text = "${daftarAnggotaFull.size} Anggota"
                
                // Re-apply filter jika ada teks di search
                val etSearch = findViewById<EditText>(R.id.etSearchAnggota)
                filterAnggota(etSearch.text.toString())
            }
        }
    }

    private fun filterAnggota(query: String) {
        val filtered = if (query.isEmpty()) {
            daftarAnggotaFull
        } else {
            daftarAnggotaFull.filter {
                it.nama.contains(query, ignoreCase = true) ||
                // FIX #7: cari berdasarkan NIS, bukan UUID internal
                it.nis.contains(query, ignoreCase = true)
            }
        }
        adapter.updateData(filtered)

        // FIX #14: tampilkan empty state jika tidak ada data
        val emptyContainer = findViewById<View>(R.id.emptyStateContainerAnggota)
        if (filtered.isEmpty()) {
            emptyContainer.visibility = View.VISIBLE
            tvEmptyState.text = if (query.isEmpty()) "Belum ada anggota.\nTambahkan anggota baru!" else "Anggota \"$query\" tidak ditemukan."
            rvDaftarAnggota.visibility = View.GONE
        } else {
            emptyContainer.visibility = View.GONE
            rvDaftarAnggota.visibility = View.VISIBLE
        }
    }
}