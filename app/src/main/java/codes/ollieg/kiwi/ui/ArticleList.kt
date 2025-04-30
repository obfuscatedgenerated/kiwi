package codes.ollieg.kiwi.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.R
import codes.ollieg.kiwi.data.room.Article
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

// 3rd party library used in this file: coil
// purpose: to load images of an unknown format from a stored bytearray efficiently
// https://coil-kt.github.io/coil/
// https://github.com/coil-kt/coil/blob/main/LICENSE.txt

@Composable
fun ArticleList(
    articles: List<Article>,
    useThumbnails: Boolean = true,
    onResultClick: ((Article) -> Unit)? = null,
) {
    LazyColumn(
    ) {
        items(articles.size) { index ->
            val article = articles[index]

            // memoise thumbnail as bytearrays are not comparable (leads to repaints if not done)
            val thumbnail = remember { article.thumbnail }

            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Log.i("ArticleList", "Clicked on article: ${article.title}")

                        if (onResultClick != null) {
                            onResultClick(article)
                        } else {
                            Log.i("ArticleList", "No onResultClick provided")
                        }
                    },
                headlineContent = {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                supportingContent = {
                    Text(
                        text = article.parsedSnippet
                            ?: stringResource(R.string.no_preview_available),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = null,
                    )
                },
                leadingContent = {
                    if (!useThumbnails) {
                        return@ListItem
                    }

                    if (thumbnail === null) {
                        // no thumbnail, show placeholder
                        Icon(
                            Icons.AutoMirrored.Filled.Article,
                            contentDescription = null,
                            modifier = Modifier
                                .height(64.dp)
                                .width(64.dp)
                        )
                    }

                    // display thumbnail from stored bytearray using coil
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(thumbnail)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Thumbnail for ${article.title}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(64.dp)
                            .width(64.dp)
                    )
                }
            )
        }
    }
}