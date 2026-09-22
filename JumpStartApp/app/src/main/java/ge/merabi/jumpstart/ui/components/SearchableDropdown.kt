package ge.merabi.jumpstart.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * ველი ჩამოსაშლელი სიით + ტექსტური ძებნით.
 * ოპერატორს შეუძლია აკრიფოს პირველი ასოები სიის გასაფილტრად,
 * ან თუ allowFreeText = true, პირდაპირ ჩაწეროს მნიშვნელობა ხელით.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowFreeText: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember(selected) { mutableStateOf(selected) }

    val filtered = remember(query, options) {
        if (query.isBlank()) options
        else options.filter { it.contains(query, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                if (allowFreeText) onSelectedChange(it)
                expanded = true
            },
            label = { Text(label) },
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filtered.take(60).forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        query = option
                        onSelectedChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
