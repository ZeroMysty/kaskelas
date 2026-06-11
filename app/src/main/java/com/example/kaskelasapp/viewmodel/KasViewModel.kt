package com.example.kaskelasapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kaskelasapp.KasKelasApp
import com.example.kaskelasapp.data.AnggotaEntity
import com.example.kaskelasapp.data.TransaksiEntity
import com.example.kaskelasapp.repository.KasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class KasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KasRepository = (application as KasKelasApp).repository

    private val _anggotaList = MutableStateFlow<List<AnggotaEntity>>(emptyList())
    val anggotaList: StateFlow<List<AnggotaEntity>> = _anggotaList

    private val _transaksiList = MutableStateFlow<List<TransaksiEntity>>(emptyList())
    val transaksiList: StateFlow<List<TransaksiEntity>> = _transaksiList

    private val _totalSaldo = MutableStateFlow<Long>(0L)
    val totalSaldo: StateFlow<Long> = _totalSaldo

    fun loadAllAnggota() {
        viewModelScope.launch {
            _anggotaList.value = repository.getAllAnggota()
        }
    }

    fun insertAnggota(anggota: AnggotaEntity) {
        viewModelScope.launch {
            repository.insertAnggota(anggota)
            loadAllAnggota()
        }
    }

    fun updateAnggota(anggota: AnggotaEntity) {
        viewModelScope.launch {
            repository.updateAnggota(anggota)
            loadAllAnggota()
        }
    }

    fun deleteAnggota(id: String) {
        viewModelScope.launch {
            repository.deleteAnggota(id)
            loadAllAnggota()
        }
    }

    fun loadAllTransaksi() {
        viewModelScope.launch {
            _transaksiList.value = repository.getAllTransaksi()
        }
    }

    fun loadTransaksiByAnggota(anggotaId: String) {
        viewModelScope.launch {
            _transaksiList.value = repository.getTransaksiByAnggota(anggotaId)
        }
    }

    fun insertTransaksi(transaksi: TransaksiEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertTransaksi(transaksi)
            loadAllTransaksi()
            loadTotalSaldo()
            onComplete()
        }
    }

    fun updateTransaksi(transaksi: TransaksiEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateTransaksi(transaksi)
            loadAllTransaksi()
            loadTotalSaldo()
            onComplete()
        }
    }

    fun deleteTransaksi(id: Int, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteTransaksiById(id)
            loadAllTransaksi()
            loadTotalSaldo()
            onComplete?.invoke()
        }
    }

    fun loadTotalSaldo() {
        viewModelScope.launch {
            _totalSaldo.value = repository.hitungTotalSaldo()
        }
    }

    fun resetDatabase(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.resetDatabase()
            loadAllAnggota()
            loadAllTransaksi()
            loadTotalSaldo()
            onComplete?.invoke()
        }
    }

    // Load data langsung dari DB sebelum backup (tidak andalkan StateFlow.value)
    fun getDataForBackup(onReady: (List<AnggotaEntity>, List<TransaksiEntity>) -> Unit) {
        viewModelScope.launch {
            val anggota = repository.getAllAnggota()
            val transaksi = repository.getAllTransaksi()
            onReady(anggota, transaksi)
        }
    }

    // Batch restore — insert semua data sekaligus, hanya 1x trigger loadAll di akhir
    fun batchRestore(
        anggotaList: List<AnggotaEntity>,
        transaksiList: List<TransaksiEntity>,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            repository.batchRestore(anggotaList, transaksiList)
            loadAllAnggota()
            loadAllTransaksi()
            loadTotalSaldo()
            onComplete?.invoke()
        }
    }
}

// Factory menggunakan Application — tidak perlu repository parameter lagi
class KasViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KasViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
