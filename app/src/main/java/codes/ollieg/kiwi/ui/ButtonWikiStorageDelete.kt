package codes.ollieg.kiwi.ui

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import codes.ollieg.kiwi.R
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
        }
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = stringResource(R.string.clear_offline_storage_used_by_wiki, wiki.name),
        )
    }
}
