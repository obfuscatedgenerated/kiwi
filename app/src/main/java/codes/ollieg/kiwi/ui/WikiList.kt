package codes.ollieg.kiwi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.data.room.Wiki

@Composable
fun WikiList(
    wikis: List<Wiki>,
    modifier: Modifier = Modifier,
    subtexts: List<String>? = null,
    button: @Composable ((Wiki) -> Unit)? = null,
) {
    LazyColumn (
        modifier = modifier.fillMaxWidth()
    ) {
        items(wikis.size) { index ->
            val wiki = wikis[index]
            val subtext = subtexts?.getOrNull(index)

            // TODO: adapt to listitem for consistency
            Row(
                modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column (
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = wiki.name,
                    )

                    if (subtext != null) {
                        Text(
                            text = subtext,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (button != null) {
                    button(wiki)
                }
            }
        }
    }
}