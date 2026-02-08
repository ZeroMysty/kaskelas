package com.example.kaskelasapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnggotaBayarAdapter(
    private val list: List<Anggota>,
    private val onClick: (Anggota) -> Unit
) : RecyclerView.Adapter<AnggotaBayarAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama = view.findViewById<TextView>(R.id.tvNamaItem)
        val imgIcon = view.findViewById<ImageView>(R.id.imgIcon)
        val btnBayar = view.findViewById<Button>(R.id.btnBayarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_anggota_bayar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val anggota = list[position]
        holder.tvNama.text = anggota.nama

        holder.btnBayar.setOnClickListener { onClick(anggota) }
    }

    override fun getItemCount() = list.size
}
