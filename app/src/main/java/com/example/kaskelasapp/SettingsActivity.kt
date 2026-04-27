package com.example.kaskelasapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val etNominalKas = findViewById<EditText>(R.id.etNominalKas)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanSettings)
        val btnHapusData = findViewById<Button>(R.id.btnHapusData)
        val rgNominal = findViewById<RadioGroup>(R.id.rgNominalOptions)
        val layoutCustom = findViewById<View>(R.id.layoutCustomNominal)

        val sharedPref = getSharedPreferences("SettingsKas", Context.MODE_PRIVATE)
        val currentNominal = sharedPref.getString("nominal_kas", "2000") ?: "2000"
        
        etNominalKas.addTextChangedListener(CurrencyTextWatcher(etNominalKas))

        // Set Initial State Berdasarkan Data Tersimpan
        when (currentNominal) {
            "2000" -> {
                findViewById<RadioButton>(R.id.rb2k).isChecked = true
                layoutCustom.visibility = View.GONE
            }
            "5000" -> {
                findViewById<RadioButton>(R.id.rb5k).isChecked = true
                layoutCustom.visibility = View.GONE
            }
            else -> {
                findViewById<RadioButton>(R.id.rbCustom).isChecked = true
                layoutCustom.visibility = View.VISIBLE
                etNominalKas.setText(currentNominal)
            }
        }

        // Listener RadioGroup (Tampilkan/Sembunyikan Input Custom)
        rgNominal.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbCustom) {
                layoutCustom.visibility = View.VISIBLE
            } else {
                layoutCustom.visibility = View.GONE
            }
        }

        btnSimpan.setOnClickListener {
            val selectedId = rgNominal.checkedRadioButtonId
            var finalNominal = ""

            when (selectedId) {
                R.id.rb2k -> finalNominal = "2000"
                R.id.rb5k -> finalNominal = "5000"
                R.id.rbCustom -> {
                    finalNominal = etNominalKas.text.toString().replace(".", "")
                }
            }

            if (finalNominal.isNotEmpty()) {
                with(sharedPref.edit()) {
                    putString("nominal_kas", finalNominal)
                    apply()
                }
                Toast.makeText(this, "Pengaturan berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Nominal tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        btnHapusData.setOnClickListener {
            showFirstWarning()
        }

        BottomNavHelper.setupBottomNav(this)
    }

    private fun showFirstWarning() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Hapus Seluruh Data?")
            .setMessage("Tindakan ini akan menghapus semua riwayat transaksi dan daftar anggota secara permanen.")
            .setPositiveButton("Lanjut") { _, _ ->
                showSecondWarningWithTimer()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showSecondWarningWithTimer() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("PERINGATAN TERAKHIR!")
        builder.setMessage("Data yang dihapus TIDAK DAPAT dikembalikan. Silakan tunggu 5 detik untuk mengonfirmasi.")
        builder.setCancelable(false)
        
        builder.setPositiveButton("Hapus Permanen", null) 
        builder.setNegativeButton("Batal", null)
        
        val dialog = builder.create()
        dialog.show()
        
        val btnDelete = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
        btnDelete.isEnabled = false
        
        val timer = object : android.os.CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000) + 1
                btnDelete.text = "Hapus Permanen (${seconds}s)"
            }
            
            override fun onFinish() {
                btnDelete.isEnabled = true
                btnDelete.text = "Hapus Permanen"
                btnDelete.setOnClickListener {
                    val db = DatabaseHelper(this@SettingsActivity)
                    db.resetDatabase()
                    Toast.makeText(this@SettingsActivity, "Seluruh data telah dihapus", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                    
                    // Kembali ke MainActivity (Beranda)
                    val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }
        }
        timer.start()
    }
}
