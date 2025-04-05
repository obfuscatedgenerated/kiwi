package codes.ollieg.kiwi.ui

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.ollieg.kiwi.data.room.Wiki
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

const val DEBOUNCE_TIME = 1000L

class WikiSearchViewModel : ViewModel() {
    val liveInput = MutableStateFlow("")

    // don't want to spam the api too much, so debounce the input
    @OptIn(FlowPreview::class)
    val debouncedInput = liveInput.debounce(DEBOUNCE_TIME).distinctUntilChanged()

    // TODO: update to Article entity
    val searchResults = MutableStateFlow<List<String>>(emptyList())

    private fun updateSearch(query: String) {
        // TODO: search with api url
        // for now return a list of foo, bar, baz
        searchResults.value = listOf<String>("foo", "bar", "baz").filter { it.contains(query, ignoreCase = true) }
    }

    init {
        // update search results when debounced input changes
        viewModelScope.launch {
            debouncedInput.collect { query ->
                updateSearch(query)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiSearchBar(
    wiki: Wiki,
    viewModel: WikiSearchViewModel = WikiSearchViewModel(),
) {
    var searchBarState = rememberSearchBarState()
    var textFieldState = rememberTextFieldState()
    val scope = rememberCoroutineScope()

    val searchResults = viewModel.searchResults.collectAsState()

    // side effect to update view model when text field changes
    LaunchedEffect(textFieldState.text) {
        viewModel.liveInput.value = textFieldState.text.toString()
    }

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onSearch = { /* do nothing, the search results will be live when typing */ },
            placeholder = { Text("Search ${wiki.name}") },
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
        searchResults.value.forEach {
            Text(
                text = it,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}