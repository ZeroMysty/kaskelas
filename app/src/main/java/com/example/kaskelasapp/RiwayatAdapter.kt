package com.example.kaskelasapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RiwayatAdapter(private val list: List<Transaksi>, private val onClick: (Transaksi) -> Unit) :
    RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJudul = view.findViewById<TextView>(R.id.tvNamaTransaksi)
        val tvTanggal = view.findViewById<TextView>(R.id.tvTanggal)
        val tvJumlah = view.findViewById<TextView>(R.id.tvStatus)
        val imgIcon = view.findViewById<ImageView>(R.id.imgIconRiwayat)
        val imgTypeIndicator = view.findViewById<ImageView>(R.id.imgTypeIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.tvJudul.text = data.nama
        holder.tvTanggal.text = data.tanggal

        // Default icon
        holder.imgIcon.setImageResource(R.drawable.ic_wallet)

        val localeID = java.util.Locale.Builder().setLanguage("id").setRegion("ID").build()
        val numberFormat = java.text.NumberFormat.getNumberInstance(localeID)
        val formattedJumlah = numberFormat.format(data.jumlah.toLongOrNull() ?: 0)

        // Ganti ikon dan warna berdasarkan tipe (Masuk/Keluar)
        if (data.tipe == "MASUK") {
            holder.tvJumlah.text = "+ Rp $formattedJumlah"
            holder.tvJumlah.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            holder.imgTypeIndicator.setImageResource(R.drawable.ic_plus_circle_green)
        } else {
            holder.tvJumlah.text = "- Rp $formattedJumlah"
            holder.tvJumlah.setTextColor(android.graphics.Color.parseColor("#F44336"))
            holder.imgTypeIndicator.setImageResource(R.drawable.ic_minus_circle_red)
        }

        holder.itemView.setOnClickListener { onClick(data) }
    }

    override fun getItemCount() = list.size
}