package com.vendkit.shelfcount

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vendkit.shelfcount.ui.HomeScreen
import com.vendkit.shelfcount.ui.ScannerScreen
import com.vendkit.shelfcount.ui.theme.ShelfCountTheme

class MainActivity : ComponentActivity() {
    private val vm: ShelfViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShelfCountTheme {
                val items by vm.items.collectAsStateWithLifecycle()
                val lastScan by vm.lastScan.collectAsStateWithLifecycle()
                var scanning by rememberSaveable { mutableStateOf(false) }
                val snackbar = remember { SnackbarHostState() }
                var editRequest by remember { mutableStateOf<com.vendkit.shelfcount.data.Item?>(null) }

                val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    if (uri != null) vm.scanPhoto(uri)
                }

                LaunchedEffect(Unit) {
                    vm.events.collect { event ->
                        when (event) {
                            is UiEvent.Share -> {
                                val send = Intent(Intent.ACTION_SEND)
                                    .setType("text/csv")
                                    .putExtra(Intent.EXTRA_STREAM, event.uri)
                                    .putExtra(Intent.EXTRA_SUBJECT, "Shelf Count export")
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                startActivity(Intent.createChooser(send, "Share count"))
                            }
                            is UiEvent.NameNew -> editRequest = event.item
                            is UiEvent.Message -> {
                                val result = snackbar.showSnackbar(
                                    message = event.text,
                                    actionLabel = if (event.undo != null) "Undo" else null,
                                    // Give people time to react when an Undo is offered.
                                    duration = if (event.undo != null) SnackbarDuration.Long else SnackbarDuration.Short,
                                )
                                if (result == SnackbarResult.ActionPerformed && event.undo != null) vm.restore(event.undo)
                            }
                        }
                    }
                }

                if (scanning) {
                    BackHandler { scanning = false }
                    ScannerScreen(lastScan = lastScan, onResult = vm::onCameraScan, onClose = { scanning = false })
                } else {
                    HomeScreen(
                        items = items,
                        snackbar = snackbar,
                        onScan = { scanning = true },
                        onPhoto = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onExport = vm::export,
                        onChange = vm::changeQuantity,
                        onSave = vm::save,
                        onDelete = vm::delete,
                        onClearAll = vm::clearAll,
                        editRequest = editRequest,
                        onEditRequestHandled = { editRequest = null },
                    )
                }
            }
        }
    }
}
