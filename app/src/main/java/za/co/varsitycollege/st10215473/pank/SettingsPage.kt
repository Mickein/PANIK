package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsPage : AppCompatActivity() {
    private lateinit var backToProfile: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings_page)

        backToProfile = findViewById(R.id.btnBackToProfile)
        backToProfile.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, ProfileFragment::class.java)
            startActivity(intent)
        })
    }
}