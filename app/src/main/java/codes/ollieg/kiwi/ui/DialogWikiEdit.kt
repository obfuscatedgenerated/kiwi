package codes.ollieg.kiwi.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.R
import codes.ollieg.kiwi.data.room.Wiki
import codes.ollieg.kiwi.data.room.WikisViewModel

fun saveWiki(wiki: Wiki, wikisViewModel: WikisViewModel): Boolean {
    // tries to update/insert the wiki into the database but will return false if it fails
    // note that the form validation should check if the name is already in use or this will return false (unique constraint)
    try {
        wikisViewModel.upsert(wiki)
        return true
    } catch (e: Error) {
        Log.e("DialogWikiEdit", "Error saving wiki: ${e.message}")
        return false
    }
}

fun deleteWiki(wikiId: Long, wikisViewModel: WikisViewModel): Boolean {
    // tries to delete the wiki from the database but will return false if it fails
    // note: not allowed to delete id 1 (wikipedia), this will return false if attempted
    // the user can still edit the wiki though, but by default it is wikipedia
    try {
        wikisViewModel.deleteById(wikiId)
        return true
    } catch (e: Error) {
        Log.e("DialogWikiEdit", "Error deleting wiki: ${e.message}")
        return false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogWikiEdit(
    wikiId: Long?, // if not specified, this will be an add wiki dialog
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current.applicationContext as Application
    val wikisViewModel = WikisViewModel(context)

    val wiki = if (wikiId != null) {
        wikisViewModel.getById(wikiId)!!
    } else {
        // make a temporary wiki object when adding wikis, which will be inserted into the database when the user clicks save
        Wiki(
            id = 0, // room should automatically assign an id
            name = "",
            apiUrl = "",
            authUsername = "",
            authPassword = ""
        )
    }

    // wiki object fields copied into state to avoid weirdness when editing form fields (reverts to initial value due to it being reset each paint)
    // to be copied back into the wiki object when saving
    var wikiApiUrl by remember { mutableStateOf(wiki.apiUrl) }
    var wikiName by remember { mutableStateOf(wiki.name) }
    var wikiAuthUsername by remember { mutableStateOf(wiki.authUsername) }
    var wikiAuthPassword by remember { mutableStateOf(wiki.authPassword) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    // now using bottom sheet rather than dialog so it can be swiped down

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RectangleShape, // gets rid of the rounded corners to make it look more natural
        dragHandle = null, // as well as hiding the drag handle
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier
                .safeDrawingPadding()
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // separate boxes for left and right floated content
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.close_modal),
                            )
                        }

                        // if wikiId is null, this is an add wiki dialog
                        val title = if (wikiId == null) {
                            "Add wiki"
                        } else {
                            "Edit wiki"
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    // right floated content
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val newWiki = wiki.copy(
                                    apiUrl = wikiApiUrl,
                                    name = wikiName,
                                    authUsername = wikiAuthUsername,
                                    authPassword = wikiAuthPassword
                                )

                                val success = saveWiki(newWiki, wikisViewModel)

                                if (success) {
                                    onDismissRequest()
                                } else {
                                    /* TODO: show error message */
                                }
                            },
                            colors = ButtonDefaults.textButtonColors()
                        ) {
                            Text("Save")
                        }
                    }
                }
            },
            bottomBar = {
                // compute bottom padding, reducing if keyboard is open
                // it'll be 72dp if keyboard is closed, and 16dp if it's open
                var bottomPadding = 56.dp - WindowInsets.ime.getBottom(LocalDensity.current).dp
                if (bottomPadding < 0.dp) {
                    bottomPadding = 0.dp
                }
                bottomPadding += 16.dp

                // status icon and text ("looks good" or error e.g. "missing x field", "invalid url", "couldnt reach api", "not a mediawiki api")
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = bottomPadding)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    Text(
                        text = stringResource(R.string.your_config_looks_good),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding)
            ) {
                val formFieldHorizontalPadding = 16.dp
                val formFieldBottomPadding = 12.dp

                // main details form

                OutlinedTextField(
                    value = wikiApiUrl,
                    onValueChange = { value ->
                        wikiApiUrl = value
                    },
                    label = { Text(stringResource(R.string.api_url)) },
                    placeholder = { Text(stringResource(R.string.placeholder_api_url)) },
                    supportingText = {
                        Text(
                            text = stringResource(R.string.subtext_api_url),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp,
                            bottom = formFieldBottomPadding,
                            start = formFieldHorizontalPadding,
                            end = formFieldHorizontalPadding
                        ),
                )

                // note: this will get set automatically when trying the api if empty
                OutlinedTextField(
                    value = wikiName,
                    onValueChange = { value ->
                        wikiName = value
                    },
                    label = { Text(stringResource(R.string.wiki_name)) },
                    placeholder = { Text(stringResource(R.string.placeholder_wiki_name)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = formFieldBottomPadding,
                            start = formFieldHorizontalPadding,
                            end = formFieldHorizontalPadding
                        ),
                )

                Spacer(modifier = Modifier.padding(24.dp))

                // authentication form

                Text(
                    text = stringResource(R.string.auth_section),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = wikiAuthUsername,
                    onValueChange = { value ->
                        wikiAuthUsername = value
                    },
                    label = { Text(stringResource(R.string.username)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = formFieldBottomPadding,
                            start = formFieldHorizontalPadding,
                            end = formFieldHorizontalPadding
                        ),
                )

                OutlinedTextField(
                    value = wikiAuthPassword,
                    onValueChange = { value ->
                        wikiAuthPassword = value
                    },
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = formFieldBottomPadding,
                            start = formFieldHorizontalPadding,
                            end = formFieldHorizontalPadding
                        ),
                )

                Spacer(modifier = Modifier.padding(24.dp))

                // right aligned delete button if wiki id is not 1 or null
                // user isn't allowed to delete wiki 1, and null is a new wiki so it can't be deleted before it's saved
                if (wikiId != null && wikiId != 1L) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                /* TODO: confirm delete */
                                val success = deleteWiki(wikiId, wikisViewModel)
                                if (success) {
                                    onDismissRequest()
                                } else {
                                    /* TODO: show error message */
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(),

                            ) {
                            Text(
                                text = stringResource(R.string.delete_wiki),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}