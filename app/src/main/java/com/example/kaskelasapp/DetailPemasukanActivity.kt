package com.example.kaskelasapp

import android.net.Uri
import android.os.Bundle
import java.io.File
import android.view.View
import android.widget.ImageButton
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.kaskelasapp.viewmodel.KasViewModel
import com.example.kaskelasapp.viewmodel.KasViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailPemasukanActivity : AppCompatActivity() {

    private lateinit var viewModel: KasViewModel
    private var transaksiId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pemasukan)

        viewModel = ViewModelProvider(
            this,
            KasViewModelFactory(application)
        )[KasViewModel::class.java]

        transaksiId = intent.getIntExtra("TRANSAKSI_ID", -1)

        if (transaksiId != -1) {
            lifecycleScope.launch {
                val app = application as com.example.kaskelasapp.KasKelasApp
                val db = app.database
                val transaksi = withContext(Dispatchers.IO) {
                    db.transaksiDao().getTransaksiById(transaksiId)
                }

                if (transaksi != null) {
                    val localeID = java.util.Locale.Builder().setLanguage("id").setRegion("ID").build()
                    val formatter = java.text.NumberFormat.getNumberInstance(localeID)
                    // Fix: format jumlah dengan pemisah ribuan
                    val jumlahBersih = transaksi.jumlah.replace(".", "").replace(",", "").toLongOrNull() ?: 0L

                    findViewById<TextView>(R.id.tvDetailNamaP).text = transaksi.nama
                    findViewById<TextView>(R.id.tvDetailJumlahP).text = "Rp ${formatter.format(jumlahBersih)}"
                    findViewById<TextView>(R.id.tvDetailTanggalP).text = transaksi.tanggal
                    findViewById<TextView>(R.id.tvDetailKeteranganP).text =
                        if (transaksi.keterangan.isBlank()) "-" else transaksi.keterangan

                    val ivBukti = findViewById<ImageView>(R.id.ivDetailBuktiP)
                    val tvNoBukti = findViewById<TextView>(R.id.tvNoBuktiP)

                    if (!transaksi.buktiFoto.isNullOrEmpty()) {
                        try {
                            val file = File(transaksi.buktiFoto)
                            if (file.exists()) {
                                ivBukti.setImageURI(Uri.fromFile(file))
                            } else {
                                ivBukti.setImageURI(Uri.parse(transaksi.buktiFoto))
                            }
                            ivBukti.alpha = 1.0f
                            ivBukti.scaleType = ImageView.ScaleType.CENTER_CROP

                            ivBukti.setOnClickListener {
                                showFullScreenImage(this@DetailPemasukanActivity, transaksi.buktiFoto)
                            }
                        } catch (e: Exception) {
                            ivBukti.visibility = View.GONE
                            tvNoBukti.visibility = View.VISIBLE
                            tvNoBukti.text = "Gagal memuat gambar"
                        }
                    } else {
                        ivBukti.visibility = View.GONE
                        tvNoBukti.visibility = View.VISIBLE
                    }
                }
            }
        }

        findViewById<ImageButton>(R.id.btnBackDetailP).setOnClickListener { finish() }
        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnSelesaiDetailP).setOnClickListener {
            finish()
        }

        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnEditDetailP).setOnClickListener {
            val intent = android.content.Intent(this@DetailPemasukanActivity, TambahPemasukanActivity::class.java)
            intent.putExtra("MODE", "EDIT")
            intent.putExtra("TRANSAKSI_ID", transaksiId)
            startActivity(intent)
            finish()
        }

        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnHapusDetailP).setOnClickListener {
            konfirmasiHapus()
        }

        BackgroundHelper.applyAnimatedBackground(this)

        // Quick Entrance Anim
        findViewById<View>(R.id.tvDetailJumlahP)?.parent?.let { parent ->
            if (parent is View) {
                parent.alpha = 0f
                parent.translationY = 50f
                parent.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(100).start()
            }
        }
    }

    private fun konfirmasiHapus() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Yakin ingin menghapus transaksi ini? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteTransaksi(transaksiId) {
                    Toast.makeText(this, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showFullScreenImage(context: android.content.Context, imagePath: String) {
        val dialog = android.app.Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_full_screen_image)
        val imageView = dialog.findViewById<ImageView>(R.id.ivFullScreen)
        val btnClose = dialog.findViewById<ImageView>(R.id.btnFullClose)

        try {
            val file = File(imagePath)
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file))
            } else {
                imageView.setImageURI(Uri.parse(imagePath))
            }
        } catch (e: Exception) {
            // handle error
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}