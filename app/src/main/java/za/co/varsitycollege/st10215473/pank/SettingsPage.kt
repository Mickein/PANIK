package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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

        // Set an onClickListener for btnLanguage to navigate to the languages activity
        btnLanguage.setOnClickListener {
            val intent = Intent(this, languages::class.java)
            startActivityForResult(intent, REQUEST_CODE_TRANSLATION)
        }

        btnBackToProfile.setOnClickListener {
            finish()  // This will close the SettingsActivity and return to MainActivity
        }




    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if result is OK and we have received data
        if (requestCode == REQUEST_CODE_TRANSLATION && resultCode == RESULT_OK) {
            val translatedTexts = data?.getStringArrayListExtra("translatedTexts")

            if (translatedTexts != null && translatedTexts.size == 4) {
                // Update the text of the buttons with the translated values
                txtBackToProfile.text = translatedTexts[0] // "Back to Profile"
                btnReportHistory.text = translatedTexts[1] // "Report History"
                btnLanguage.text = translatedTexts[2] // "Language"
                btnNotifications.text = translatedTexts[3] // "Notifications"
            }
        }

    }
    companion object {
        const val REQUEST_CODE_TRANSLATION = 1001
    }

}
