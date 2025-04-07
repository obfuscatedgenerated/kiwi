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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import codes.ollieg.kiwi.AppScreens
import codes.ollieg.kiwi.data.room.ArticlesViewModel
import codes.ollieg.kiwi.data.room.WikisViewModel

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
            text = "Starred articles",
            style = MaterialTheme.typography.titleLarge,
        )

        if (starredArticles.value == null) {
            CenteredLoader()
        } else if (starredArticles.value!!.isEmpty()) {
            Text(
                text = "No starred articles... yet!",
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
