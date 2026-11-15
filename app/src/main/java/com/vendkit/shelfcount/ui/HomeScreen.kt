package com.vendkit.shelfcount.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vendkit.shelfcount.R
import com.vendkit.shelfcount.data.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    items: List<Item>,
    snackbar: SnackbarHostState,
    onScan: () -> Unit,
    onPhoto: () -> Unit,
    onExport: () -> Unit,
    onChange: (Item, Int) -> Unit,
    onSave: (Item, String, Int) -> Unit,
    onDelete: (Item) -> Unit,
    onClearAll: () -> Unit,
    editRequest: Item? = null,
    onEditRequestHandled: () -> Unit = {},
) {
    var editing by remember { mutableStateOf<Item?>(null) }
    LaunchedEffect(editRequest) { if (editRequest != null) { editing = editRequest; onEditRequestHandled() } }
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Shelf Count", fontWeight = FontWeight.Black)
                        Text(
                            summary(items),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onExport, enabled = items.isNotEmpty()) {
                        Icon(Icons.Filled.Share, contentDescription = "Export CSV")
                    }
                    Box {
                        IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "More") }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(
                                text = { Text("Clear all counts") },
                                enabled = items.isNotEmpty(),
                                onClick = { menu = false; confirmClear = true },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(onClick = onScan, modifier = Modifier.weight(1f).height(56.dp)) {
                        Icon(painterResource(R.drawable.ic_scan), contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Scan", style = MaterialTheme.typography.titleMedium)
                    }
                    FilledTonalButton(onClick = onPhoto, modifier = Modifier.weight(1f).height(56.dp)) {
                        Icon(painterResource(R.drawable.ic_photo), contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("From photo", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState(Modifier.padding(padding))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 4.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(items, key = { it.barcode }) { item ->
                    ItemRow(item, onClick = { editing = item }, onChange = { onChange(item, it) })
                }
            }
        }
    }

    editing?.let { item ->
        EditDialog(
            item = item,
            onDismiss = { editing = null },
            onSave = { name, qty -> onSave(item, name, qty); editing = null },
            onDelete = { onDelete(item); editing = null },
        )
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all counts?") },
            text = { Text("This removes every product from the list. Export first if you need the numbers.") },
            confirmButton = { TextButton(onClick = { onClearAll(); confirmClear = false }) { Text("Clear all") } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
        )
    }
}

internal fun summary(items: List<Item>): String {
    val units = items.sumOf { it.quantity }
    val products = items.size
    return "$products ${if (products == 1) "product" else "products"} · $units ${if (units == 1) "unit" else "units"}"
}

@Composable
private fun ItemRow(item: Item, onClick: () -> Unit, onChange: (Int) -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier.padding(start = 18.dp, end = 10.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${item.barcode} · ${item.format}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalIconButton(onClick = { onChange(-1) }, enabled = item.quantity > 0) {
                Icon(painterResource(R.drawable.ic_minus), contentDescription = "One less ${item.name}")
            }
            Text(
                item.quantity.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 44.dp),
            )
            FilledTonalIconButton(onClick = { onChange(1) }) {
                Icon(Icons.Filled.Add, contentDescription = "One more ${item.name}")
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painterResource(R.drawable.ic_scan),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Nothing counted yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Scan a product barcode with the camera, or pick a photo of one. Scanning the same barcode again adds one more.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EditDialog(item: Item, onDismiss: () -> Unit, onSave: (String, Int) -> Unit, onDelete: () -> Unit) {
    var name by rememberSaveable(item.barcode) { mutableStateOf(item.name) }
    var qty by rememberSaveable(item.barcode) { mutableStateOf(item.quantity.toString()) }
    val qtyValue = qty.toIntOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${item.barcode} · ${item.format}",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(60) },
                    label = { Text("Name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = qty,
                    onValueChange = { v -> qty = v.filter(Char::isDigit).take(6) },
                    label = { Text("Quantity") },
                    singleLine = true,
                    isError = qtyValue == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                TextButton(onClick = onDelete, contentPadding = PaddingValues(0.dp)) {
                    Text("Delete product", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, qtyValue ?: item.quantity) }, enabled = qtyValue != null) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
