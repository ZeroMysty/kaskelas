package com.example.kaskelasapp

import android.net.Uri
import android.os.Bundle
import java.io.File
import android.view.View
import android.widget.ImageButton
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailPemasukanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pemasukan)

        val db = DatabaseHelper(this)
        val transaksiId = intent.getIntExtra("TRANSAKSI_ID", -1)

        if (transaksiId != -1) {
            val transaksi = db.getTransaksiById(transaksiId)
            if (transaksi != null) {
                findViewById<TextView>(R.id.tvDetailNamaP).text = transaksi.nama
                findViewById<TextView>(R.id.tvDetailJumlahP).text = "Rp ${transaksi.jumlah}"
                findViewById<TextView>(R.id.tvDetailTanggalP).text = transaksi.tanggal
                findViewById<TextView>(R.id.tvDetailKeteranganP).text = transaksi.keterangan
                
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
                            showFullScreenImage(this, transaksi.buktiFoto!!)
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

        findViewById<ImageButton>(R.id.btnBackDetailP).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSelesaiDetailP).setOnClickListener { finish() }
        
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