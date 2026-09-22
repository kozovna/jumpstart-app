package ge.merabi.jumpstart.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ge.merabi.jumpstart.data.Order
import ge.merabi.jumpstart.data.OrderStatus
import ge.merabi.jumpstart.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: OrderViewModel,
    onBack: () -> Unit,
    onOpenOrder: (Long) -> Unit
) {
    val orders by viewModel.orders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("შეკვეთების ისტორია") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "უკან")
                    }
                }
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("შეკვეთები ჯერ არ არის", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderRow(order = order, onClick = { onOpenOrder(order.id) })
                }
            }
        }
    }
}

@Composable
private fun OrderRow(order: Order, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd.MM, HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${order.carBrand} ${order.carModel}".trim(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                StatusChip(order.status)
            }
            Spacer(Modifier.height(4.dp))
            Text("${dateFormat.format(Date(order.timestamp))}  •  ${order.district}")
            Text("${order.clientName}  •  ${order.clientPhone}")
            Spacer(Modifier.height(4.dp))
            Text(
                "${formatPrice(order.price)} ₾  ${if (order.isNight) "🌙" else "☀️"}",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val color = when (status) {
        OrderStatus.NEW -> MaterialTheme.colorScheme.primary
        OrderStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }
    AssistChip(
        onClick = {},
        label = { Text(status, style = MaterialTheme.typography.bodySmall) },
        colors = AssistChipDefaults.assistChipColors(labelColor = color)
    )
}

private fun formatPrice(price: Double): String =
    if (price == price.toLong().toDouble()) price.toLong().toString() else price.toString()
