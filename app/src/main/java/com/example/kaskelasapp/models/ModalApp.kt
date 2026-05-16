package com.example.kaskelasapp.models

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
    val keterangan: String = "",
    val anggota_id: String? = null,
    val metode: String = "TUNAI", // "TUNAI" atau "E-WALLET"
    val status: String = "SUCCESS" // "SUCCESS" atau "PENDING" (untuk konfirmasi)
)
