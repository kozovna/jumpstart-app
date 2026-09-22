package ge.merabi.jumpstart.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import ge.merabi.jumpstart.data.CarBrands
import ge.merabi.jumpstart.data.Order
import ge.merabi.jumpstart.data.OrderStatus
import ge.merabi.jumpstart.data.TbilisiDistricts
import ge.merabi.jumpstart.ui.components.SearchableDropdown
import ge.merabi.jumpstart.util.ClipboardUtil
import ge.merabi.jumpstart.util.LocationUtil
import ge.merabi.jumpstart.util.OrderTextGenerator
import ge.merabi.jumpstart.util.ShareUtil
import ge.merabi.jumpstart.util.TariffUtil
import ge.merabi.jumpstart.viewmodel.OrderViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(
    orderId: Long?,
    viewModel: OrderViewModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var loadedOrder by remember { mutableStateOf<Order?>(null) }
    var loading by remember { mutableStateOf(orderId != null) }

    LaunchedEffect(orderId) {
        if (orderId != null) {
            loadedOrder = viewModel.getOrder(orderId)
            loading = false
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var clientName by remember { mutableStateOf(loadedOrder?.clientName ?: "") }
    var clientPhone by remember { mutableStateOf(loadedOrder?.clientPhone ?: "") }
    var carBrand by remember { mutableStateOf(loadedOrder?.carBrand ?: "") }
    var carModel by remember { mutableStateOf(loadedOrder?.carModel ?: "") }
    var engineType by remember { mutableStateOf(loadedOrder?.engineType ?: "") }
    var engineVolume by remember { mutableStateOf(loadedOrder?.engineVolume ?: "") }
    var plateNumber by remember { mutableStateOf(loadedOrder?.plateNumber ?: "") }
    var carColor by remember { mutableStateOf(loadedOrder?.carColor ?: "") }
    var district by remember { mutableStateOf(loadedOrder?.district ?: "") }
    var exactAddress by remember { mutableStateOf(loadedOrder?.exactAddress ?: "") }
    var mapsLink by remember { mutableStateOf(loadedOrder?.mapsLink ?: "") }
    var additionalInfo by remember { mutableStateOf(loadedOrder?.additionalInfo ?: "") }
    var status by remember { mutableStateOf(loadedOrder?.status ?: OrderStatus.NEW) }
    var priceText by remember {
        mutableStateOf(
            loadedOrder?.price?.let {
                if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }

    val isNightNow = remember {
        loadedOrder?.isNight ?: TariffUtil.isNightNow()
    }

    // ღამის ტარიფის შემთხვევაში ავტომატურად შემოთავაზება, თუ ველი ცარიელია
    LaunchedEffect(Unit) {
        if (loadedOrder == null && isNightNow && priceText.isBlank()) {
            priceText = TariffUtil.NIGHT_PRICE.toLong().toString()
        }
    }

    var missingFieldsMessage by remember { mutableStateOf<String?>(null) }
    var statusExpanded by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                val link = LocationUtil.getCurrentLocationMapsLink(context)
                if (link != null) {
                    mapsLink = link
                    snackbarHostState.showSnackbar("ლოკაცია დამატებულია")
                } else {
                    snackbarHostState.showSnackbar("ლოკაციის მიღება ვერ მოხერხდა")
                }
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("GPS ნებართვა არ არის მინიჭებული") }
        }
    }

    fun requestLocation() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            scope.launch {
                val link = LocationUtil.getCurrentLocationMapsLink(context)
                if (link != null) {
                    mapsLink = link
                    snackbarHostState.showSnackbar("ლოკაცია დამატებულია")
                } else {
                    snackbarHostState.showSnackbar("ლოკაციის მიღება ვერ მოხერხდა")
                }
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun validate(): List<String> {
        val missing = mutableListOf<String>()
        if (clientName.isBlank()) missing.add("კლიენტის სახელი")
        if (clientPhone.isBlank()) missing.add("ტელეფონის ნომერი")
        if (carBrand.isBlank()) missing.add("ავტომობილის მარკა")
        if (engineType.isBlank()) missing.add("ძრავის ტიპი")
        if (plateNumber.isBlank()) missing.add("სახელმწიფო ნომერი")
        if (district.isBlank()) missing.add("უბანი")
        if (exactAddress.isBlank() && mapsLink.isBlank()) missing.add("ზუსტი ლოკაცია (მისამართი ან Maps ბმული)")
        if (priceText.isBlank() || priceText.toDoubleOrNull() == null) missing.add("ფასი")
        return missing
    }

    fun buildOrder(): Order {
        val cal = Calendar.getInstance()
        val timestamp = loadedOrder?.timestamp ?: System.currentTimeMillis()
        val night = loadedOrder?.isNight ?: TariffUtil.isNightForHour(cal.get(Calendar.HOUR_OF_DAY))
        return Order(
            id = loadedOrder?.id ?: 0,
            timestamp = timestamp,
            clientName = clientName.trim(),
            clientPhone = clientPhone.trim(),
            carBrand = carBrand.trim(),
            carModel = carModel.trim(),
            engineType = engineType.trim(),
            engineVolume = engineVolume.trim(),
            plateNumber = plateNumber.trim(),
            carColor = carColor.trim(),
            district = district.trim(),
            exactAddress = exactAddress.trim(),
            mapsLink = mapsLink.trim(),
            isNight = night,
            price = priceText.toDoubleOrNull() ?: 0.0,
            additionalInfo = additionalInfo.trim(),
            status = status
        )
    }

    fun saveAndCopy(thenShare: Boolean) {
        val missing = validate()
        if (missing.isNotEmpty()) {
            missingFieldsMessage = "აკლია: " + missing.joinToString(", ")
            return
        }
        missingFieldsMessage = null
        val order = buildOrder()
        val text = OrderTextGenerator.generate(order)

        if (loadedOrder == null) {
            viewModel.insertOrder(order) {
                ClipboardUtil.copyToClipboard(context, "შეკვეთა", text)
                scope.launch { snackbarHostState.showSnackbar("✓ შეკვეთა დაკოპირდა") }
                if (thenShare) ShareUtil.shareText(context, text)
                onDone()
            }
        } else {
            viewModel.updateOrder(order) {
                ClipboardUtil.copyToClipboard(context, "შეკვეთა", text)
                scope.launch { snackbarHostState.showSnackbar("✓ შეკვეთა დაკოპირდა") }
                if (thenShare) ShareUtil.shareText(context, text)
                onDone()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (loadedOrder == null) "ახალი შეკვეთა" else "შეკვეთის რედაქტირება") }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(Modifier.padding(16.dp)) {
                    missingFieldsMessage?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { saveAndCopy(thenShare = false) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text("📋 დაკოპირება", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = { saveAndCopy(thenShare = true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text("📤 გაგზავნა", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item { SectionTitle("კლიენტის ინფორმაცია") }
            item {
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("კლიენტის სახელი") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("ტელეფონის ნომერი") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { SectionTitle("ავტომობილის ინფორმაცია") }
            item {
                SearchableDropdown(
                    label = "მარკა",
                    options = CarBrands.ALL,
                    selected = carBrand,
                    onSelectedChange = { carBrand = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = carModel,
                    onValueChange = { carModel = it },
                    label = { Text("მოდელი") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                SearchableDropdown(
                    label = "ძრავის ტიპი",
                    options = CarBrands.ENGINE_TYPES,
                    selected = engineType,
                    onSelectedChange = { engineType = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                SearchableDropdown(
                    label = "ძრავის მოცულობა",
                    options = CarBrands.ENGINE_VOLUMES,
                    selected = engineVolume,
                    onSelectedChange = { engineVolume = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = plateNumber,
                    onValueChange = { plateNumber = it.uppercase() },
                    label = { Text("სახელმწიფო ნომერი") },
                    placeholder = { Text("AA-123-AA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                SearchableDropdown(
                    label = "ფერი",
                    options = CarBrands.COLORS,
                    selected = carColor,
                    onSelectedChange = { carColor = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { SectionTitle("თბილისის ტერიტორია") }
            item {
                SearchableDropdown(
                    label = "უბანი",
                    options = TbilisiDistricts.ALL,
                    selected = district,
                    onSelectedChange = { district = it },
                    allowFreeText = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { SectionTitle("ზუსტი ლოკაცია") }
            item {
                OutlinedTextField(
                    value = exactAddress,
                    onValueChange = { exactAddress = it },
                    label = { Text("ზუსტი მისამართი / ლოკაცია") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = mapsLink,
                    onValueChange = { mapsLink = it },
                    label = { Text("Google Maps ლოკაციის ბმული") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedButton(
                    onClick = { requestLocation() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ჩემი მიმდინარე ლოკაცია")
                }
            }

            item { SectionTitle("მომსახურების დრო და ფასი") }
            item {
                val tariffLabel = if (isNightNow) "🌙 ღამის ტარიფი" else "☀️ დღის ტარიფი"
                Text(tariffLabel, style = MaterialTheme.typography.titleMedium)
            }
            if (!isNightNow) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TariffUtil.DAY_PRICE_OPTIONS.forEach { p ->
                            val priceLong = p.toLong().toString()
                            FilterChip(
                                selected = priceText == priceLong,
                                onClick = { priceText = priceLong },
                                label = { Text("$priceLong ₾") }
                            )
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("ფასი (₾)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { SectionTitle("დამატებითი ინფორმაცია") }
            item {
                OutlinedTextField(
                    value = additionalInfo,
                    onValueChange = { additionalInfo = it },
                    label = { Text("დამატებითი ინფორმაცია (არასავალდებულო)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { SectionTitle("შეკვეთის სტატუსი") }
            item {
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = status,
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
                                    status = s
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}
