package com.example.kaskelasapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaksi")
data class TransaksiEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "judul") val nama: String,
    val jumlah: String,
    val tanggal: String,
    @ColumnInfo(name = "jenis") val tipe: String,
    val keterangan: String,
    @ColumnInfo(name = "anggota_id") val anggota_id: String? = null,
    @ColumnInfo(name = "bukti_foto") val buktiFoto: String? = null
)
