package com.example.kaskelasapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kaskelasapp.data.AnggotaEntity
import com.example.kaskelasapp.data.TransaksiEntity
import com.example.kaskelasapp.repository.KasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class KasViewModel(private val repository: KasRepository) : ViewModel() {

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

    fun insertTransaksi(transaksi: TransaksiEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.insertTransaksi(transaksi)
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
}

class KasViewModelFactory(private val repository: KasRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KasViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
