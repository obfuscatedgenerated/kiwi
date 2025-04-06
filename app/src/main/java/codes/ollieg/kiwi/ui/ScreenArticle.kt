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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // TODO: pull to refresh (use skipCache = true when loading article)

    if (article.value == null) {
        CenteredLoader()
        // TODO: might get stuck if cache value is also null, might need to return explcitly "false" when that happens or do a timeout
    } else {
        var parsed = article.value!!.parsedContent

        if (parsed == null) {
            return Text(
                text = "Couldn't load article",
                modifier = Modifier.padding(16.dp)
            )
        }

        // TODO: make headings bigger based on number of equals (could try annotated string?)

        // double any newline characters to make paragraphs more readable
        // TODO: split paragraphs into separate text elements to make lazy loading more efficient
        parsed = parsed.replace("\n", "\n\n")

        LazyColumn {
            item {
                Text(
                    text = parsed,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 24.sp, // more readable TODO: configurable
                    fontFamily = FontFamily.Serif // TODO: connect to preference system
                )
            }
        }
    }
}

// TODO: scroll to top button
