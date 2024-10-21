import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySavedLanguage()
    }

    private fun applySavedLanguage() {
        // Load the saved language preference
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("selectedLanguage", "en") // Default to English

        // Apply the language if it is not already applied
        if (savedLanguage != null) {
            setLocale(savedLanguage)
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        // Apply the language setting to the context
        resources.updateConfiguration(config, resources.displayMetrics)

        // This line ensures the new locale is applied across activities
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}