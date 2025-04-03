package codes.ollieg.kiwi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiEditDialog(
    wikiId: Long?, // if not specified, this will be an add wiki dialog
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    // now using bottom sheet rather than dialog so it can be swiped down

    ModalBottomSheet (
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RectangleShape, // gets rid of the rounded corners to make it look more natural
        dragHandle = null, // as well as hiding the drag handle
    ) {
        Scaffold (
            modifier = Modifier.safeDrawingPadding().fillMaxSize().padding(horizontal = 8.dp),
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // separate boxes for left and right floated content
                    Row (
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Close modal",
                            )
                        }

                        // if wikiId is null, this is an add wiki dialog
                        val title = if (wikiId == null) {
                            "Add wiki"
                        } else {
                            "Edit wiki"
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    // right floated content
                    Row (
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { /* TODO: save wiki */ },
                            modifier = Modifier.padding(16.dp),
                            colors = ButtonDefaults.textButtonColors()
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        ) { padding ->

        }
    }
}