package codes.ollieg.kiwi.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun WikiEditDialog(
    wikiId: Long?, // if not specified, this will be an add wiki dialog
    onDismissRequest: () -> Unit,
) {
    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismissRequest
    ) {
        Text(text = "Edit Wiki Dialog")
    }
}