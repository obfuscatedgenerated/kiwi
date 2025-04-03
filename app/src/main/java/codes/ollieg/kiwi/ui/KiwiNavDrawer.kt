package codes.ollieg.kiwi.ui

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import codes.ollieg.kiwi.AppScreens
import codes.ollieg.kiwi.R
import codes.ollieg.kiwi.data.room.WikisViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun KiwiNavDrawerWikiItem(
    wikiId: Long,
    wikisViewModel: WikisViewModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wiki by wikisViewModel.getByIdLive(wikiId).observeAsState()

    NavigationDrawerItem(
        icon = {
            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
        },
        label = { Text(text = wiki?.name ?: "Loading...") },
        selected = selected,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun KiwiNavDrawer(
    navController: NavController,
    wikisViewModel: WikisViewModel,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable () -> Unit
) {
    // get navController context as state so it updates
    val navState by navController.currentBackStackEntryAsState()

    // get the current route from the navController
    val navRoute = navState?.destination?.route
    val navArgs = navState?.arguments

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Text("KiWi", modifier = Modifier.padding(16.dp))
                Spacer(modifier = Modifier.padding(4.dp))

                val subtitleModifier =
                    Modifier.padding(start = 24.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)

                Text(text = "Wikis", modifier = subtitleModifier)

                val itemModifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)

                // dynamically populate wikis list using the wikisViewModel and custom component
                val allWikis by wikisViewModel.allWikis.observeAsState()
                allWikis?.forEach { wiki ->
                    Log.i("NavDrawer", "Wiki: ${wiki.name}")
                    KiwiNavDrawerWikiItem(
                        wikiId = wiki.id,
                        wikisViewModel = wikisViewModel,

                        // check if the route is a wiki home and the wiki id matches
                        selected = (navRoute == "${AppScreens.WikiHome.name}/{wiki_id}" && navArgs?.getLong("wiki_id") == wiki.id),

                        onClick = {
                            // navigate to the wiki home screen
                            navController.navigate(AppScreens.WikiHome.name + "/${wiki.id}")
                            scope.launch {
                                drawerState.close()
                            }
                        },

                        modifier = itemModifier
                    )
                }

                // divider adjusted to be more transparent and take up less width
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )

                Text(text = "Settings", modifier = subtitleModifier)

                // TODO: use shared base component for these like done for wikis

                NavigationDrawerItem(
                    modifier = itemModifier,
                    icon = {
                        Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null)
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
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
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
        // render the main content of the screen passed in
        content()
    }
}