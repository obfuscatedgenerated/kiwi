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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import codes.ollieg.kiwi.R
import codes.ollieg.kiwi.data.getSiteInfo
import codes.ollieg.kiwi.data.logInToMediawiki
import codes.ollieg.kiwi.data.room.Wiki
import codes.ollieg.kiwi.data.room.WikisViewModel

private fun saveWiki(wiki: Wiki, wikisViewModel: WikisViewModel): Boolean {
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

private fun deleteWiki(wikiId: Long, wikisViewModel: WikisViewModel): Boolean {
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

// returns the error message resource id if the config is invalid, or null if it is valid
private suspend fun checkConfig(
    apiUrl: String,
    authUsername: String,
    authPassword: String
): Int? {
    // basic sanity checks
    if (apiUrl == "") {
        Log.e("DialogWikiEdit", "Empty api url")
        return R.string.config_empty_url
    }

    val apiUrlValid = apiUrl.startsWith("https://") || apiUrl.startsWith("http://")
    if (!apiUrlValid) {
        Log.e("DialogWikiEdit", "Invalid api url: $apiUrl")
        return R.string.config_invalid_url_protocol
    }

    val apiEndsWith = apiUrl.endsWith("/api.php")
    if (!apiEndsWith) {
        Log.e("DialogWikiEdit", "Invalid api url: $apiUrl")
        return R.string.config_no_api_php
    }

    // try logging in first, as some wikis strictly require authentication
    if (authUsername != "" || authPassword != "") {
        try {
            logInToMediawiki(
                apiUrl,
                authUsername,
                authPassword
            )
        } catch (e: Exception) {
            Log.e("DialogWikiEdit", "Error logging in: ${e.message}")
            return R.string.config_login_failed
        }
    }

    // try fetching the site info to check if the api is valid
    try {
        val siteInfo = getSiteInfo(apiUrl)
        Log.i("DialogWikiEdit", "Site info: $siteInfo")

        // require TextExtracts and PageImages
        val hasTextExtracts = siteInfo.extensions.contains("TextExtracts")
        val hasPageImages = siteInfo.extensions.contains("PageImages")

        if (!hasTextExtracts) {
            Log.e("DialogWikiEdit", "API does not support TextExtracts")
            return R.string.config_no_textextracts
        }

        if (!hasPageImages) {
            Log.e("DialogWikiEdit", "API does not support PageImages")
            return R.string.config_no_pageimages
        }
    } catch (e: Exception) {
        Log.e("DialogWikiEdit", "Error fetching site info: ${e.message}")
        return R.string.config_cannot_access
    }

    return null
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

    var deleteDialogShow by remember { mutableStateOf(false) }

    // wiki object fields copied into state to avoid weirdness when editing form fields (reverts to initial value due to it being reset each paint)
    // to be copied back into the wiki object when saving
    var wikiApiUrl by remember { mutableStateOf(wiki.apiUrl) }
    var wikiName by remember { mutableStateOf(wiki.name) }
    var wikiAuthUsername by remember { mutableStateOf(wiki.authUsername) }
    var wikiAuthPassword by remember { mutableStateOf(wiki.authPassword) }

    var wikiConfigErrorId by remember { mutableStateOf<Int?>(null) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    // now using bottom sheet rather than dialog so it can be swiped down

    // focus traversal order for the form fields. semantics is only for talkback
    val (first, second, third, fourth, fifth, sixth) = remember { FocusRequester.createRefs() }

    // run check when config changes
    // TODO debounce this
    LaunchedEffect(wikiApiUrl, wikiAuthUsername, wikiAuthPassword) {
        wikiConfigErrorId = checkConfig(
            apiUrl = wikiApiUrl,
            authUsername = wikiAuthUsername,
            authPassword = wikiAuthPassword
        )

        // if no name is set, use the site name as the wiki name
        if (wikiConfigErrorId === null && wikiName === "") {
            try {
                val siteInfo = getSiteInfo(wikiApiUrl)
                wikiName = siteInfo.siteName
            } catch (e: Exception) {
                Log.e("DialogWikiEdit", "Error fetching site info: ${e.message}")
            }
        }
    }

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
                            colors = ButtonDefaults.textButtonColors(),
                            modifier = Modifier
                                .semantics {
                                    traversalIndex = 4f
                                }
                                .focusRequester(fifth)
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
                    // tick for valid config, cross for invalid
                    if (wikiConfigErrorId === null) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    // show looks good if the config is valid, otherwise show the error message
                    val resourceId = wikiConfigErrorId ?: R.string.config_ok
                    val color = if (wikiConfigErrorId == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    Text(
                        text = stringResource(resourceId),
                        style = MaterialTheme.typography.bodyLarge,
                        color = color,
                    )
                }
            }
        ) { padding ->
            if (deleteDialogShow) {
                AlertDialog(
                    title = {
                        Text(
                            text = stringResource(R.string.delete_wiki_dialog_title, wiki.name)
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(
                                R.string.delete_wiki_dialog_text,
                            )
                        )
                    },
                    onDismissRequest = {
                        deleteDialogShow = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                Log.i("DialogWikiEdit", "Delete confirmed")

                                // TODO: check success
                                deleteWiki(wiki.id, wikisViewModel)

                                // dismiss the dialog and the parent edit dialog
                                deleteDialogShow = false
                                onDismissRequest()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.delete))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                Log.i("DialogWikiEdit", "Delete cancelled")
                                deleteDialogShow = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp,
                            bottom = formFieldBottomPadding,
                            start = formFieldHorizontalPadding,
                            end = formFieldHorizontalPadding
                        )
                        .semantics {
                            traversalIndex = 0f
                        }
                        .focusRequester(first)
                )

                OutlinedTextField(
                    value = wikiName,
                    onValueChange = { value ->
                        wikiName = value
                    },
                    label = { Text(stringResource(R.string.wiki_name)) },
                    placeholder = { Text(stringResource(R.string.placeholder_wiki_name)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = formFieldBottomPadding,
                            start = formFieldHorizontalPadding,
                            end = formFieldHorizontalPadding
                        )
                        .semantics {
                            traversalIndex = 1f
                        }
                        .focusRequester(second)
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
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = formFieldBottomPadding,
                            start = formFieldHorizontalPadding,
                            end = formFieldHorizontalPadding
                        )
                        .semantics {
                            traversalIndex = 2f
                        }
                        .focusRequester(third)
                )

                OutlinedTextField(
                    value = wikiAuthPassword,
                    onValueChange = { value ->
                        wikiAuthPassword = value
                    },
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = formFieldBottomPadding,
                            start = formFieldHorizontalPadding,
                            end = formFieldHorizontalPadding
                        )
                        .semantics {
                            traversalIndex = 3f
                        }
                        .focusRequester(fourth)
                )

                Spacer(modifier = Modifier.padding(24.dp))

                // right aligned delete button if wiki id is not 1 or null
                // user isn't allowed to delete wiki 1, and null is a new wiki so it can't be deleted before it's saved
                if (wikiId != null && wikiId != 1L) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                traversalIndex = 5f
                            }
                            .focusRequester(sixth)
                    ) {
                        Button(
                            onClick = {
                                deleteDialogShow = true
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