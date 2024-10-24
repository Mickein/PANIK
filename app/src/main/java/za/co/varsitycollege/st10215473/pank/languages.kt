package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
    private lateinit var translator: Translator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_languages)

        btnAfrikaans = findViewById(R.id.btnAfrikaans)
        btnEnglish = findViewById(R.id.btnEnglish)
        btnChinese = findViewById(R.id.btnChinese)
        btnGerman = findViewById(R.id.btnGerman)
        btnKorean = findViewById(R.id.btnKorean)

        btnAfrikaans.setOnClickListener {
            setupTranslator(TranslateLanguage.AFRIKAANS)
        }

        btnEnglish.setOnClickListener {
            setupTranslator(TranslateLanguage.ENGLISH)
        }

        btnChinese.setOnClickListener {
            setupTranslator(TranslateLanguage.CHINESE)
        }

        btnGerman.setOnClickListener {
            setupTranslator(TranslateLanguage.GERMAN)
        }

        btnKorean.setOnClickListener {
            setupTranslator(TranslateLanguage.KOREAN)
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


    private fun setupTranslator(targetLanguage: String) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(targetLanguage)
            .build()

        translator = com.google.mlkit.nl.translate.Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                Toast.makeText(this, "Model downloaded. Translating...", Toast.LENGTH_SHORT).show()
                translateSettingsActivityText()

                // Save the selected language in SharedPreferences
                saveLanguagePreference(targetLanguage)
            }
            .addOnFailureListener { exception ->
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

}