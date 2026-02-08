package com.example.kaskelasapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button

class AnggotaActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var rvDaftarAnggota: RecyclerView
    private lateinit var adapter: AnggotaAdapter
    private var daftarAnggotaFull = listOf<Anggota>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anggota)

        db = DatabaseHelper(this)
        rvDaftarAnggota = findViewById(R.id.rvDaftarAnggota)
        rvDaftarAnggota.layoutManager = LinearLayoutManager(this)

        loadDataAnggota()

        findViewById<Button>(R.id.btnTambahAnggotaBaru).setOnClickListener {
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

        // Back button
        findViewById<ImageView>(R.id.btnBackAnggota).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadDataAnggota()
    }

    private fun hideKeyboard(view: EditText) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun loadDataAnggota() {
        daftarAnggotaFull = db.getAllAnggota()
        adapter = AnggotaAdapter(daftarAnggotaFull, R.layout.item_anggota_edit) { anggota: Anggota ->
            val intent = Intent(this, EditAnggotaActivity::class.java)
            intent.putExtra("ANGGOTA_ID", anggota.id)
            intent.putExtra("ANGGOTA_NAMA", anggota.nama)
            intent.putExtra("ANGGOTA_NIS", anggota.nis)
            startActivity(intent)
        }
        rvDaftarAnggota.adapter = adapter
    }

    private fun filterAnggota(query: String) {
        val filtered = if (query.isEmpty()) {
            daftarAnggotaFull
        } else {
            daftarAnggotaFull.filter {
                it.nama.contains(query, ignoreCase = true) || 
                it.id.contains(query, ignoreCase = true)
            }
        }
        adapter = AnggotaAdapter(filtered, R.layout.item_anggota_edit) { anggota: Anggota ->
            val intent = Intent(this, EditAnggotaActivity::class.java)
            intent.putExtra("ANGGOTA_ID", anggota.id)
            intent.putExtra("ANGGOTA_NAMA", anggota.nama)
            intent.putExtra("ANGGOTA_NIS", anggota.nis)
            startActivity(intent)
        }
        rvDaftarAnggota.adapter = adapter
    }
}