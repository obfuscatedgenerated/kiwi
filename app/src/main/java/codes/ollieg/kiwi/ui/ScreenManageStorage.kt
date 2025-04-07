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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.R
import codes.ollieg.kiwi.data.room.ArticlesViewModel
import codes.ollieg.kiwi.data.room.WikisViewModel

@Composable
fun ScreenManageStorage(
) {
    val context = LocalContext.current.applicationContext as Application
    val wikisViewModel = WikisViewModel(context)
    val articlesViewModel = ArticlesViewModel(context)

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
                    text = stringResource(R.string.clear_all),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { padding ->
        WikiList(
            wikis = allWikis.value ?: emptyList(),
            subtexts = allWikis.value?.map { wiki ->
                // estimate storage usage for each wiki
                val storage = articlesViewModel.estimateOfflineStorageUsageForWikiLive(wiki).observeAsState()

                if (storage.value?.bytes == null) {
                    return@map stringResource(R.string.estimating_usage)
                }

                // format bytes to the cleanest unit
                var byteUnit = R.string.bytes
                var byteValue = storage.value?.bytes ?: 0L
                val count = storage.value?.count ?: 0

                if (byteValue == 0L || count == 0) {
                    return@map stringResource(R.string.no_offline_data)
                }

                // could handle pluralisation of "bytes" but it's impossible to be a single byte

                if (byteValue > 1024) {
                    byteValue /= 1024
                    byteUnit = R.string.kb
                }
                if (byteValue > 1024) {
                    byteValue /= 1024
                    byteUnit = R.string.mb
                }
                if (byteValue > 1024) {
                    byteValue /= 1024
                    byteUnit = R.string.gb
                }

                // e.g. 2MB used (50 articles)
                val byteString = stringResource(byteUnit, byteValue)
                val countString = pluralStringResource(R.plurals.articles, count, count)
                stringResource(R.string.offline_storage_value, byteString, countString)
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
