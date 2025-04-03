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
fun ButtonWikiStorageDelete(
    wiki: Wiki,
    onClick: (Wiki) -> Unit
) {
    IconButton(
        onClick = {
            Log.i("ButtonWikiStorageDelete", "Delete button clicked for ${wiki.name}")
            onClick(wiki)
        },
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Clear offline storage used by ${wiki.name}",
        )
    }
}
