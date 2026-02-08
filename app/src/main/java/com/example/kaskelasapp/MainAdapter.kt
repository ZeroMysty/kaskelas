package com.example.kaskelasapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnggotaAdapter(
    private val listAnggota: List<Anggota>,
    private val layoutResId: Int, // Bisa item_anggota_bayar atau item_anggota_edit
    private val listener: (Anggota) -> Unit
) : RecyclerView.Adapter<AnggotaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama = view.findViewById<TextView>(R.id.tvNamaEdit)
        val tvId = view.findViewById<TextView>(R.id.tvIdEdit) // Hanya ada di item_anggota_edit
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val anggota = listAnggota[position]
        holder.tvNama?.text = anggota.nama
        holder.tvId?.text = "ID: ${anggota.id}"

        holder.itemView.setOnClickListener { listener(anggota) }
    }

    override fun getItemCount() = listAnggota.size
}