package codes.ollieg.kiwi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import codes.ollieg.kiwi.data.room.WikisViewModel
import codes.ollieg.kiwi.ui.ArticleScreen
import codes.ollieg.kiwi.ui.ManageStorageScreen
import codes.ollieg.kiwi.ui.ManageWikisScreen
import codes.ollieg.kiwi.ui.NavDrawer
import codes.ollieg.kiwi.ui.OtherSettingsScreen
import codes.ollieg.kiwi.ui.WikiHomeScreen
import codes.ollieg.kiwi.ui.theme.KiWiTheme
import kotlinx.coroutines.launch


enum class AppScreens {
    WikiHome,
    Article,
    ManageWikis,
    ManageStorage,
    OtherSettings,
}

class ConnectionChangeReceiver : BroadcastReceiver {
    var lastOnlineValue: Boolean? = null

    fun checkOnline(context: Context): Boolean {
        // check if the device is connected to the internet
        // this api is deprecated, but i couldn't find another way that works nicely with broadcast receivers
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    constructor(context: Context) : super() {
        // set initial value for lastOnlineValue
        lastOnlineValue = checkOnline(context)
        Log.i("ConnectionChangeReceiver", "Initial connection state: $lastOnlineValue")
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onReceive(context: Context, intent: Intent) {
        val isOnline = checkOnline(context)
        Log.i("ConnectionChangeReceiver", "Connection state event: $isOnline")

        if (isOnline == lastOnlineValue) {
            // no change in connection state, do nothing
            Log.i("ConnectionChangeReceiver", "No change in connection state, ignoring event.")
            return
        }

        lastOnlineValue = isOnline

        if (!isOnline) {
            Toast.makeText(context, "Your device just went offline.\n" +
                    "Only offline articles are available.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Back online!", Toast.LENGTH_LONG).show()
        }
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // register receiver to show toast when device connects to or loses connection to the internet
        val connChangeReceiver = ConnectionChangeReceiver(this)
        val connChangeReceiverFlags = ContextCompat.RECEIVER_NOT_EXPORTED
        val connChangeFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        ContextCompat.registerReceiver(this, connChangeReceiver, connChangeFilter, connChangeReceiverFlags)

        setContent {
            val navController = rememberNavController()

            val wikisViewModel = WikisViewModel(this.application)

            // part of docs on how to use the drawer, heavily adapted to a component
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            KiWiTheme {
                NavDrawer (
                    navController = navController,
                    wikisViewModel = wikisViewModel,
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
                                    // get navstate here too, just in topbar to avoid useless repainting
                                    val navState by navController.currentBackStackEntryAsState()
                                    val navRoute = navState?.destination?.route
                                    val navArgs = navState?.arguments

                                    // get the current screen from the route
                                    val screen =
                                        navRoute?.substringBefore("/")?.let { AppScreens.valueOf(it) }

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
                    ) { innerPadding -> //pass the innerPadding to avoid the content of the Scaffold overlapping with the TopAppBar
                        NavHost(
                            navController = navController,
                            startDestination = "${AppScreens.WikiHome.name}/1",
                            modifier = Modifier.padding(innerPadding)// use the innerPadding
                        ) {
                            // wiki home screen
                            composable(
                                route = "${AppScreens.WikiHome.name}/{wiki_id}", arguments = listOf(
                                    navArgument(name = "wiki_id") {
                                        type = NavType.LongType
                                    }
                                )) { context ->

                                val wikiId = context.arguments?.getLong("wiki_id")!!
                                WikiHomeScreen(wikiId)
                            }

                            // article screen
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
                                val wikiId = context.arguments?.getLong("wiki_id")!!

                                ArticleScreen(
                                    wikiId = wikiId,
                                    articleId = articleId
                                )
                            }

                            // settings screens

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