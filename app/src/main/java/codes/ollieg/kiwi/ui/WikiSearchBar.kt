package codes.ollieg.kiwi.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.ollieg.kiwi.data.fromApiBase
import codes.ollieg.kiwi.data.fetch
import codes.ollieg.kiwi.data.room.Article
import codes.ollieg.kiwi.data.withDefaultHeaders
import codes.ollieg.kiwi.data.room.Wiki
import codes.ollieg.kiwi.data.setQueryParameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.json.JSONObject

const val DEBOUNCE_TIME = 1500L
const val N_RESULTS = 15

// TODO: make search respond quicker

class WikiSearchViewModel(private val wiki: Wiki) : ViewModel() {
    val liveInput = MutableStateFlow("")

    // don't want to spam the api too much, so debounce the input
    @OptIn(FlowPreview::class)
    val debouncedInput = liveInput.debounce(DEBOUNCE_TIME).distinctUntilChanged()

    val searchResults = MutableStateFlow<List<Article>>(emptyList())
    var runningSearch = MutableStateFlow(false)

    private fun updateSearch(query: String) {
        if (query.isEmpty()) {
            searchResults.value = emptyList()
            return
        }

        runningSearch.value = true

        // build the search request url safely
        var searchUrl = fromApiBase(wiki.apiUrl, "?action=query&list=search&utf8=&format=json")
        searchUrl = setQueryParameter(searchUrl, "srsearch", query)
        searchUrl = setQueryParameter(searchUrl, "srlimit", N_RESULTS.toString())

        Log.i("WikiSearchViewModel", "searchUrl: $searchUrl")

        // launch a coroutine to fetch the search results and update the state
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val searchRes = fetch(searchUrl, withDefaultHeaders())
                Log.i("WikiSearchViewModel", "searchRes: $searchRes")

                // parse the json
                val data = JSONObject(searchRes)
                val searchData = data.getJSONObject("query").getJSONArray("search")
                val articles = mutableListOf<Article>()

                // iterate over the search results and create article objects
                for (i in 0 until searchData.length()) {
                    val entry = searchData.getJSONObject(i)
                    val pageId = entry.getLong("pageid")
                    val title = entry.getString("title")

                    var snippetHtml: String? = null
                    try {
                        snippetHtml = entry.getString("snippet")
                    } catch (e: Exception) {
                        Log.e("WikiSearchViewModel", "Error getting snippet (or null)", e)
                    }

                    var parsedSnippet = AnnotatedString.fromHtml(snippetHtml ?: "")

                    val article = Article(
                        wikiId = wiki.id,
                        pageId = pageId,
                        title = title,
                        parsedSnippet = parsedSnippet.toString(),
                    )

                    articles.add(article)
                }

                // update the search results state
                if (articles.isEmpty()) {
                    searchResults.value = emptyList()
                } else {
                    searchResults.value = articles
                }

                runningSearch.value = false
            } catch (e: Exception) {
                Log.e("WikiSearchViewModel", "Error fetching search results", e)
                // TODO: graceful ui behaviour
            }
        }
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
    viewModel: WikiSearchViewModel = WikiSearchViewModel(wiki),
    onResultClick: ((Article) -> Unit)? = null,
) {
    var searchBarState = rememberSearchBarState()
    var textFieldState = rememberTextFieldState()
    val scope = rememberCoroutineScope()

    val searchResults = viewModel.searchResults.collectAsState()
    val runningSearch = viewModel.runningSearch.collectAsState()
    val debouncedInput = viewModel.debouncedInput.collectAsState("")

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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close search"
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
        if (debouncedInput.value.isEmpty()) {
            return@ExpandedFullScreenSearchBar Text(
                text = "Type something to search...",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }

        // show a loading indicator if the search is running
        if (runningSearch.value == true) {
            return@ExpandedFullScreenSearchBar CenteredLoader()
        }

        // empty state
        if (searchResults.value.isEmpty()) {
            return@ExpandedFullScreenSearchBar Text(
                text = "No results found.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }

        LazyColumn(
        ) {
            items(searchResults.value.size) { index ->
                val article = searchResults.value[index]

                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Log.i("WikiSearchBar", "Clicked on article: ${article.title}")

                            if (onResultClick != null) {
                                onResultClick(article)
                            } else {
                                Log.i("WikiSearchBar", "No onResultClick provided")
                            }
                        },
                    headlineContent = {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = article.parsedSnippet ?: "No preview available.",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = null,
                        )
                    },
                )
            }

            // TODO: load next page of results if they scroll past the 15th result
        }
    }
}

// TODO: remember ui state for back navigation
// TODO: back to top floating action button
