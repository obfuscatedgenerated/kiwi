package codes.ollieg.kiwi.ui

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.R
import codes.ollieg.kiwi.data.room.Article
import codes.ollieg.kiwi.data.room.ArticlesViewModel
import codes.ollieg.kiwi.data.room.WikisViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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

    var article: Article? by remember { mutableStateOf(null) }

    LaunchedEffect(articleId) {
        Log.i("ScreenArticle", "Loading article ONCE with id: $articleId")
        article = null

        CoroutineScope(Dispatchers.IO).launch {
            article = articlesViewModel.repo.getById(wiki, articleId)
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    if (article == null) {
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
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Scroll to top",
                        )
                    }
                }
            }
        ) { paddingValues ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true

                    CoroutineScope(Dispatchers.IO).launch {
                        Log.i("ScreenArticle", "Refreshing article with id: $articleId")

                        // explicitly skip cache to reload from api
                        article = articlesViewModel.repo.getById(wiki, articleId, true)
                    }
                }
            ) {
                var content = article?.parsedContent

                // change isRefreshing to false once loaded
                if (isRefreshing) {
                    isRefreshing = false
                }

                ArticleContent(
                    content,
                    article?.thumbnail,
                    lazyListState = lazyListState,
                    modifier = Modifier.padding(paddingValues).combinedClickable(
                        onClick = {},
                        onDoubleClick = {
                            // double tap to star
                            val articleValue = article ?: return@combinedClickable

                            Log.i("ScreenArticle", "Double tapped to star article: ${articleValue.title}")

                            val newArticle = articleValue.copy(
                                // toggle the starred state of the article
                                starred = !articleValue.starred
                            )

                            // update the article in the database
                            articlesViewModel.updateInCache(newArticle)

                            // show toast depending on the new state
                            val context = wikisViewModel.getApplication() as Context
                            val toastText = if (newArticle.starred) {
                                context.getString(R.string.starred_article, newArticle.title)
                            } else {
                                context.getString(R.string.unstarred_article, newArticle.title)
                            }

                            Toast.makeText(
                                context,
                                toastText,
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        indication = null,
                        interactionSource = null
                    )
                )
            }
        }
    }
}
