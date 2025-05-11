package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.data.PreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenOtherSettings(
) {
    Log.i("OtherSettingsScreen", "Other Settings Screen")

    val context = LocalContext.current.applicationContext
    val preferencesViewModel = PreferencesViewModel(context as Application)

    val fontOptions = listOf("Serif", "SansSerif", "Monospace", "Cursive")
    val fontPrefState = preferencesViewModel.fontName.observeAsState()
    var fontDropdownExpanded by remember { mutableStateOf(false) }

    // font dropdown
    ExposedDropdownMenuBox(
        expanded = fontDropdownExpanded,
        onExpandedChange = { fontDropdownExpanded = it },
        modifier = Modifier.padding(16.dp)
    ) {
        TextField(
            label = { Text("Article font") },
            value = fontPrefState.value ?: "Serif",
            onValueChange = { },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            readOnly = true
        )

        ExposedDropdownMenu(
            expanded = fontDropdownExpanded,
            onDismissRequest = { fontDropdownExpanded = false }
        ) {
            fontOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        preferencesViewModel.setFont(option)
                        fontDropdownExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
