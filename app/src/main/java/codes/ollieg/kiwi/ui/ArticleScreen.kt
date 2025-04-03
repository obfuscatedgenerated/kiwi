package codes.ollieg.kiwi.ui

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ArticleScreen(
    wikiId: String,
    articleId: String,
) {
    Log.i("ArticleScreen", "wikiId: $wikiId, articleId: $articleId")
    Text(
        text = "Article Screen for $wikiId - $articleId",
        modifier = Modifier.padding(16.dp)
    )
}
