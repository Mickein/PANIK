package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class languages : AppCompatActivity() {
    lateinit var btnAfrikaans: Button
    lateinit var btnEnglish: Button
    lateinit var btnChinese: Button
    lateinit var btnGerman: Button
    lateinit var btnKorean: Button
    lateinit var BackToSettings: Button
    lateinit var Language: TextView
    lateinit var progressBar: ProgressBar
    private lateinit var translator: Translator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_languages)

        BackToSettings = findViewById(R.id.btnBack)
        Language = findViewById(R.id.tvLanguages)
        progressBar = findViewById(R.id.progressBar)

        btnAfrikaans = findViewById(R.id.btnAfrikaans)
        btnEnglish = findViewById(R.id.btnEnglish)
        btnChinese = findViewById(R.id.btnChinese)
        btnGerman = findViewById(R.id.btnGerman)
        btnKorean = findViewById(R.id.btnKorean)
        // Load and apply the saved language when the activity opens
        val savedLanguage = loadLanguagePreference()
        if (savedLanguage != null) {
            applySavedLanguage(savedLanguage)
        }

        btnAfrikaans.setOnClickListener {
            showLoadingState(btnAfrikaans)
            setupTranslator(TranslateLanguage.AFRIKAANS, btnAfrikaans)
        }

        btnEnglish.setOnClickListener {
            showLoadingState(btnEnglish)
            setupTranslator(TranslateLanguage.ENGLISH, btnEnglish)
        }

        btnChinese.setOnClickListener {
            showLoadingState(btnChinese)
            setupTranslator(TranslateLanguage.CHINESE, btnChinese)
        }

        btnGerman.setOnClickListener {
            showLoadingState(btnGerman)
            setupTranslator(TranslateLanguage.GERMAN, btnGerman)
        }

        btnKorean.setOnClickListener {
            showLoadingState(btnKorean)
            setupTranslator(TranslateLanguage.KOREAN, btnKorean)
        }
    }

    private fun saveLanguagePreference(languageCode: String) {
        val sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("selectedLanguage", languageCode)
        editor.apply()
        // Apply the language preference immediately
        applyLanguage(languageCode)
    }
    private fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        // Update the current context with the new language settings
        resources.updateConfiguration(config, resources.displayMetrics)

        // Restart the activity to apply the language change
        val refreshIntent = Intent(this, SettingsPage::class.java)
        startActivity(refreshIntent)
        finish()  // Close the current activity
    }


    // Show loading state (same for all buttons)
    private fun showLoadingState(button: Button) {
        button.isEnabled = false
        button.text = "Loading..."
        progressBar.visibility = View.VISIBLE
    }

    // Hide loading state and reset button to its original text
    private fun hideLoadingState(button: Button, originalText: String) {
        button.isEnabled = true
        button.text = originalText
        progressBar.visibility = View.GONE
    }

    // Updated setupTranslator function to accept the original button text
    private fun setupTranslator(targetLanguage: String, button: Button) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(targetLanguage)
            .build()

        translator = com.google.mlkit.nl.translate.Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        val originalText = button.text.toString() // Get the original button text

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // Hide loading state and restore the original button text
                hideLoadingState(button, originalText)
                translateSettingsActivityText()

                saveLanguagePreference(targetLanguage)
            }
            .addOnFailureListener { exception ->
                hideLoadingState(button, originalText) // Restore original text on failure
                Toast.makeText(this, "Failed to download model: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun translateSettingsActivityText() {
        // Texts to translate
        val textsToTranslate = listOf(
            "Back to Profile",
            "Report History",
            "Language",
            "Notifications"
        )

        val translatedTexts = mutableListOf<String>()

        for (text in textsToTranslate) {
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    translatedTexts.add(translatedText)
                    if (translatedTexts.size == textsToTranslate.size) {
                        // Once all texts are translated, pass them back to SettingsPage
                        val intent = Intent()
                        intent.putStringArrayListExtra("translatedTexts", ArrayList(translatedTexts))
                        setResult(RESULT_OK, intent)
                        finish()  // Return to SettingsPage
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Translation failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun loadLanguagePreference(): String? {
        val sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        return sharedPref.getString("selectedLanguage", null)
    }
    // Apply the translation based on the saved language
    private fun applySavedLanguage(languageCode: String) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(languageCode)
            .build()

        val translator = com.google.mlkit.nl.translate.Translation.getClient(options)
        val conditions = DownloadConditions.Builder().requireWifi().build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translateLanguageActivityText(translator)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to apply saved language", Toast.LENGTH_SHORT).show()
            }
    }

    private fun translateLanguageActivityText(translator: Translator) {
        val textsToTranslate = listOf(
            "Back To Settings",
            "Languages"
        )

        val translatedTexts = mutableListOf<String>()

        for (text in textsToTranslate) {
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    translatedTexts.add(translatedText)
                    if (translatedTexts.size == textsToTranslate.size) {
                        BackToSettings.text = translatedTexts[0]
                        Language.text = translatedTexts[1]

                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Translation failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}