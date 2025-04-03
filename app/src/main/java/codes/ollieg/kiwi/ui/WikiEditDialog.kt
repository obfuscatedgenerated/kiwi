package codes.ollieg.kiwi.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape

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
        Surface (
            modifier = Modifier.safeContentPadding().fillMaxSize()
        ) {
            Text("Edit Wiki Dialog - $wikiId")
        }
    }
}