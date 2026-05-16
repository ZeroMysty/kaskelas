package com.example.kaskelasapp.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kaskelasapp.R
import com.example.kaskelasapp.models.Transaksi
import java.text.NumberFormat
import java.util.*

class KonfirmasiAdapter(
    private var list: List<Transaksi>,
    private val onApprove: (Transaksi) -> Unit,
    private val onReject: (Transaksi) -> Unit
) : RecyclerView.Adapter<KonfirmasiAdapter.ViewHolder>() {

    fun updateData(newList: List<Transaksi>) {
        list = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJudul = view.findViewById<TextView>(R.id.tvJudulKonfirmasi)
        val tvTanggal = view.findViewById<TextView>(R.id.tvTanggalKonfirmasi)
        val tvJumlah = view.findViewById<TextView>(R.id.tvJumlahKonfirmasi)
        val btnApprove = view.findViewById<Button>(R.id.btnApproveKonfirmasi)
        val btnReject = view.findViewById<Button>(R.id.btnRejectKonfirmasi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_konfirmasi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.tvJudul.text = data.nama
        holder.tvTanggal.text = data.tanggal
        
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        holder.tvJumlah.text = formatter.format(data.jumlah.toLongOrNull() ?: 0L).replace("Rp", "Rp ")

        holder.btnApprove.setOnClickListener { onApprove(data) }
        holder.btnReject.setOnClickListener { onReject(data) }
    }

    override fun getItemCount() = list.size
}
