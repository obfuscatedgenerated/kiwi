package codes.ollieg.kiwi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
                Scaffold(topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primaryadd
                        ),
                        title = {
                            Text("KiWi")
                        },
                    )
                }) { innerPadding ->//pass the innerPadding to avoid the content of the Scaffold overlapping with the TopAppBar
                    val navController = rememberNavController()

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