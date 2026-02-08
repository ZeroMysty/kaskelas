package com.example.kaskelasapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "uangkas.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        // Tabel Anggota
        db.execSQL("CREATE TABLE anggota (id TEXT PRIMARY KEY, nama TEXT, nis TEXT)")
        // Tabel Transaksi
        db.execSQL("CREATE TABLE transaksi (id INTEGER PRIMARY KEY AUTOINCREMENT, judul TEXT, jumlah TEXT, tanggal TEXT, jenis TEXT, keterangan TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS anggota")
        db.execSQL("DROP TABLE IF EXISTS transaksi")
        onCreate(db)
    }

    // Fungsi Tambah Anggota
    fun insertAnggota(id: String, nama: String, nis: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("id", id)
            put("nama", nama)
            put("nis", nis)
        }
        return db.insert("anggota", null, values)
    }

    // Fungsi Tambah Transaksi
    fun insertTransaksi(judul: String, jumlah: String, tanggal: String, jenis: String, keterangan: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("judul", judul)
            put("jumlah", jumlah)
            put("tanggal", tanggal)
            put("jenis", jenis)
            put("keterangan", keterangan)
        }
        return db.insert("transaksi", null, values)
    }

    // Fungsi Ambil Semua Transaksi
    fun getAllTransaksi(): List<Transaksi> {
        val db = this.readableDatabase
        val list = mutableListOf<Transaksi>()
        val cursor = db.query("transaksi", null, null, null, null, null, "id DESC")
        
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow("id"))
                val nama = getString(getColumnIndexOrThrow("judul"))
                val jumlah = getString(getColumnIndexOrThrow("jumlah"))
                val tanggal = getString(getColumnIndexOrThrow("tanggal"))
                val tipe = getString(getColumnIndexOrThrow("jenis"))
                val ket = getString(getColumnIndexOrThrow("keterangan"))
                list.add(Transaksi(id, nama, jumlah, tanggal, tipe, ket))
            }
            close()
        }
        return list
    }

    // Fungsi Ambil Transaksi By ID
    fun getTransaksiById(id: Int): Transaksi? {
        val db = this.readableDatabase
        val cursor = db.query("transaksi", null, "id = ?", arrayOf(id.toString()), null, null, null)
        
        return if (cursor.moveToFirst()) {
            val nama = cursor.getString(cursor.getColumnIndexOrThrow("judul"))
            val jumlah = cursor.getString(cursor.getColumnIndexOrThrow("jumlah"))
            val tanggal = cursor.getString(cursor.getColumnIndexOrThrow("tanggal"))
            val tipe = cursor.getString(cursor.getColumnIndexOrThrow("jenis"))
            val ket = cursor.getString(cursor.getColumnIndexOrThrow("keterangan"))
            cursor.close()
            Transaksi(id, nama, jumlah, tanggal, tipe, ket)
        } else {
            cursor.close()
            null
        }
    }

    // Fungsi Hitung Total Saldo
    fun hitungTotalSaldo(): Long {
        val db = this.readableDatabase
        val cursorMasuk = db.rawQuery("SELECT SUM(CAST(jumlah AS INTEGER)) as total FROM transaksi WHERE jenis='MASUK'", null)
        val cursorKeluar = db.rawQuery("SELECT SUM(CAST(jumlah AS INTEGER)) as total FROM transaksi WHERE jenis='KELUAR'", null)
        
        var totalMasuk = 0L
        var totalKeluar = 0L
        
        if (cursorMasuk.moveToFirst()) {
            totalMasuk = cursorMasuk.getLong(0)
        }
        if (cursorKeluar.moveToFirst()) {
            totalKeluar = cursorKeluar.getLong(0)
        }
        
        cursorMasuk.close()
        cursorKeluar.close()
        
        return totalMasuk - totalKeluar
    }

    // Fungsi Ambil Semua Anggota
    fun getAllAnggota(): List<Anggota> {
        val db = this.readableDatabase
        val list = mutableListOf<Anggota>()
        val cursor = db.query("anggota", null, null, null, null, null, null)
        
        with(cursor) {
            while (moveToNext()) {
                val id = getString(getColumnIndexOrThrow("id"))
                val nama = getString(getColumnIndexOrThrow("nama"))
                val nis = getString(getColumnIndexOrThrow("nis"))
                list.add(Anggota(id, nama, nis))
            }
            close()
        }
        return list
    }

    // Fungsi Update Anggota
    fun updateAnggota(id: String, nama: String, nis: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nama", nama)
            put("nis", nis)
        }
        return db.update("anggota", values, "id = ?", arrayOf(id))
    }

    // Fungsi Hapus Anggota
    fun deleteAnggota(id: String): Int {
        val db = this.writableDatabase
        return db.delete("anggota", "id = ?", arrayOf(id))
    }
}