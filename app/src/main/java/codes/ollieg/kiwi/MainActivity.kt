package codes.ollieg.kiwi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import codes.ollieg.kiwi.ui.theme.KiWiTheme

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
            KiWiTheme {
                val navController = rememberNavController()

                Scaffold(topBar = {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        title = {
                            // get navController context as state so it updates
                            val context by navController.currentBackStackEntryAsState()

                            // get the current route from the navController
                            val route = context?.destination?.route
                            val arguments = context?.arguments

                            // get the current screen from the route
                            val screen = route?.substringBefore("/")?.let { AppScreens.valueOf(it) }

                            // decide the title based on the current screen
                            val title = when (screen) {
                                AppScreens.WikiHome -> {
                                   // get the wiki name argument
                                   arguments?.getString("wiki") ?: "Wiki"
                                }
                                AppScreens.Article -> {
                                    // get the article name argument
                                    arguments?.getString("article") ?: "Article"
                                }
                                AppScreens.ManageWikis -> "Manage Wikis"
                                AppScreens.ManageStorage -> "Manage Storage"
                                AppScreens.OtherSettings -> "Other Settings"
                                else -> "KiWi"
                            }

                            Text(text = title)
                        },
                    )
                }) { innerPadding ->//pass the innerPadding to avoid the content of the Scaffold overlapping with the TopAppBar
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
                            Greeting(context.arguments?.getString("wiki") ?: "No wiki provided")
                        }

                        composable(route = "${AppScreens.Article.name}/{wiki}/{article}", arguments = listOf(
                            navArgument(name = "wiki") {
                                type = NavType.StringType
                            },
                            navArgument(name = "article") {
                                type = NavType.StringType
                            }
                        )) { context ->
                            val article = context.arguments?.getString("article") ?: "No article provided"
                            val wiki = context.arguments?.getString("wiki") ?: "No wiki provided"
                            Greeting("$article from $wiki")
                        }

                        composable(route = AppScreens.ManageWikis.name) {

                        }

                        composable(route = AppScreens.ManageStorage.name) {

                        }

                        composable(route = AppScreens.OtherSettings.name) {

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KiWiTheme {
        Greeting("Android")
    }
}