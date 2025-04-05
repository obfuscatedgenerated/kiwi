package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.data.room.ArticlesViewModel
import codes.ollieg.kiwi.data.room.WikisViewModel

@Composable
fun ScreenArticle(
    wikiId: Long,
    articleId: Long,
) {
    Log.i("ScreenArticle", "wikiId: $wikiId, articleId: $articleId")

    val context = LocalContext.current.applicationContext as Application

    val wikisViewModel = WikisViewModel(context)
    val articlesViewModel = ArticlesViewModel(context)

    val wiki = wikisViewModel.getById(wikiId)!!
    val article = articlesViewModel.getByIdLive(wiki, articleId).observeAsState()

//    if (article.value == null) {
//        Log.i("ScreenArticle", "Article is null")
//        return Text(
//            text = "Couldn't load article",
//            modifier = Modifier.padding(16.dp)
//        )
//    }

    if (article.value == null) {
        Text(
            text = "Loading article...",
            modifier = Modifier.padding(16.dp)
        )
        // TODO: some way to know if it started null or was posted. could check for state update ig?
    } else {
        var parsed = article.value!!.parsedContent!!

        // TODO: make headings bigger based on number of equals (could try annotated string?)

        LazyColumn {
            item {
                Text(
                    text = parsed,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// TODO: scroll to top button
