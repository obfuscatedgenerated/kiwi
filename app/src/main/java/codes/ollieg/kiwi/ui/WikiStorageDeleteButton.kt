package codes.ollieg.kiwi.ui

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.data.room.Wiki

@Composable
fun WikiStorageDeleteButton(
    wiki: Wiki,
) {
    IconButton(
        onClick = { Log.i("WikiStorageDeleteButton", "Delete button clicked for ${wiki.name}") },
        modifier = Modifier.padding(16.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Delete offline storage used by ${wiki.name}",
        )
    }
}
