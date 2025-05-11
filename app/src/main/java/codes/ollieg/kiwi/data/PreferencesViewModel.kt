package codes.ollieg.kiwi.data

import android.app.Application
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PreferencesViewModel(application: Application): AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)

    fun resolveFontFamily(name: String): FontFamily? {
        return when (name) {
            "Serif" -> FontFamily.Serif
            "SansSerif" -> FontFamily.SansSerif
            "Monospace" -> FontFamily.Monospace
            "Cursive" -> FontFamily.Cursive
            else -> null
        }
    }

    val fontName: LiveData<String> = preferencesManager.fontFlow
        .map{ it ?: "Serif"} // default value "Serif"
        .asLiveData()

    val fontFamily : LiveData<FontFamily> = fontName.map { name ->
        resolveFontFamily(name) ?: FontFamily.Serif // default value "Serif"
    }

    fun setFont(name: String) {
        // check font name is valid
        if (name.isEmpty()) {
            throw IllegalArgumentException("Font name cannot be empty")
        }

        if (resolveFontFamily(name) == null) {
            throw IllegalArgumentException("Font name is not valid")
        }

        viewModelScope.launch {
            preferencesManager.saveFont(name)
        }
    }

    val lineHeight: LiveData<Int> = preferencesManager.lineHeightFlow
        .map{ it ?: 24 } // default value 24
        .asLiveData()

    fun setLineHeight(lineHeight: Int) {
        // check line height is valid
        if (lineHeight <= 0) {
            throw IllegalArgumentException("Line height must be greater than 0")
        }

        viewModelScope.launch {
            preferencesManager.saveLineHeight(lineHeight)
        }
    }
}