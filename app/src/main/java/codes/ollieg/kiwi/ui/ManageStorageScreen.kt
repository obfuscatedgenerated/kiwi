package codes.ollieg.kiwi.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import codes.ollieg.kiwi.data.room.WikisViewModel

@Composable
fun ManageStorageScreen(
) {
    val context = LocalContext.current.applicationContext as Application
    val wikisViewModel = WikisViewModel(context)

    val allWikis = wikisViewModel.allWikis.observeAsState()

    WikiList(
        wikis = allWikis.value ?: emptyList(),
        button = { wiki ->
            WikiStorageDeleteButton(
                wiki = wiki,
            )
        }
    )
}
