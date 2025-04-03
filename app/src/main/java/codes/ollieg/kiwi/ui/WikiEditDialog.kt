package codes.ollieg.kiwi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiEditDialog(
    wikiId: Long?, // if not specified, this will be an add wiki dialog
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    // now using bottom sheet rather than dialog so it can be swiped down

    ModalBottomSheet (
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RectangleShape, // gets rid of the rounded corners to make it look more natural
        dragHandle = null, // as well as hiding the drag handle
    ) {
        Scaffold (
            modifier = Modifier.safeDrawingPadding().fillMaxSize().padding(horizontal = 8.dp),
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // separate boxes for left and right floated content
                    Row (
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Close modal",
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
                    Row (
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { /* TODO: save wiki */ },
                            colors = ButtonDefaults.textButtonColors()
                        ) {
                            Text("Save")
                        }
                    }
                }
            },
            bottomBar = {
                // status icon and text ("looks good" or error e.g. "missing x field", "invalid url", "couldnt reach api", "not a mediawiki api")

                Row (
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 72.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    Text(
                        text = "Your config looks good!",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        ) { padding ->
            Column (
                modifier = Modifier.padding(padding)
            ) {
                val formFieldHorizontalPadding = 16.dp
                val formFieldBottomPadding = 12.dp

                // main details form

                OutlinedTextField(
                    value = "",
                    onValueChange = { /* TODO: update wiki api url in temporary object */ },
                    label = { Text("API URL") },
                    placeholder = { Text("e.g. https://en.wikipedia.org/w/api.php") },
                    supportingText = {
                        Text(
                            text = "Usually found at https://example.com/w/api.php",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = formFieldBottomPadding,
                            start = formFieldHorizontalPadding,
                            end = formFieldHorizontalPadding
                        ),
                )

                // note: this will get set automatically when trying the api if empty
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* TODO: update wiki name in temporary object */ },
                    label = { Text("Wiki name") },
                    placeholder = { Text("e.g. Wikipedia") },
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
                    text = "Authentication (optional)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = "",
                    onValueChange = { /* TODO: update wiki username in temporary object */ },
                    label = { Text("Username") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = formFieldBottomPadding,
                            start = formFieldHorizontalPadding,
                            end = formFieldHorizontalPadding
                        ),
                )

                OutlinedTextField(
                    value = "",
                    onValueChange = { /* TODO: update wiki password in temporary object */ },
                    label = { Text("Password") },
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
            }
        }
    }
}