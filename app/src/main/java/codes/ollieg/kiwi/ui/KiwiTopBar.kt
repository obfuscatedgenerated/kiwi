package codes.ollieg.kiwi.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import codes.ollieg.kiwi.AppScreens
import codes.ollieg.kiwi.data.room.WikisViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

                AppScreens.Article -> {
                    // get the article name argument
                    // TODO: resolve to actual name from api, this is just the page slug
                    navArgs?.getString("article") ?: "Article"
                }

                AppScreens.ManageWikis -> "Wikis"
                AppScreens.ManageStorage -> "Offline storage"
                AppScreens.OtherSettings -> "Other settings"
                else -> "KiWi"
            }

            Text(text = title)
        },
        navigationIcon = {
            if (screen == AppScreens.Article) {
                // show a back button to go back to the wiki home
                IconButton(onClick = {
                    // pop the back stack to go back to the previous screen
                    navController.popBackStack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            } else {
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
        }
    )
}
