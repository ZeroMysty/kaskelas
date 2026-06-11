package com.example.kaskelasapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.example.kaskelasapp.viewmodel.KasViewModel
import com.example.kaskelasapp.viewmodel.KasViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TambahPemasukanActivity : AppCompatActivity() {
    
    private var imageUri: Uri? = null
    private lateinit var ivPreview: ImageView
    private lateinit var viewModel: KasViewModel
    // FIX #5: simpan tanggal yang dipilih user
    private var selectedDate: Date = Date()
    private lateinit var tvTanggalPilihan: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pemasukan)
        BackgroundHelper.applyAnimatedBackground(this)

        viewModel = ViewModelProvider(
            this,
            KasViewModelFactory(application)
        )[KasViewModel::class.java]

        val etNama = findViewById<EditText>(R.id.etNamaPemasukan)
        val etJumlah = findViewById<EditText>(R.id.etJumlahPemasukan)
        val etKet = findViewById<EditText>(R.id.etKeteranganPemasukan)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanPemasukan)
        val btnPilihFoto = findViewById<LinearLayout>(R.id.btnPilihFotoPemasukan)
        ivPreview = findViewById(R.id.ivPreviewPemasukan)
        tvTanggalPilihan = findViewById(R.id.tvTanggalPilihan)

        // Tampilkan tanggal hari ini sebagai default
        updateTanggalDisplay()

        // FIX #5: Tombol pemilih tanggal
        tvTanggalPilihan.setOnClickListener { showDatePicker() }
        findViewById<ImageView>(R.id.ivIconKalender)?.setOnClickListener { showDatePicker() }

        // REGISTER IMAGE PICKER
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                ivPreview.setImageURI(uri)
                ivPreview.setPadding(0, 0, 0, 0)
                ivPreview.imageTintList = null
                ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }

        // REGISTER PERMISSION LAUNCHER
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pickImage.launch("image/*")
            } else {
                Toast.makeText(this, "Izin galeri diperlukan!", Toast.LENGTH_SHORT).show()
            }
        }

        btnPilihFoto.setOnClickListener {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                pickImage.launch("image/*")
            } else {
                requestPermissionLauncher.launch(permission)
            }
        }

        // Ambil anggotaId dari intent (jika ada)
        val anggotaId = intent.getStringExtra("ANGGOTA_ID")
        
        val intentMode = intent.getStringExtra("MODE")
        val transaksiId = intent.getIntExtra("TRANSAKSI_ID", -1)
        
        if (intentMode == "EDIT" && transaksiId != -1) {
            findViewById<TextView>(R.id.tvHeaderTitlePemasukan).text = "Edit Pemasukan"
            btnSimpan.text = "Simpan Perubahan"
            
            lifecycleScope.launch {
                val db = (application as com.example.kaskelasapp.KasKelasApp).database
                val tx = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    db.transaksiDao().getTransaksiById(transaksiId)
                }
                if (tx != null) {
                    etNama.setText(tx.nama)
                    etJumlah.setText(tx.jumlah)
                    etKet.setText(tx.keterangan)
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    try {
                        selectedDate = sdf.parse(tx.tanggal) ?: Date()
                        updateTanggalDisplay()
                    } catch (e: Exception) {}
                    if (tx.buktiFoto != null) {
                        imageUri = Uri.fromFile(File(tx.buktiFoto))
                        ivPreview.setImageURI(imageUri)
                        ivPreview.setPadding(0, 0, 0, 0)
                        ivPreview.imageTintList = null
                        ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                }
            }
        }

        // FORMAT RUPIAH
        etJumlah.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    etJumlah.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDoubleOrNull() ?: 0.0
                        val formatted = DecimalFormat("#,###")
                            .format(parsed)
                            .replace(",", ".")

                        current = formatted
                        etJumlah.setText(formatted)
                        etJumlah.setSelection(formatted.length)
                    } else {
                        current = ""
                        etJumlah.setText("")
                    }

                    etJumlah.addTextChangedListener(this)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // BUTTON SIMPAN
        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString()
            val jumlahRaw = etJumlah.text.toString()
            val jumlahBersih = jumlahRaw.replace(".", "")
            val ket = etKet.text.toString()
            val tanggal = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate)

            if (nama.isEmpty() || jumlahBersih.isEmpty()) {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val message = """
                Yakin ingin menyimpan pemasukan?

                Nama       : $nama
                Jumlah     : Rp $jumlahRaw
                Tanggal    : $tanggal
                Keterangan : ${if (ket.isEmpty()) "-" else ket}
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Pemasukan")
                .setMessage(message)
                .setPositiveButton("Ya") { _, _ ->
                    try {
                        val finalImagePath = imageUri?.let { uri ->
                            if (uri.scheme == "file") uri.path else saveImageToInternalStorage(uri)
                        }

                        val newTrans = com.example.kaskelasapp.data.TransaksiEntity(
                            id = if (intentMode == "EDIT") transaksiId else 0,
                            nama = nama,
                            jumlah = jumlahBersih,
                            tanggal = tanggal,
                            tipe = "MASUK",
                            keterangan = ket,
                            anggota_id = anggotaId,
                            buktiFoto = finalImagePath
                        )

                        if (intentMode == "EDIT") {
                            viewModel.updateTransaksi(newTrans) {
                                Toast.makeText(this@TambahPemasukanActivity, "Perubahan disimpan!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        } else {
                            viewModel.insertTransaksi(newTrans) {
                                Toast.makeText(this@TambahPemasukanActivity, "Pemasukan disimpan!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }

                    } catch (e: Exception) {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }

        // Tombol back
        findViewById<ImageView>(R.id.btnBackPemasukan).setOnClickListener {
            konfirmasiKeluar(etNama, etJumlah)
        }

        // FIX #15: Konfirmasi saat sistem back ditekan
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                konfirmasiKeluar(etNama, etJumlah)
            }
        })
    }

    // FIX #5: Tampilkan MaterialDatePicker
    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal Transaksi")
            .setSelection(selectedDate.time)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            selectedDate = Date(selection)
            updateTanggalDisplay()
        }
        picker.show(supportFragmentManager, "DATE_PICKER_PEMASUKAN")
    }

    private fun updateTanggalDisplay() {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        tvTanggalPilihan.text = sdf.format(selectedDate)
    }

    // FIX #15: Konfirmasi jika ada data yang sudah diisi
    private fun konfirmasiKeluar(etNama: EditText, etJumlah: EditText) {
        val hasData = etNama.text.isNotEmpty() || etJumlah.text.isNotEmpty()
        if (hasData) {
            AlertDialog.Builder(this)
                .setTitle("Batalkan Pemasukan?")
                .setMessage("Data yang sudah diisi akan hilang. Yakin ingin keluar?")
                .setPositiveButton("Ya, Keluar") { _, _ -> finish() }
                .setNegativeButton("Tetap di Sini", null)
                .show()
        } else {
            finish()
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "bukti_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}