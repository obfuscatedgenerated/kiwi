package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.R
import codes.ollieg.kiwi.data.PreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenOtherSettings(
) {
    Log.i("OtherSettingsScreen", "Other Settings Screen")

    val context = LocalContext.current.applicationContext
    val preferencesViewModel = PreferencesViewModel(context as Application)

    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
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
                label = { Text(stringResource(R.string.article_font)) },
                value = fontPrefState.value ?: "Serif",
                onValueChange = { },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
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

        val lineHeightOptions = listOf(12, 24, 36, 48)
        val lineHeightPrefState = preferencesViewModel.lineHeight.observeAsState()
        var lineHeightDropdownExpanded by remember { mutableStateOf(false) }

        // line height dropdown
        ExposedDropdownMenuBox(
            expanded = lineHeightDropdownExpanded,
            onExpandedChange = { lineHeightDropdownExpanded = it },
            modifier = Modifier.padding(16.dp)
        ) {
            TextField(
                label = { Text(stringResource(R.string.line_height)) },
                value = lineHeightPrefState.value?.toString() ?: "24",
                onValueChange = { },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true
            )

            ExposedDropdownMenu(
                expanded = lineHeightDropdownExpanded,
                onDismissRequest = { lineHeightDropdownExpanded = false }
            ) {
                lineHeightOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text("$option") },
                        onClick = {
                            preferencesViewModel.setLineHeight(option)
                            lineHeightDropdownExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}
