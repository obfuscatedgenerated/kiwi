package codes.ollieg.kiwi.ui

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codes.ollieg.kiwi.R
import codes.ollieg.kiwi.data.PreferencesViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlin.text.split


val SECTION_DEBUG = false

@Composable
fun ArticleContent(
    parsedContent: String?,
    thumbnail: ByteArray?,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    if (parsedContent == null) {
        return Text(
            text = stringResource(R.string.couldnt_load_article),
            modifier = Modifier.padding(16.dp)
        )
    }

    val context = LocalContext.current.applicationContext
    val preferencesViewModel = PreferencesViewModel(context as Application)

    val fontFamilyState by preferencesViewModel.fontFamily.observeAsState()
    val lineHeightState by preferencesViewModel.lineHeight.observeAsState()

    val sections = parsedContent.split("\n\n")

    // memoise thumbnail as bytearrays are not comparable (leads to repaints if not done)
    val remThumbnail = remember { thumbnail }
    var overlayShowing by remember { mutableStateOf(false) }

    LazyColumn (
        modifier = modifier,
        state = lazyListState,
    ) {
        if (remThumbnail != null) {
            item {
                val context = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(remThumbnail)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                        .clickable(onClick = {
                            // open image in fullscreen
                            overlayShowing = true
                        }),
                )
            }
        }
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
                    text = strippedParagraph.substring(
                        headingLevel + 1,
                        strippedParagraph.length - headingLevel - 1
                    )

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
                    modifier = Modifier.padding(
                        bottom = bottomPadding,
                        top = topPadding,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    lineHeight = lineHeightState?.sp ?: 24.sp,
                    fontFamily = fontFamilyState,
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

    // full screen image overlay
    if (overlayShowing) {
        Scaffold (
            containerColor = Color.Black.copy(alpha = 0.8f),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        Icons.Default.Close,
                        tint = Color.White,
                        contentDescription = stringResource(R.string.overlay_close),
                        modifier = Modifier
                            .clickable {
                                overlayShowing = false
                            }
                            .padding(8.dp)
                    )
                }
            }
        ) { paddingValues ->
            val context = LocalContext.current
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(remThumbnail)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize(0.9f)
                )
            }
        }
    }
}