package codes.ollieg.kiwi

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import codes.ollieg.kiwi.ui.ArticleScreen
import codes.ollieg.kiwi.ui.ManageStorageScreen
import codes.ollieg.kiwi.ui.ManageWikisScreen
import codes.ollieg.kiwi.ui.OtherSettingsScreen
import codes.ollieg.kiwi.ui.WikiHomeScreen

@Composable
fun KiwiNavHost(
    navController: NavHostController,
    padding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = "${AppScreens.WikiHome.name}/1", // start on wikipedia home screen (user can't delete wikipedia)
        modifier = Modifier.padding(padding)
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