package codes.ollieg.kiwi.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import codes.ollieg.kiwi.data.room.Article

@Composable
fun ArticleList(
    articles: List<Article>,
    onResultClick: ((Article) -> Unit)? = null,
) {
    LazyColumn(
    ) {
        items(articles.size) { index ->
            val article = articles[index]

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
                        text = article.parsedSnippet ?: "No preview available.",
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

                // TODO: support thumbnails if available
            )
        }
    }
}