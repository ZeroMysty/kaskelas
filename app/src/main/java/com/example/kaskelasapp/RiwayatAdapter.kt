package com.example.kaskelasapp

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class RiwayatAdapter(private var list: List<Transaksi>, private val onClick: (Transaksi) -> Unit) :
    RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    fun updateData(newList: List<Transaksi>) {
        list = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJudul = view.findViewById<TextView>(R.id.tvNamaRiwayat)
        val tvTanggal = view.findViewById<TextView>(R.id.tvTanggalRiwayat)
        val tvJumlah = view.findViewById<TextView>(R.id.tvNominalRiwayat)
        val ivTipe = view.findViewById<ImageView>(R.id.ivTipeRiwayat)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.tvJudul.text = data.nama
        holder.tvTanggal.text = data.tanggal

        val localeID = java.util.Locale.Builder().setLanguage("id").setRegion("ID").build()
        val numberFormat = java.text.NumberFormat.getNumberInstance(localeID)
        val formattedJumlah = numberFormat.format(data.jumlah.toLongOrNull() ?: 0)

        // Ganti ikon dan warna berdasarkan tipe (Masuk/Keluar)
        if (data.tipe == "MASUK") {
            holder.tvJumlah.text = "+ Rp $formattedJumlah"
            holder.tvJumlah.setTextColor(android.graphics.Color.parseColor("#16A34A"))
            holder.ivTipe.setImageResource(R.drawable.ic_plus_circle_green)
            holder.ivTipe.setBackgroundResource(R.drawable.bg_chip_green)
        } else {
            holder.tvJumlah.text = "- Rp $formattedJumlah"
            holder.tvJumlah.setTextColor(android.graphics.Color.parseColor("#DC2626"))
            holder.ivTipe.setImageResource(R.drawable.ic_minus_circle_red)
            holder.ivTipe.setBackgroundResource(R.drawable.bg_chip_red)
        }



        holder.itemView.setOnClickListener { onClick(data) }
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

    override fun getItemCount() = list.size
}