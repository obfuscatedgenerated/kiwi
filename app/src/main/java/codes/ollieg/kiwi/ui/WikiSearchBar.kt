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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import codes.ollieg.kiwi.R
import codes.ollieg.kiwi.data.room.Article
import codes.ollieg.kiwi.data.room.ArticlesViewModel
import codes.ollieg.kiwi.data.room.Wiki
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

const val DEBOUNCE_TIME = 500L
const val N_RESULTS = 15

class WikiSearchViewModel(private val wiki: Wiki, private val articlesViewModel: ArticlesViewModel) : ViewModel() {
    val liveInput = MutableStateFlow("")

    // don't want to spam the api too much, so debounce the input
    @OptIn(FlowPreview::class)
    val debouncedInput = liveInput.debounce(DEBOUNCE_TIME).distinctUntilChanged()

    private val _searchResults = MutableStateFlow(listOf<Article>())
    private val _runningSearch = MutableStateFlow(false)

    // public read only stateflows
    val searchResults = _searchResults.asStateFlow()
    val runningSearch = _runningSearch.asStateFlow()

    private suspend fun updateSearch(query: String) {
        if (query.isEmpty()) {
            Log.i("WikiSearchViewModel", "Empty query, clearing search results")
            _searchResults.value = emptyList()
            return
        }

        _runningSearch.value = true

        val context = articlesViewModel.getApplication<Application>().applicationContext

        try {
            val results = articlesViewModel.search(wiki, query, context, N_RESULTS)
            Log.i("WikiSearchViewModel", "search results: ${results.size}")
            _searchResults.value = results
        } catch (e: Exception) {
            Log.e("WikiSearchViewModel", "Error searching wiki: ${e.message}")
            _searchResults.value = emptyList()
        } finally {
            _runningSearch.value = false
            Log.i("WikiSearchViewModel", "search completed")
        }
    }

    init {
        // update search results when debounced input changes
        viewModelScope.launch {
            debouncedInput.collectLatest { query ->
                Log.i("WikiSearchViewModel", "debouncedInput: $query")
                updateSearch(query)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiSearchBar(
    wiki: Wiki,
    articlesViewModel: ArticlesViewModel,
    onResultClick: ((Article) -> Unit)? = null,
) {
    val viewModel = remember { WikiSearchViewModel(wiki, articlesViewModel) }

    var searchBarState = rememberSearchBarState()
    var textFieldState = rememberTextFieldState()
    val scope = rememberCoroutineScope()

    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val runningSearch by viewModel.runningSearch.collectAsStateWithLifecycle()
    val debouncedInput by viewModel.debouncedInput.collectAsStateWithLifecycle("")

    Log.i("WikiSearchBar", "ui searchResults: ${searchResults.size}")

    // side effect to update view model when text field changes
    LaunchedEffect(textFieldState) {
        // snapshot flow ensures that the text field state is updated, not just recomposition
        snapshotFlow { textFieldState.text }
            .collectLatest { text ->
                viewModel.liveInput.value = text.toString()
            }
    }

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            onSearch = { /* do nothing, the search results will be live when typing */ },
            placeholder = { Text(stringResource(R.string.search_wiki, wiki.name)) },
            leadingIcon = {
                if (searchBarState.currentValue == SearchBarValue.Expanded) {
                    // use back button when expanded
                    IconButton(
                        onClick = {
                            scope.launch { searchBarState.animateToCollapsed() }
                            textFieldState.clearText()
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.search_close)
                        )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .semantics { traversalIndex = 0f },
    )

    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField,
    ) {
        // show something if debounced input is empty
        if (debouncedInput.isEmpty()) {
            return@ExpandedFullScreenSearchBar Text(
                text = stringResource(R.string.search_empty_prompt),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }

        // show a loading indicator if the search is running
        if (runningSearch == true) {
            return@ExpandedFullScreenSearchBar CenteredLoader()
        }

        // empty state
        if (searchResults.isEmpty()) {
            return@ExpandedFullScreenSearchBar Text(
                text = stringResource(R.string.search_no_results),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }

        // show article list and pass through the click handler
        ArticleList(
            articles = searchResults,
            useThumbnails = false,
            onResultClick = onResultClick,
        )

        // TODO: load next page of results if they scroll past the 15th result
    }
}
