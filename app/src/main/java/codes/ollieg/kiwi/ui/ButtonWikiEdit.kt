package codes.ollieg.kiwi.ui

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import codes.ollieg.kiwi.data.room.Wiki

@Composable
fun ButtonWikiEdit(
    wiki: Wiki,
    onClick: (Wiki) -> Unit,
) {
    IconButton(
        onClick = {
            Log.i("ButtonWikiEdit", "Edit button clicked for ${wiki.name}")
            onClick(wiki)
        }
    ) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Edit ${wiki.name}",
        )
    }
}
