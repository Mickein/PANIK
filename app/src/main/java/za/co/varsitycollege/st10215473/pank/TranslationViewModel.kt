package za.co.varsitycollege.st10215473.pank.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.common.model.DownloadConditions

class TranslationViewModel : ViewModel() {
    private var translator: Translator? = null
    private val _translatedTexts = MutableLiveData<Map<String, String>>() // Holds translations for each TextView
    val translatedTexts: LiveData<Map<String, String>> get() = _translatedTexts

    fun setupTranslator(targetLanguage: String) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(targetLanguage)
            .build()

        translator = Translation.getClient(options)
    }

    fun downloadTranslationModel() {
        val conditions = DownloadConditions.Builder()
            .requireWifi() // Only download when on Wi-Fi
            .build()

        translator?.downloadModelIfNeeded(conditions)
            ?.addOnSuccessListener {
                // Model downloaded successfully
                _translatedTexts.value = mapOf() // Reset translations
            }
    }

    fun translateTexts(originalTexts: List<String>) {
        val translations = mutableMapOf<String, String>()

        originalTexts.forEach { originalText ->
            translator?.translate(originalText)
                ?.addOnSuccessListener { translatedText ->
                    translations[originalText] = translatedText
                    _translatedTexts.value = translations // Update LiveData with translations
                }
                ?.addOnFailureListener {
                    // Handle translation failure if necessary
                }
        }
    }
}
