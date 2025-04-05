package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import codes.ollieg.kiwi.AppScreens
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

    val wiki = wikisViewModel.getByIdLive(wikiId).observeAsState()

    WikiSearchBar(
        wiki = wiki.value ?: return,
        onResultClick = { article ->
            // go to relevant article page using wiki id and its mediawiki page id
            navController.navigate("${AppScreens.Article.name}/${article.wikiId}/${article.pageId}")
        }
    )
}
