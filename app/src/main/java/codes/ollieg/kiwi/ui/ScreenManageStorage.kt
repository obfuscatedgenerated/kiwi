package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.data.room.WikisViewModel

@Composable
fun ScreenManageStorage(
) {
    val context = LocalContext.current.applicationContext as Application
    val wikisViewModel = WikisViewModel(context)

    val allWikis = wikisViewModel.allWikis.observeAsState()

    Scaffold (
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { Log.i("ScreenManageStorage", "Delete button clicked") },
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null
                )

                Text(
                    text = "Clear all",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { padding ->
        WikiList(
            wikis = allWikis.value ?: emptyList(),
            subtexts = allWikis.value?.map { wiki ->
                "Storage size goes here"
            },
            button = { wiki ->
                ButtonWikiStorageDelete(
                    wiki = wiki,
                    onClick = {/* TODO */}
                )
            },
            modifier = Modifier.padding(padding)
        )
    }
}
