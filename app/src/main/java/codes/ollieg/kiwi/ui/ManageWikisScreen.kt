package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.data.room.WikisViewModel

@Composable
fun ManageWikisScreen(
) {
    val context = LocalContext.current.applicationContext as Application
    val wikisViewModel = WikisViewModel(context)

    val allWikis = wikisViewModel.allWikis.observeAsState()
    val wikiNames = allWikis.value?.map { it.name } ?: emptyList()

    Text(
        text = wikiNames.joinToString(", "),
        modifier = Modifier.padding(16.dp)
    )
}
