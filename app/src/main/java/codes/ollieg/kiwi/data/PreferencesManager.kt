package codes.ollieg.kiwi.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "kiwiPrefs")

class PreferencesManager(private val context: Context) {
    companion object {
        private val FONT_KEY = stringPreferencesKey("font")
    }

    val fontFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[FONT_KEY]
        }

    suspend fun saveFont(font: String) {
        context.dataStore.edit { preferences ->
            preferences[FONT_KEY] = font
        }
    }
}
