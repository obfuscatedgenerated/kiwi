package codes.ollieg.kiwi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import codes.ollieg.kiwi.ui.theme.KiWiTheme
import kotlinx.coroutines.launch

enum class AppScreens {
    WikiHome,
    Article,
    ManageWikis,
    ManageStorage,
    OtherSettings,
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // TODO: move stuff to custom layout using content: @Composable () -> Unit
            KiWiTheme {
                val navController = rememberNavController()

                // get navController context as state so it updates
                val navState by navController.currentBackStackEntryAsState()

                // get the current route from the navController
                val navRoute = navState?.destination?.route
                val navArgs = navState?.arguments

                // navdrawer code adapted from android docs
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerContent = {
                        ModalDrawerSheet {
                            Text("KiWi", modifier = Modifier.padding(16.dp))
                            Spacer(modifier = Modifier.padding(4.dp))

                            val subtitleModifier = Modifier.padding(start = 24.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)

                            Text(text="Wikis", modifier = subtitleModifier)

                            val itemModifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)

                            // TODO: generate dynamically
                            NavigationDrawerItem(
                                modifier = itemModifier,
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_menu_book_24),
                                        contentDescription = null
                                    )
                                },
                                label = { Text(text = "Wikipedia") },
                                selected = (navRoute?.startsWith(AppScreens.WikiHome.name) == true && navArgs?.getString("wiki") == "wikipedia"),
                                onClick = {
                                    // navigate to the wiki home screen
                                    navController.navigate("${AppScreens.WikiHome.name}/wikipedia")
                                    scope.launch {
                                        drawerState.close()
                                    }
                                }
                            )

                            // divider adjusted to be more transparent and take up less width
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )

                            Text(text="Settings", modifier = subtitleModifier)

                            // TODO: use shared base component for these

                            NavigationDrawerItem(
                                modifier = itemModifier,
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.library_books),
                                        contentDescription = null
                                    )
                                },
                                label = { Text(text = "Manage wikis") },
                                selected = (navRoute == AppScreens.ManageWikis.name),
                                onClick = {
                                    // navigate to the manage wikis screen
                                    navController.navigate(AppScreens.ManageWikis.name)
                                    scope.launch {
                                        drawerState.close()
                                    }
                                }
                            )

                            NavigationDrawerItem(
                                modifier = itemModifier,
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.hard_disk),
                                        contentDescription = null
                                    )
                                },
                                label = { Text(text = "Manage offline storage") },
                                selected = (navRoute == AppScreens.ManageStorage.name),
                                onClick = {
                                    // navigate to the manage storage screen
                                    navController.navigate(AppScreens.ManageStorage.name)
                                    scope.launch {
                                        drawerState.close()
                                    }
                                }
                            )

                            NavigationDrawerItem(
                                modifier = itemModifier,
                                icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                                label = { Text(text = "Other settings") },
                                selected = (navRoute == AppScreens.OtherSettings.name),
                                onClick = {
                                    // navigate to the other settings screen
                                    navController.navigate(AppScreens.OtherSettings.name)
                                    scope.launch {
                                        drawerState.close()
                                    }
                                }
                            )
                        }
                    },
                    drawerState = drawerState
                ) {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.primary
                                ),
                                title = {
                                    // get the current screen from the route
                                    val screen =
                                        navRoute?.substringBefore("/")?.let { AppScreens.valueOf(it) }

                                    // decide the title based on the current screen
                                    val title = when (screen) {
                                        AppScreens.WikiHome -> {
                                            // get the wiki name argument
                                            // TODO: resolve to actual name from datastore, this is just the id
                                            navArgs?.getString("wiki") ?: "Wiki"
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
                    ) { innerPadding ->//pass the innerPadding to avoid the content of the Scaffold overlapping with the TopAppBar
                        NavHost(
                            navController = navController,
                            startDestination = "${AppScreens.WikiHome.name}/wikipedia",
                            modifier = Modifier.padding(innerPadding)// use the innerPadding
                        ) {
                            composable(
                                route = "${AppScreens.WikiHome.name}/{wiki}", arguments = listOf(
                                    navArgument(name = "wiki") {
                                        type = NavType.StringType
                                    }
                                )) { context ->

                                val wikiId = context.arguments?.getString("wiki")!!
                                WikiHomeScreen(wikiId)
                            }

                            composable(
                                route = "${AppScreens.Article.name}/{wiki}/{article}",
                                arguments = listOf(
                                    navArgument(name = "wiki") {
                                        type = NavType.StringType
                                    },
                                    navArgument(name = "article") {
                                        type = NavType.StringType
                                    }
                                )) { context ->

                                val articleId = context.arguments?.getString("article")!!
                                val wikiId = context.arguments?.getString("wiki")!!

                                ArticleScreen(
                                    wikiId = wikiId,
                                    articleId = articleId
                                )
                            }

                            composable(route = AppScreens.ManageWikis.name) {
                                ManageWikisScreen()
                            }

                            composable(route = AppScreens.ManageStorage.name) {
                                ManageStorageScreen()
                            }

                            composable(route = AppScreens.OtherSettings.name) {
                                OtherSettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}