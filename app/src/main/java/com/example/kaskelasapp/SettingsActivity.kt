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

        val etNominalCustom = findViewById<EditText>(R.id.etNominalCustom)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanSettings)
        val btnResetData = findViewById<Button>(R.id.resetData)
        
        val btn2k = findViewById<Button>(R.id.btnNominal2)
        val btn5k = findViewById<Button>(R.id.btnNominal5)
        val btnLainnya = findViewById<Button>(R.id.btnNominalLainnya)

        val sharedPref = getSharedPreferences("SettingsKas", Context.MODE_PRIVATE)
        val currentNominal = sharedPref.getString("nominal_kas", "2000") ?: "2000"
        
        etNominalCustom.addTextChangedListener(CurrencyTextWatcher(etNominalCustom))

        // UI State Logic
        fun updateButtonStyles(selectedNominal: String) {
            // Reset styles (using glassy backgrounds)
            btn2k.alpha = 0.6f
            btn5k.alpha = 0.6f
            btnLainnya.alpha = 0.6f
            
            when (selectedNominal) {
                "2000" -> btn2k.alpha = 1.0f
                "5000" -> btn5k.alpha = 1.0f
                else -> btnLainnya.alpha = 1.0f
            }
        }

        updateButtonStyles(currentNominal)
        if (currentNominal != "2000" && currentNominal != "5000") {
            etNominalCustom.setText(currentNominal)
            etNominalCustom.visibility = View.VISIBLE
        } else {
            etNominalCustom.visibility = View.GONE
        }

        btn2k.setOnClickListener {
            updateButtonStyles("2000")
            etNominalCustom.visibility = View.GONE
            saveNominal("2000")
        }

        btn5k.setOnClickListener {
            updateButtonStyles("5000")
            etNominalCustom.visibility = View.GONE
            saveNominal("5000")
        }

        btnLainnya.setOnClickListener {
            updateButtonStyles("lainnya")
            etNominalCustom.visibility = View.VISIBLE
            etNominalCustom.requestFocus()
        }

        btnSimpan.setOnClickListener {
            val nominalText = etNominalCustom.text.toString().replace(".", "")
            if (etNominalCustom.visibility == View.VISIBLE) {
                if (nominalText.isNotEmpty()) {
                    saveNominal(nominalText)
                    Toast.makeText(this, "Pengaturan disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Masukkan nominal custom", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Pengaturan disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnResetData.setOnClickListener {
            showFirstWarning()
        }

        BottomNavHelper.setupBottomNav(this)
        BackgroundHelper.applyAnimatedBackground(this)
    }

    private fun saveNominal(nominal: String) {
        val sharedPref = getSharedPreferences("SettingsKas", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("nominal_kas", nominal)
            apply()
        }
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
