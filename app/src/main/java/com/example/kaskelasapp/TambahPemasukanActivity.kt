package com.example.kaskelasapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TambahPemasukanActivity : AppCompatActivity() {
    
    private var imageUri: Uri? = null
    private lateinit var ivPreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pemasukan)
        BackgroundHelper.applyAnimatedBackground(this)


        val db = DatabaseHelper(this)

        // 🔥 Ambil dari XML (HARUS SESUAI ID)
        val etNama = findViewById<EditText>(R.id.etNamaPemasukan)
        val etJumlah = findViewById<EditText>(R.id.etJumlahPemasukan)
        val etKet = findViewById<EditText>(R.id.etKeteranganPemasukan)
        val btnSimpan = findViewById<Button >(R.id.btnSimpanPemasukan)
        val btnPilihFoto = findViewById<LinearLayout>(R.id.btnPilihFotoPemasukan)
        ivPreview = findViewById(R.id.ivPreviewPemasukan)

        // 🔥 REGISTER IMAGE PICKER
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                ivPreview.setImageURI(uri)
                ivPreview.setPadding(0, 0, 0, 0)
                ivPreview.imageTintList = null
                ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }

        // 🔥 REGISTER PERMISSION LAUNCHER
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

        // 🔥 Ambil anggotaId dari intent
        val anggotaId = intent.getStringExtra("ANGGOTA_ID")

        // 🔥 FORMAT RUPIAH
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

        // 🔥 BUTTON SIMPAN → ALERT DULU
        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString()
            val jumlahRaw = etJumlah.text.toString()
            val jumlahBersih = jumlahRaw.replace(".", "")
            val ket = etKet.text.toString()
            val tanggal = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

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
                        // Simpan foto ke internal storage jika ada
                        val finalImagePath = imageUri?.let { saveImageToInternalStorage(it) }

                        val result = db.insertTransaksi(
                            nama,
                            jumlahBersih,
                            tanggal,
                            "MASUK",
                            ket,
                            anggotaId,
                            finalImagePath
                        )

                        if (result == -1L) {
                            Toast.makeText(this, "Gagal menyimpan ke database", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Pemasukan disimpan!", Toast.LENGTH_SHORT).show()
                            finish()
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

        // 🔙 tombol back
        findViewById<ImageView>(R.id.btnBackPemasukan).setOnClickListener {
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