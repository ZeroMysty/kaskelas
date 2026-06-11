package com.example.kaskelasapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anggota")
data class AnggotaEntity(
    @PrimaryKey val id: String,
    val nama: String,
    val nis: String
)
