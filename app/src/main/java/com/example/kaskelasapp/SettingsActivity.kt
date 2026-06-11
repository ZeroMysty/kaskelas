package com.example.kaskelasapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.kaskelasapp.data.AnggotaEntity
import com.example.kaskelasapp.data.AppDatabase
import com.example.kaskelasapp.data.TransaksiEntity
import com.example.kaskelasapp.repository.KasRepository
import com.example.kaskelasapp.viewmodel.KasViewModel
import com.example.kaskelasapp.viewmodel.KasViewModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class BackupData(
    val anggotaList: List<AnggotaEntity>,
    val transaksiList: List<TransaksiEntity>
)

class SettingsActivity : AppCompatActivity() {

    private lateinit var viewModel: KasViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val database = AppDatabase.getDatabase(this)
        val repository = KasRepository(database.anggotaDao(), database.transaksiDao())
        val factory = KasViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[KasViewModel::class.java]

        val etNominalCustom = findViewById<EditText>(R.id.etNominalCustom)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanSettings)
        val btnResetData = findViewById<Button>(R.id.resetData)
        
        val btn2k = findViewById<Button>(R.id.btnNominal2)
        val btn5k = findViewById<Button>(R.id.btnNominal5)
        val btnLainnya = findViewById<Button>(R.id.btnNominalLainnya)

        val sharedPref = getSharedPreferences("SettingsKas", Context.MODE_PRIVATE)
        val currentNominal = sharedPref.getString("nominal_kas", "2000") ?: "2000"
        val isAppLockEnabled = sharedPref.getBoolean("app_lock_enabled", false)
        
        val switchAppLock = findViewById<SwitchMaterial>(R.id.switchAppLock)
        switchAppLock.isChecked = isAppLockEnabled
        
        switchAppLock.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("app_lock_enabled", isChecked)
                apply()
            }
            if (isChecked) {
                Toast.makeText(this, "Kunci Aplikasi Diaktifkan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Kunci Aplikasi Dinonaktifkan", Toast.LENGTH_SHORT).show()
            }
        }
        
        etNominalCustom.addTextChangedListener(CurrencyTextWatcher(etNominalCustom))

        fun updateButtonStyles(selectedNominal: String) {
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

        val btnBukuPanduan = findViewById<Button>(R.id.btnBukuPanduan)
        btnBukuPanduan.setOnClickListener {
            startActivity(Intent(this, ManualBookActivity::class.java))
        }

        val backupLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
            if (uri != null) {
                performBackup(uri)
            }
        }

        val restoreLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                performRestore(uri)
            }
        }

        findViewById<Button>(R.id.btnBackupData).setOnClickListener {
            val fileName = "kas_backup_${System.currentTimeMillis()}.json"
            backupLauncher.launch(fileName)
        }

        findViewById<Button>(R.id.btnRestoreData).setOnClickListener {
            restoreLauncher.launch(arrayOf("application/json", "*/*"))
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
                    viewModel.resetDatabase {
                        Toast.makeText(this@SettingsActivity, "Seluruh data telah dihapus", Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                        
                        val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
        timer.start()
    }

    private fun performBackup(uri: Uri) {
        lifecycleScope.launch {
            try {
                val anggotaList = viewModel.anggotaList.value
                val transaksiList = viewModel.transaksiList.value
                val backupData = BackupData(anggotaList, transaksiList)
                val jsonStr = Gson().toJson(backupData)

                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(jsonStr)
                    }
                }
                Toast.makeText(this@SettingsActivity, "Backup berhasil disimpan", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@SettingsActivity, "Gagal melakukan backup", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performRestore(uri: Uri) {
        lifecycleScope.launch {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        val jsonStr = reader.readText()
                        val backupData = Gson().fromJson(jsonStr, BackupData::class.java)

                        if (backupData != null && backupData.anggotaList != null && backupData.transaksiList != null) {
                            // Wipe current db and import
                            viewModel.resetDatabase {
                                backupData.anggotaList.forEach { viewModel.insertAnggota(it) }
                                backupData.transaksiList.forEach { viewModel.insertTransaksi(it) }
                                Toast.makeText(this@SettingsActivity, "Restore berhasil!", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@SettingsActivity, "Format file tidak valid", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@SettingsActivity, "Gagal merestore data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
