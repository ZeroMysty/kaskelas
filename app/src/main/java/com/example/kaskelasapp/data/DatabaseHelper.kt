package com.example.kaskelasapp.data

import com.example.kaskelasapp.models.Anggota
import com.example.kaskelasapp.models.Transaksi

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "uangkas.db", null, 3) {
    override fun onCreate(db: SQLiteDatabase) {
        // Tabel Anggota
        db.execSQL("CREATE TABLE anggota (id TEXT PRIMARY KEY, nama TEXT, nis TEXT)")
        // Tabel Transaksi
        db.execSQL("CREATE TABLE transaksi (id INTEGER PRIMARY KEY AUTOINCREMENT, judul TEXT, jumlah TEXT, tanggal TEXT, jenis TEXT, keterangan TEXT, anggota_id TEXT, metode TEXT, status TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE transaksi ADD COLUMN anggota_id TEXT")
            }
            if (oldVersion < 3) {
                // Check if columns exist before adding to avoid crash if upgrade partially succeeded
                try { db.execSQL("ALTER TABLE transaksi ADD COLUMN metode TEXT DEFAULT 'TUNAI'") } catch (e: Exception) {}
                try { db.execSQL("ALTER TABLE transaksi ADD COLUMN status TEXT DEFAULT 'SUCCESS'") } catch (e: Exception) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fungsi Tambah Anggota
    fun insertAnggota(id: String, nama: String, nis: String): Long {
        return try {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put("id", id)
                put("nama", nama)
                put("nis", nis)
            }
            db.insert("anggota", null, values)
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }

    // Fungsi Tambah Transaksi
    fun insertTransaksi(judul: String, jumlah: String, tanggal: String, jenis: String, keterangan: String, anggotaId: String? = null, metode: String = "TUNAI", status: String = "SUCCESS"): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("judul", judul)
            put("jumlah", jumlah)
            put("tanggal", tanggal)
            put("jenis", jenis)
            put("keterangan", keterangan)
            put("metode", metode)
            put("status", status)
            if (anggotaId != null) {
                put("anggota_id", anggotaId)
            }
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
                
                // Get extra columns safely
                val indexAnggotaId = getColumnIndex("anggota_id")
                val anggotaId = if (indexAnggotaId != -1 && !isNull(indexAnggotaId)) getString(indexAnggotaId) else null
                
                val indexMetode = getColumnIndex("metode")
                val metode = if (indexMetode != -1 && !isNull(indexMetode)) getString(indexMetode) else "TUNAI"
                
                val indexStatus = getColumnIndex("status")
                val status = if (indexStatus != -1 && !isNull(indexStatus)) getString(indexStatus) else "SUCCESS"
                
                list.add(Transaksi(id, nama, jumlah, tanggal, tipe, ket, anggotaId, metode, status))
            }
            close()
        }
        return list
    }

    // Fungsi Ambil Transaksi Spesifik Anggota
    fun getTransaksiByAnggota(anggotaId: String): List<Transaksi> {
        val db = this.readableDatabase
        val list = mutableListOf<Transaksi>()
        val cursor = db.query("transaksi", null, "anggota_id = ?", arrayOf(anggotaId), null, null, "id DESC")
        
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow("id"))
                val nama = getString(getColumnIndexOrThrow("judul"))
                val jumlah = getString(getColumnIndexOrThrow("jumlah"))
                val tanggal = getString(getColumnIndexOrThrow("tanggal"))
                val tipe = getString(getColumnIndexOrThrow("jenis"))
                val ket = getString(getColumnIndexOrThrow("keterangan"))
                list.add(Transaksi(id, nama, jumlah, tanggal, tipe, ket, anggotaId))
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
            val indexAnggotaId = cursor.getColumnIndex("anggota_id")
            val anggotaId = if (indexAnggotaId != -1 && !cursor.isNull(indexAnggotaId)) cursor.getString(indexAnggotaId) else null
            cursor.close()
            Transaksi(id, nama, jumlah, tanggal, tipe, ket, anggotaId)
        } else {
            cursor.close()
            null
        }
    }

    // Fungsi Hitung Total Saldo (Current Balance)
    fun hitungTotalSaldo(): Long {
        return hitungTotalPemasukan() - hitungTotalPengeluaran()
    }

    // Fungsi Hitung Total Pemasukan
    fun hitungTotalPemasukan(): Long {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(CAST(jumlah AS INTEGER)) as total FROM transaksi WHERE jenis='MASUK' AND status='SUCCESS'", null)
        var total = 0L
        if (cursor.moveToFirst()) {
            total = cursor.getLong(0)
        }
        cursor.close()
        return total
    }

    // Fungsi Hitung Total Pengeluaran
    fun hitungTotalPengeluaran(): Long {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(CAST(jumlah AS INTEGER)) as total FROM transaksi WHERE jenis='KELUAR' AND status='SUCCESS'", null)
        var total = 0L
        if (cursor.moveToFirst()) {
            total = cursor.getLong(0)
        }
        cursor.close()
        return total
    }

    // Fungsi Hitung Saldo Spesifik Siswa (Hanya yang SUCCESS)
    fun getSaldoBySiswa(anggotaId: String): Long {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(CAST(jumlah AS INTEGER)) FROM transaksi WHERE anggota_id = ? AND jenis = 'MASUK' AND status = 'SUCCESS'", arrayOf(anggotaId))
        var total = 0L
        if (cursor.moveToFirst()) {
            total = cursor.getLong(0)
        }
        cursor.close()
        return total
    }

    // Fungsi Hitung Saldo Berdasarkan Metode
    fun getSaldoByMetode(metode: String): Long {
        val db = this.readableDatabase
        val cursorMasuk = db.rawQuery("SELECT SUM(CAST(jumlah AS INTEGER)) FROM transaksi WHERE metode = ? AND jenis = 'MASUK' AND status = 'SUCCESS'", arrayOf(metode))
        val cursorKeluar = db.rawQuery("SELECT SUM(CAST(jumlah AS INTEGER)) FROM transaksi WHERE metode = ? AND jenis = 'KELUAR' AND status = 'SUCCESS'", arrayOf(metode))
        
        var totalMasuk = 0L
        var totalKeluar = 0L
        
        if (cursorMasuk.moveToFirst()) totalMasuk = cursorMasuk.getLong(0)
        if (cursorKeluar.moveToFirst()) totalKeluar = cursorKeluar.getLong(0)
        
        cursorMasuk.close()
        cursorKeluar.close()
        
        return totalMasuk - totalKeluar
    }

    // Fungsi Ambil Transaksi yang Pending
    fun getPendingTransaksi(): List<Transaksi> {
        val db = this.readableDatabase
        val list = mutableListOf<Transaksi>()
        val cursor = db.query("transaksi", null, "status = 'PENDING'", null, null, null, "id DESC")
        
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow("id"))
                val nama = getString(getColumnIndexOrThrow("judul"))
                val jumlah = getString(getColumnIndexOrThrow("jumlah"))
                val tanggal = getString(getColumnIndexOrThrow("tanggal"))
                val tipe = getString(getColumnIndexOrThrow("jenis"))
                val ket = getString(getColumnIndexOrThrow("keterangan"))
                val anggotaId = getString(getColumnIndexOrThrow("anggota_id"))
                val metode = getString(getColumnIndexOrThrow("metode"))
                val status = getString(getColumnIndexOrThrow("status"))
                list.add(Transaksi(id, nama, jumlah, tanggal, tipe, ket, anggotaId, metode, status))
            }
            close()
        }
        return list
    }

    // Fungsi Update Status Transaksi (Confirm/Reject)
    fun updateTransaksiStatus(id: Int, newStatus: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("status", newStatus)
        }
        return db.update("transaksi", values, "id = ?", arrayOf(id.toString()))
    }

    // Fungsi Hitung Jumlah Transaksi Pending
    fun getCountPending(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM transaksi WHERE status = 'PENDING'", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
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

    // Fungsi Cari Anggota Berdasarkan NIS (Login Siswa)
    fun findAnggotaByNis(nis: String): Anggota? {
        return try {
            val db = this.readableDatabase
            val cursor = db.query("anggota", null, "nis = ?", arrayOf(nis), null, null, null)
            if (cursor.moveToFirst()) {
                val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
                val nama = cursor.getString(cursor.getColumnIndexOrThrow("nama"))
                cursor.close()
                Anggota(id, nama, nis)
            } else {
                cursor.close()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Fungsi Hapus Anggota
    fun deleteAnggota(id: String): Int {
        val db = this.writableDatabase
        return db.delete("anggota", "id = ?", arrayOf(id))
    }

    // Fungsi Reset Seluruh Data (Factory Reset)
    fun resetDatabase() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM transaksi")
        db.execSQL("DELETE FROM anggota")
        // Reset autoincrement ID jika perlu
        db.execSQL("DELETE FROM sqlite_sequence WHERE name='transaksi'")
    }
}
