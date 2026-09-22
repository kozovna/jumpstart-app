package ge.merabi.jumpstart.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ge.merabi.jumpstart.data.Order
import ge.merabi.jumpstart.data.OrderStatus
import ge.merabi.jumpstart.util.ClipboardUtil
import ge.merabi.jumpstart.util.OrderTextGenerator
import ge.merabi.jumpstart.util.ShareUtil
import ge.merabi.jumpstart.viewmodel.OrderViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long,
    viewModel: OrderViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var order by remember { mutableStateOf<Order?>(null) }
    var statusExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        order = viewModel.getOrder(orderId)
    }

    val current = order

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("შეკვეთის დეტალები") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "უკან")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(orderId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "რედაქტირება")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "წაშლა")
                    }
                }
            )
        }
    ) { padding ->
        if (current == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()) }

        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "${current.carBrand} ${current.carModel}".trim(),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(dateFormat.format(Date(current.timestamp)), color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(16.dp))

            DetailRow("👤 კლიენტი", current.clientName)
            DetailRow("📞 ტელეფონი", current.clientPhone)
            DetailRow("⛽ ძრავი", "${current.engineVolume} ${current.engineType}".trim())
            DetailRow("🔢 ნომერი", current.plateNumber)
            if (current.carColor.isNotBlank()) DetailRow("🎨 ფერი", current.carColor)
            DetailRow("📍 უბანი", current.district)
            if (current.exactAddress.isNotBlank()) DetailRow("📌 მისამართი", current.exactAddress)
            if (current.mapsLink.isNotBlank()) DetailRow("🗺️ Maps", current.mapsLink)
            DetailRow("💰 ფასი", "${formatPrice(current.price)} ₾ (${if (current.isNight) "ღამე" else "დღე"})")
            if (current.additionalInfo.isNotBlank()) DetailRow("📝 დამატებითი", current.additionalInfo)

            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it }
            ) {
                OutlinedTextField(
                    value = current.status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("სტატუსი") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    OrderStatus.ALL.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = {
                                statusExpanded = false
                                val updated = current.copy(status = s)
                                order = updated
                                viewModel.updateOrder(updated)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { ShareUtil.dialPhone(context, current.clientPhone) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("დარეკვა")
                }
                OutlinedButton(
                    onClick = {
                        val text = OrderTextGenerator.generate(current)
                        ClipboardUtil.copyToClipboard(context, "შეკვეთა", text)
                        scope.launch { snackbarHostState.showSnackbar("✓ შეკვეთა დაკოპირდა") }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("დაკოპირება")
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    val text = OrderTextGenerator.generate(current)
                    ShareUtil.shareText(context, text)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("გაზიარება")
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("შეკვეთის წაშლა") },
                text = { Text("დარწმუნებული ხართ, რომ გსურთ ამ შეკვეთის წაშლა?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.deleteOrder(current) { onBack() }
                    }) { Text("წაშლა") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("გაუქმება") }
                }
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatPrice(price: Double): String =
    if (price == price.toLong().toDouble()) price.toLong().toString() else price.toString()
