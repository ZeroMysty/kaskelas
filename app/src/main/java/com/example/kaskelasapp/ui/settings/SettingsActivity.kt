package com.example.kaskelasapp.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.kaskelasapp.data.DatabaseHelper
import com.example.kaskelasapp.utils.BackgroundHelper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.kaskelasapp.R
import com.example.kaskelasapp.ui.main.MainActivity
import com.example.kaskelasapp.utils.NavigationHelper

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val etNominalDefault = findViewById<EditText>(R.id.etNominalDefault)
        val switchNavStyle = findViewById<SwitchCompat>(R.id.switchNavStyle)
        val btnResetData = findViewById<Button>(R.id.btnResetData)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        
        val sharedPrefKas = getSharedPreferences("SettingsKas", Context.MODE_PRIVATE)
        val sharedPrefApp = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        
        val currentNominal = sharedPrefKas.getString("nominal_kas", "2000") ?: "2000"
        val useSidebar = sharedPrefApp.getBoolean("use_sidebar_nav", false)
        
        etNominalDefault.setText(currentNominal)
        switchNavStyle.isChecked = useSidebar

        switchNavStyle.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefApp.edit().putBoolean("use_sidebar_nav", isChecked).apply()
            // Refresh navigation UI
            NavigationHelper.setupNavigation(this)
            Toast.makeText(this, "Gaya navigasi diubah ke ${if (isChecked) "Sidebar" else "Bottom Nav"}", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnExportReport).setOnClickListener {
            Toast.makeText(this, "Fitur Ekspor PDF akan segera hadir!", Toast.LENGTH_SHORT).show()
        }

        btnResetData.setOnClickListener {
            showResetWarning()
        }

        btnLogout.setOnClickListener {
            finishAffinity()
            // startActivity(Intent(this, LoginActivity::class.java))
        }

        NavigationHelper.setupNavigation(this)
        BackgroundHelper.applyAnimatedBackground(this)
    }

    private fun showResetWarning() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Hapus Seluruh Data?")
            .setMessage("Tindakan ini akan menghapus semua riwayat transaksi dan daftar anggota secara permanen.")
            .setPositiveButton("Hapus") { _, _ ->
                val db = DatabaseHelper(this)
                db.resetDatabase()
                Toast.makeText(this, "Seluruh data telah dihapus", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}

