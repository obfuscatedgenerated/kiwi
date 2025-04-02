package codes.ollieg.kiwi

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ManageWikisScreen(
) {
    Log.i("ManageWikisScreen", "Manage Wikis Screen")
    Text(
        text = "Manage Wikis Screen",
        modifier = Modifier.padding(16.dp)
    )
}
