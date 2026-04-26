package com.example.kaskelasapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)



        val etNominalKas = findViewById<EditText>(R.id.etNominalKas)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanSettings)

        val sharedPref = getSharedPreferences("SettingsKas", Context.MODE_PRIVATE)
        val currentNominal = sharedPref.getString("nominal_kas", "2000")
        
        etNominalKas.addTextChangedListener(CurrencyTextWatcher(etNominalKas))
        etNominalKas.setText(currentNominal)

        btnSimpan.setOnClickListener {
            val nominal = etNominalKas.text.toString().replace(".", "")
            if (nominal.isNotEmpty()) {
                with(sharedPref.edit()) {
                    putString("nominal_kas", nominal)
                    apply()
                }
                Toast.makeText(this, "Pengaturan berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Nominal tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        BottomNavHelper.setupBottomNav(this)
    }
}
