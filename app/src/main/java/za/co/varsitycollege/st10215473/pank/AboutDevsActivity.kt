package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AboutDevsActivity : AppCompatActivity() {

    private lateinit var backToProfilePage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about_devs)

        // Handle the Back to Profile Page button click
        backToProfilePage = findViewById(R.id.imgBack)
        backToProfilePage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // Restore button visibility when going back to the main activity
            backToProfilePage.visibility = View.GONE
        }
    }
}