package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codes.ollieg.kiwi.R
import codes.ollieg.kiwi.data.room.ArticlesViewModel
import codes.ollieg.kiwi.data.room.WikisViewModel

val SECTION_DEBUG = false

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
                text = stringResource(R.string.couldnt_load_article),
                modifier = Modifier.padding(16.dp)
            )
        }

        // TODO: make headings bigger based on number of equals (could try annotated string?)

        val sections = parsed.split("\n\n")

        LazyColumn {
            items(sections.size) { index ->
                val section = sections[index]
                val paragraphs = section.split("\n")

                // render paragraphs separately for efficient loading and clean separation
                for (paragraph in paragraphs) {
                    if (paragraph.isEmpty()) continue

                    var text = paragraph
                    var textStyle = MaterialTheme.typography.bodyLarge
                    var bottomPadding = 16.dp
                    var topPadding = 16.dp

                    // if the text of this paragraph is in the format =^n (.*) =^n then it is a heading format
                    val strippedParagraph = paragraph.replace("\n", "")
                    val isHeading = Regex("^=+ (.*) =+$").matches(strippedParagraph)

                    if (isHeading) {
                        // count the number of equals signs and strip them
                        // this assumes the number of equals signs is the same on both sides, which it should be from textextracts
                        val headingLevel = strippedParagraph.indexOf(" ")
                        text = strippedParagraph.substring(headingLevel + 1, strippedParagraph.length - headingLevel - 1)

                        // determine the textstyle to apply based on the heading level
                        // we can skip h1 as it is not usually added to the text (used as title) but still just treat it as h2
                        textStyle = when (headingLevel) {
                            1, 2 -> MaterialTheme.typography.headlineLarge
                            3 -> MaterialTheme.typography.headlineMedium
                            4 -> MaterialTheme.typography.headlineSmall
                            5 -> MaterialTheme.typography.titleLarge
                            6 -> MaterialTheme.typography.titleMedium
                            else -> MaterialTheme.typography.titleSmall
                        }

                        // disable bottom padding for headings, and set top padding based on level
                        bottomPadding = 0.dp
                        topPadding = when (headingLevel) {
                            1 -> 32.dp
                            2 -> 24.dp
                            else -> 16.dp
                        }

                        // TODO: connect to preference system
                    }

                    Text(
                        text = text,
                        modifier = Modifier.padding(bottom = bottomPadding, top = topPadding, start = 16.dp, end = 16.dp),
                        lineHeight = 24.sp, // more readable TODO: configurable
                        fontFamily = FontFamily.Serif, // TODO: connect to preference system
                        fontSize = textStyle.fontSize,
                    )

                    if (SECTION_DEBUG) {
                        // new paragraph
                        HorizontalDivider()
                    }
                }

                if (SECTION_DEBUG) {
                    // new section
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// TODO: scroll to top button
