package codes.ollieg.kiwi.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import codes.ollieg.kiwi.data.room.Wiki

@Composable
fun WikiList(
    wikis: List<Wiki>,
    modifier: Modifier = Modifier,
    subtexts: List<String>? = null,
    button: @Composable ((Wiki) -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        items(wikis.size) { index ->
            val wiki = wikis[index]
            val subtext = subtexts?.getOrNull(index)

            ListItem(
                modifier = Modifier.fillMaxWidth(),
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
                headlineContent = {
                    Text(
                        text = wiki.name,
                    )
                },
                supportingContent = {
                    if (subtext != null) {
                        Text(
                            text = subtext,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                trailingContent = {
                    if (button != null) {
                        button(wiki)
                    }
                }
            )
        }
    }
}