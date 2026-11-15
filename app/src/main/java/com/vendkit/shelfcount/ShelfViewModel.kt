package com.vendkit.shelfcount

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.vendkit.shelfcount.data.AppDatabase
import com.vendkit.shelfcount.data.Item
import com.vendkit.shelfcount.logic.Barcodes
import com.vendkit.shelfcount.logic.Csv
import com.vendkit.shelfcount.scan.BarcodeReader
import com.vendkit.shelfcount.scan.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

sealed interface UiEvent {
    data class Message(val text: String, val undo: Item? = null) : UiEvent
    data class Share(val uri: Uri) : UiEvent
    data class NameNew(val item: Item) : UiEvent
}

class ShelfViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).items()

    val items: StateFlow<List<Item>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _lastScan = MutableStateFlow<Item?>(null)
    val lastScan: StateFlow<Item?> = _lastScan.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // The camera sees the same code many times a second; count it once per 1.5 s.
    private var lastCode = ""
    private var lastCodeAt = 0L

    fun onCameraScan(result: ScanResult) {
        val now = System.currentTimeMillis()
        if (result.value == lastCode && now - lastCodeAt < 1_500) return
        lastCode = result.value
        lastCodeAt = now
        count(result)
    }

    private fun count(result: ScanResult) = viewModelScope.launch {
        _lastScan.value = dao.countScan(result.value, result.format, Barcodes.defaultName(result.value), System.currentTimeMillis())
    }

    fun scanPhoto(uri: Uri) = viewModelScope.launch {
        val found = try {
            val image = InputImage.fromFilePath(getApplication(), uri)
            BarcodeReader.client.process(image).await().firstNotNullOfOrNull(BarcodeReader::toResult)
        } catch (e: Exception) {
            Log.w("ShelfCount", "Could not read barcode from photo", e)
            null
        }
        if (found == null) {
            _events.send(UiEvent.Message("No barcode found in that photo"))
        } else {
            val item = dao.countScan(found.value, found.format, Barcodes.defaultName(found.value), System.currentTimeMillis())
            if (item.quantity == 1 && item.name == Barcodes.defaultName(found.value)) {
                _events.send(UiEvent.NameNew(item))
            } else {
                _events.send(UiEvent.Message("${item.name} · now ${item.quantity}"))
            }
        }
    }

    fun changeQuantity(item: Item, delta: Int) = viewModelScope.launch {
        dao.upsert(item.copy(quantity = (item.quantity + delta).coerceAtLeast(0), updatedAt = System.currentTimeMillis()))
    }

    fun save(item: Item, name: String, quantity: Int) = viewModelScope.launch {
        dao.upsert(item.copy(name = name.trim().ifEmpty { item.name }, quantity = quantity.coerceAtLeast(0), updatedAt = System.currentTimeMillis()))
    }

    fun delete(item: Item) = viewModelScope.launch {
        dao.delete(item)
        _events.send(UiEvent.Message("Deleted ${item.name}", undo = item))
    }

    fun restore(item: Item) = viewModelScope.launch { dao.upsert(item) }

    fun clearAll() = viewModelScope.launch { dao.clear() }

    fun export() = viewModelScope.launch {
        val list = items.value
        if (list.isEmpty()) return@launch
        val uri = withContext(Dispatchers.IO) {
            val dir = File(getApplication<Application>().cacheDir, "exports").apply { mkdirs() }
            val file = File(dir, "shelf-count-${LocalDate.now()}.csv")
            file.writeText(Csv.build(list))
            FileProvider.getUriForFile(getApplication(), "${getApplication<Application>().packageName}.files", file)
        }
        _events.send(UiEvent.Share(uri))
    }
}
