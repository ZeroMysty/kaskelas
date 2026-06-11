package com.example.kaskelasapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "anggota")
data class AnggotaEntity(
    @PrimaryKey val id: String,
    val nama: String,
    val nis: String,
    @ColumnInfo(name = "tanggal_bergabung")
    val tanggalBergabung: String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
)
