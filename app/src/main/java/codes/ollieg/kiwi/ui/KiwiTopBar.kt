package codes.ollieg.kiwi.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.net.toUri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import codes.ollieg.kiwi.AppScreens
import codes.ollieg.kiwi.data.room.ArticlesViewModel
import codes.ollieg.kiwi.data.room.WikisViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleSpecificTopBar(
    navController: NavController,
    navState: NavBackStackEntry? = navController.currentBackStackEntryAsState().value,
    wikisViewModel: WikisViewModel,
) {
    val navArgs = navState?.arguments

    val articlesViewModel = ArticlesViewModel(wikisViewModel.getApplication())

    // get the article title from the api and article id argument
    val articleId = navArgs?.getLong("article_id")!!
    val wikiId = navArgs?.getLong("wiki_id")!!

    // should update automatically from cache, so no need to coordinate with the data fetching mechanism, it'll just show when it's ready
    val article = articlesViewModel.repo.getByIdCachedLive(wikisViewModel.getById(wikiId)!!, articleId).observeAsState()

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Text(
                text = article.value?.title ?: "Loading...",
                textAlign = TextAlign.Left,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            // back button to go back to the previous screen
            IconButton(onClick = {
                // pop the back stack to go back to the previous screen
                navController.popBackStack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            // TODO: make functional

            IconButton(
                onClick = {}
            ) {
                // use filled star icon for starred articles, outlined for non-starred
                // n.b. it isn't just outlined/filled. starborder and star are different icons
                // using outlined and filled properly still for compatibility in case that changes
                val starIcon = if (article.value?.starred == true) {
                    Icons.Filled.Star
                } else {
                    Icons.Outlined.StarBorder
                }

                Icon(
                    starIcon,
                    contentDescription = "Add article to starred list",
                )
            }

            IconButton(
                onClick = {
                    val context = wikisViewModel.getApplication() as Context

                    // if no url is defined on the article, show a toast
                    val url = article.value?.pageUrl
                    if (url == null) {
                        Toast.makeText(
                            context,
                            "Sorry, couldn't find a URL for this article.\nPlease refresh or try again later.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@IconButton
                    }

                    // open the article in the browser with an intent
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = url.toUri()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    context.startActivity(intent)
                }
            ) {
                Icon(
                    Icons.Outlined.Language,
                    contentDescription = "Open article in browser",
                )
            }

            IconButton(
                onClick = {}
            ) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = "Share article",
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiwiTopBar(
    navController: NavController,
    wikisViewModel: WikisViewModel,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope(),
) {
    // get navController context as state so it updates
    val navState by navController.currentBackStackEntryAsState()
    val navRoute = navState?.destination?.route
    val navArgs = navState?.arguments

    // get the current screen from the route
    val screen =
        navRoute?.substringBefore("/")?.let { AppScreens.valueOf(it) }

    // article screen has special action buttons, so its simpler to override the whole top bar
    if (screen == AppScreens.Article) {
        return ArticleSpecificTopBar(
            navController = navController,
            navState = navState,
            wikisViewModel = wikisViewModel
        )
    }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            // decide the title based on the current screen
            val title = when (screen) {
                AppScreens.WikiHome -> {
                    // get the wiki name from the id argument
                    val wikiId = navArgs?.getLong("wiki_id")!!
                    val wiki by wikisViewModel.getByIdLive(wikiId).observeAsState()
                    wiki?.name ?: "Loading..."
                }

//                AppScreens.Article -> {
//                    (top bar now completely overridden by ArticleSpecificTopBar)
//                }

                AppScreens.ManageWikis -> "Wikis"
                AppScreens.ManageStorage -> "Offline storage"
                AppScreens.OtherSettings -> "Other settings"
                else -> "KiWi"
            }

            Text(text = title, textAlign = TextAlign.Left)
        },
        navigationIcon = {
            // hamburger icon to open nav drawer
            IconButton(onClick = {
                // toggle the drawer (uses a coroutine)
                scope.launch {
                    if (drawerState.isClosed) {
                        drawerState.open()
                    } else {
                        drawerState.close()
                    }
                }
            }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }
    )
}
