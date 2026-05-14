package com.example.kaskelasapp.ui.members

import com.example.kaskelasapp.R
import com.example.kaskelasapp.models.Anggota

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
        // IDs for item_anggota_edit
        val tvNamaEdit = view.findViewById<TextView?>(R.id.tvNamaAnggotaEdit)
        val tvIdEdit = view.findViewById<TextView?>(R.id.tvIdAnggotaEdit)
        val tvInisialEdit = view.findViewById<TextView?>(R.id.tvAvatarInisialEdit)

        // IDs for item_anggota_bayar
        val tvNamaBayar = view.findViewById<TextView?>(R.id.tvNamaAnggotaBayar)
        val tvIdBayar = view.findViewById<TextView?>(R.id.tvIdAnggotaBayar)
        val tvInisialBayar = view.findViewById<TextView?>(R.id.tvAvatarInisialBayar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val anggota = listAnggota[position]
        // Ambil 1-2 huruf pertama nama sebagai inisial
        val inisial = anggota.nama.trim().split(" ")
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }

        // Handle layout edit (halaman Anggota)
        if (holder.tvNamaEdit != null) {
            holder.tvNamaEdit.text = anggota.nama
            holder.tvIdEdit?.text = "ID: ${anggota.id}"
            holder.tvInisialEdit?.text = inisial
        }

        // Handle layout bayar (halaman Beranda / Dashboard)
        if (holder.tvNamaBayar != null) {
            holder.tvNamaBayar.text = anggota.nama
            holder.tvIdBayar?.text = "ID: ${anggota.id}"
            holder.tvInisialBayar?.text = inisial
        }

        holder.itemView.setOnClickListener { listener(anggota) }
    }

    override fun getItemCount() = listAnggota.size
}
