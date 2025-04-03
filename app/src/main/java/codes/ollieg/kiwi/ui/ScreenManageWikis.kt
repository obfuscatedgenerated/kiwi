package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.data.room.WikisViewModel

@Composable
fun ScreenManageWikis(
) {
    val context = LocalContext.current.applicationContext as Application
    val wikisViewModel = WikisViewModel(context)

    val allWikis = wikisViewModel.allWikis.observeAsState()

    var editDialogVisible by remember { mutableStateOf(false) }
    var editDialogWiki: Long? by remember { mutableStateOf(null) }

    fun showEditDialog(wikiId: Long?) {
        editDialogWiki = wikiId
        editDialogVisible = true
    }

    fun hideEditDialog() {
        editDialogVisible = false
    }

    Scaffold (
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    Log.i("ScreenManageWikis", "Add button clicked")

                    // null id is used for adding wikis
                    showEditDialog(null)
                },
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )

                Text(
                    text = "Add wiki",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { padding ->
        WikiList(
            wikis = allWikis.value ?: emptyList(),
            button = { wiki ->
                ButtonWikiEdit(
                    wiki = wiki,
                    onClick = { showEditDialog(wiki.id) }
                )
            },
            modifier = Modifier.padding(padding)
        )

        // show fullscreen edit dialog form if state is true
        if (editDialogVisible) {
            DialogWikiEdit(
                wikiId = editDialogWiki,
                onDismissRequest = { hideEditDialog() },
            )
        }
    }
}
