package com.example.kaskelasapp

data class Anggota(
    val id: String,
    val nama: String,
    val nis: String
)

data class Transaksi(
    val id: Int,
    val nama: String,
    val jumlah: String,
    val tanggal: String,
    val tipe: String, // "MASUK" atau "KELUAR"
    val keterangan: String = ""
)