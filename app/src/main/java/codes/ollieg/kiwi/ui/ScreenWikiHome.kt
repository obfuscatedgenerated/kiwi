package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import codes.ollieg.kiwi.AppScreens
import codes.ollieg.kiwi.R
import codes.ollieg.kiwi.data.isLoggedInToMediawiki
import codes.ollieg.kiwi.data.logInToMediawiki
import codes.ollieg.kiwi.data.room.ArticlesViewModel
import codes.ollieg.kiwi.data.room.WikisViewModel
import io.ktor.util.valuesOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenWikiHome(
    wikiId: Long,
    navController: NavController
) {
    Log.i("ScreenWikiHome", "wikiId: $wikiId")

    val context = LocalContext.current.applicationContext as Application

    val wikisViewModel = WikisViewModel(context)
    val articlesViewModel = ArticlesViewModel(context)

    val wiki = wikisViewModel.getByIdLive(wikiId).observeAsState()

    if (wiki.value == null) {
        return
    }

    var authenticated by remember { mutableStateOf<Boolean?>(false) }

    // if wiki changes, check authentication again
    LaunchedEffect(wiki.value) {
        authenticated = false

        // log into the api if required
        if (wiki.value!!.authUsername != "" && wiki.value!!.authPassword != "") {
            Log.i("ScreenWikiHome", "Auth details found for ${wiki.value!!.name}, logging in...")

            CoroutineScope(Dispatchers.IO).launch {
                if (isLoggedInToMediawiki(wiki.value!!.apiUrl, wiki.value!!.authUsername)) {
                    Log.i(
                        "ScreenWikiHome",
                        "Already logged in to ${wiki.value!!.name}. No action needed."
                    )
                    authenticated = true
                } else {
                    Log.i("ScreenWikiHome", "Logging in to ${wiki.value!!.name}...")

                    try {
                        logInToMediawiki(
                            wiki.value!!.apiUrl,
                            wiki.value!!.authUsername,
                            wiki.value!!.authPassword
                        )
                        authenticated = true
                    } catch (e: Exception) {
                        Log.e("ScreenWikiHome", "Error logging in to ${wiki.value!!.name}: ${e.message}")
                        authenticated = null
                    }
                }
            }
        } else {
            Log.i("ScreenWikiHome", "No auth details found for ${wiki.value!!.name}. No action needed.")
            authenticated = true
        }
    }

    // wait for authentication to finish
    if (authenticated == false) {
        return CenteredLoader()
    }

    // if authentication fails, show error message
    if (authenticated == null) {
        return Text(
            text = stringResource(R.string.error_logging_in_to_wiki, wiki.value!!.name),
            modifier = Modifier.padding(16.dp)
        )
    }

    val starredArticles = articlesViewModel.getStarredByWikiLive(wiki.value!!).observeAsState()

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WikiSearchBar(
            wiki = wiki.value ?: return,
            onResultClick = { article ->
                // go to relevant article page using wiki id and its mediawiki page id
                navController.navigate("${AppScreens.Article.name}/${article.wikiId}/${article.pageId}")
            }
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = stringResource(R.string.starred_articles),
            style = MaterialTheme.typography.titleLarge,
        )

        if (starredArticles.value == null) {
            CenteredLoader()
        } else if (starredArticles.value!!.isEmpty()) {
            Text(
                text = stringResource(R.string.no_starred_articles),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            ArticleList(
                articles = starredArticles.value!!,
                onResultClick = { article ->
                    // go to relevant article page using wiki id and its mediawiki page id
                    navController.navigate("${AppScreens.Article.name}/${article.wikiId}/${article.pageId}")
                }
            )
        }
    }
}
