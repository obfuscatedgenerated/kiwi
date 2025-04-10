package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.data.room.ArticlesViewModel
import codes.ollieg.kiwi.data.room.WikisViewModel
import kotlinx.coroutines.launch

@Composable
fun ScreenArticle(
    wikiId: Long,
    articleId: Long,
) {
    Log.i("ScreenArticle", "wikiId: $wikiId, articleId: $articleId")

    val context = LocalContext.current.applicationContext as Application
    val scope = rememberCoroutineScope()

    val wikisViewModel = WikisViewModel(context)
    val articlesViewModel = ArticlesViewModel(context)

    val wiki = wikisViewModel.getById(wikiId)!!
    val article = articlesViewModel.getByIdLive(wiki, articleId).observeAsState()

    // TODO: pull to refresh (use skipCache = true when loading article)

    if (article.value == null) {
        CenteredLoader()
        // TODO: might get stuck if cache value is also null, might need to return explcitly "false" when that happens or do a timeout
    } else {
        val lazyListState = rememberLazyListState()

        Scaffold (
            floatingActionButton = {
                // if the user is already near the top, don't show the button
                val visible by remember {
                    derivedStateOf {
                        lazyListState.firstVisibleItemIndex != 0 || lazyListState.firstVisibleItemScrollOffset >= 500
                    }
                }

                AnimatedVisibility(
                    visible,
                    enter = fadeIn(tween(750)),
                    exit = fadeOut(tween(750)),
                ) {
                    FloatingActionButton(
                        onClick = {
                            Log.i("ScreenArticle", "Scroll to top button clicked")

                            // animate to top in composition aware scope
                            scope.launch {
                                lazyListState.animateScrollToItem(0)
                            }
                        },
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Icon(
                            Icons.Default.VerticalAlignTop,
                            contentDescription = "Scroll to top",
                        )
                    }
                }
            }
        ) { paddingValues ->
            ArticleContent(
                article.value!!.parsedContent,
                lazyListState = lazyListState,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
