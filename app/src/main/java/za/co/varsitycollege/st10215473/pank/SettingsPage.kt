package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class SettingsPage : AppCompatActivity() {
    lateinit var txtBackToProfile: TextView
    lateinit var btnReportHistory: Button
    lateinit var btnLanguage: Button
    lateinit var btnNotifications: Button
    lateinit var btnBackToProfile:Button

    //Fingerprint manager
    private val promptManager by lazy {
        BiometricPromptManager(this)
    }
    // Register activity result launcher for biometric enrollment
    private val enrollLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle the result if needed (e.g., show a Toast if enrolled successfully)
        Toast.makeText(this, "Biometric setup result: $result", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_page)

        txtBackToProfile = findViewById(R.id.txtBackToProfile)
        btnReportHistory = findViewById(R.id.btnReportHistory)
        btnLanguage = findViewById(R.id.btnLanguage)
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

        btnReportHistory.setOnClickListener {
            val intent = Intent(this, ReportHistoryActivity::class.java)
            startActivity(intent)
        }

           /*     // Set up biometric prompt handling
                    setupBiometrics()

                // Show biometric prompt when the activity is opened
                promptManager.showBiometricPrompt(
                    title = "Biometric Authentication",
                    description = "Please authenticate to access Settings Features"
                )*/

        btnReportHistory.setOnClickListener {
            val intent = Intent(this, ReportHistoryActivity::class.java)
            startActivity(intent)
        }

    }

    // Method to handle biometric authentication and potential enrollment
    private fun setupBiometrics() {
        lifecycleScope.launch {
            promptManager.promptResults.collect { result ->
                when (result) {
                    is BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                        if (Build.VERSION.SDK_INT >= 30) {
                            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                putExtra(
                                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                                )
                            }
                            enrollLauncher.launch(enrollIntent)
                        } else {
                            Toast.makeText(this@SettingsPage, "Biometrics not set up", Toast.LENGTH_SHORT).show()
                            finish()  // Redirect back if biometrics are not set
                        }
                    }
                    is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                        Toast.makeText(this@SettingsPage, "Error: ${result.error}", Toast.LENGTH_SHORT).show()
                        finish()  // Redirect back to the previous page on authentication error
                    }
                    BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                        Toast.makeText(this@SettingsPage, "Authentication Failed", Toast.LENGTH_SHORT).show()
                        finish()  // Redirect back on failed authentication
                    }
                    BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                        Toast.makeText(this@SettingsPage, "Authentication Successful", Toast.LENGTH_SHORT).show()
                    }
                    BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
                        Toast.makeText(this@SettingsPage, "Biometric feature unavailable", Toast.LENGTH_SHORT).show()
                    }
                    BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                        Toast.makeText(this@SettingsPage, "Biometric hardware unavailable", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
