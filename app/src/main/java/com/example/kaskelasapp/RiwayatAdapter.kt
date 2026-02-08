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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.tvJudul.text = data.nama
        holder.tvTanggal.text = data.tanggal
        holder.tvJumlah.text = "Rp. ${data.jumlah}"

        // Ganti ikon berdasarkan tipe (Masuk/Keluar)
        if (data.tipe == "MASUK") {
            holder.imgIcon.setImageResource(R.drawable.green_add)
        } else {
            holder.imgIcon.setImageResource(R.drawable.negative)
        }

        holder.itemView.setOnClickListener { onClick(data) }
    }

    override fun getItemCount() = list.size
}