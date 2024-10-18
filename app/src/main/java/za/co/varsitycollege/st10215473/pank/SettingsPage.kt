package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class SettingsPage : AppCompatActivity() {
    lateinit var txtBackToProfile: TextView
    lateinit var btnReportHistory: Button
    lateinit var btnLanguage: Button
    lateinit var btnNotifications: Button
    lateinit var btnBackToProfile:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_page)

        txtBackToProfile = findViewById(R.id.txtBackToProfile)
        btnReportHistory = findViewById(R.id.btnReportHistory)
        btnLanguage = findViewById(R.id.btnLanguage)
        btnNotifications = findViewById(R.id.btnNotifications)
        btnBackToProfile = findViewById(R.id.btnBackToProfile)


        // Load and apply the saved language when the activity opens
        val savedLanguage = loadLanguagePreference()
        if (savedLanguage != null) {
            applySavedLanguage(savedLanguage)
        }

        // Set an onClickListener for btnLanguage to navigate to the languages activity
        btnLanguage.setOnClickListener {
            val intent = Intent(this, languages::class.java)
            startActivityForResult(intent, REQUEST_CODE_TRANSLATION)
        }

        btnBackToProfile.setOnClickListener {
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()  // This will close the SettingsActivity and return to MainActivity
        }


    }
    // Method to load the saved language from SharedPreferences
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
                translateSettingsActivityText(translator)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to apply saved language", Toast.LENGTH_SHORT).show()
            }
    }

    private fun translateSettingsActivityText(translator: Translator) {
        val textsToTranslate = listOf(
            "Back to Profile Page",
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
                        txtBackToProfile.text = translatedTexts[0]
                        btnReportHistory.text = translatedTexts[1]
                        btnLanguage.text = translatedTexts[2]
                        btnNotifications.text = translatedTexts[3]
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Translation failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    companion object {
        const val REQUEST_CODE_TRANSLATION = 1001
    }


}
