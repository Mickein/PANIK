package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsPage : AppCompatActivity() {
    private lateinit var backToProfile: TextView
    private lateinit var toReportHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings_page)

        backToProfile = findViewById(R.id.btnBackToProfile)
        backToProfile.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
        toReportHistory = findViewById(R.id.btnReportHistory)
        toReportHistory.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, ReportHistoryActivity::class.java)
            startActivity(intent)
        })

        val openNotificationsButton = findViewById<Button>(R.id.btnNotifications)
        val openLanguageButton = findViewById<Button>(R.id.btnLanguage)
        openNotificationsButton.setOnClickListener {
            Toast.makeText(baseContext, "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }
        openLanguageButton.setOnClickListener {
            Toast.makeText(baseContext, "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }
}