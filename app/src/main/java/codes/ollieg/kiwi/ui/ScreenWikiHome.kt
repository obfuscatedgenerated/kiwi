package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.data.room.WikisViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenWikiHome(
    wikiId: Long,
    // TODO: implement this so when the user types in the search bar, it shows a list of articles (debounce)
    searchCallback: (String) -> List<String>, // maybe change return type to a list of articles when that exists
) {
    Log.i("ScreenWikiHome", "wikiId: $wikiId")

    val context = LocalContext.current.applicationContext as Application
    val wikisViewModel = WikisViewModel(context)

    val wiki = wikisViewModel.getByIdLive(wikiId).observeAsState()

    var searchBarState = rememberSearchBarState()
    var textFieldState = rememberTextFieldState()
    val scope = rememberCoroutineScope()

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onSearch = { /* do nothing, the search results will be live when typing */ },
            placeholder = { Text("Search ${wiki.value?.name ?: "wiki"}") },
            leadingIcon = {
                if (searchBarState.currentValue == SearchBarValue.Expanded) {
                    // use back button when expanded
                    IconButton(
                        onClick = {
                            scope.launch { searchBarState.animateToCollapsed() }
                            textFieldState.clearText()
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            },
        )
    }

    SearchBar(
        state = searchBarState,
        inputField = inputField,
        modifier = Modifier.fillMaxWidth().padding(8.dp).semantics { traversalIndex = 0f },
    )

    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField,
    ) {

    }
}
