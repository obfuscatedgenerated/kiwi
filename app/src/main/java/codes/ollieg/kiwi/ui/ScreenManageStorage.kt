package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.R
import codes.ollieg.kiwi.data.room.ArticlesViewModel
import codes.ollieg.kiwi.data.room.Wiki
import codes.ollieg.kiwi.data.room.WikisViewModel

@Composable
fun ScreenManageStorage(
) {
    val context = LocalContext.current.applicationContext as Application
    val wikisViewModel = WikisViewModel(context)
    val articlesViewModel = ArticlesViewModel(context)

    val allWikis = wikisViewModel.allWikis.observeAsState()

    var clearDialogVisible by remember { mutableStateOf(false) }
    var clearDialogWiki: Wiki? by remember { mutableStateOf(null) }

    Scaffold (
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    Log.i("ScreenManageStorage", "Clear all button clicked")

                    // null means all wikis
                    clearDialogWiki = null
                    clearDialogVisible = true
                },
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
        if (clearDialogVisible) {
            AlertDialog(
                title = {
                    val clearDialogWikiName = if (clearDialogWiki == null) {
                        stringResource(R.string.all_wikis)
                    } else {
                        clearDialogWiki!!.name
                    }

                    Text(
                        text = stringResource(R.string.clear_wiki_storage_dialog_title, clearDialogWikiName)
                    )
                },
                text = {
                    Text(
                        text = stringResource(
                            R.string.clear_wiki_storage_dialog_text
                        )
                    )
                },
                onDismissRequest = {
                    clearDialogVisible = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            Log.i("ScreenManageStorage", "Clear confirmed")

                            if (clearDialogWiki == null) {
                                // clear all wikis
                                articlesViewModel.deleteAllFromCache()
                            } else {
                                // clear specific wiki
                                articlesViewModel.deleteAllByWikiFromCache(clearDialogWiki!!)
                            }

                            clearDialogVisible = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.clear))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            Log.i("ScreenManageStorage", "Clear cancelled")
                            clearDialogVisible = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

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
                    onClick = {
                        Log.i("ScreenManageStorage", "Delete button clicked for ${wiki.name}")

                        clearDialogWiki = wiki
                        clearDialogVisible = true
                    }
                )
            },
            modifier = Modifier.padding(padding)
        )
    }
}
