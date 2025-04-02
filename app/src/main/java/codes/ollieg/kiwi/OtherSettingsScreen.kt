package codes.ollieg.kiwi

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OtherSettingsScreen(
) {
    Log.i("OtherSettingsScreen", "Other Settings Screen")
    Text(
        text = "Other Settings Screen",
        modifier = Modifier.padding(16.dp)
    )
}
