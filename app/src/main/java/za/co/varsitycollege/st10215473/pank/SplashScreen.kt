package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var splashGreeting: TextView
    private lateinit var splashBG: ImageView
    private lateinit var splashLogo: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        splashLogo = findViewById(R.id.imgSplashLogo)
        splashBG = findViewById(R.id.imgSplashBG)
        splashGreeting = findViewById(R.id.tvWelcome)

        splashLogo.animate().translationY(4000F).setDuration(1000).setStartDelay(2000)
        splashGreeting.animate().translationY(4000F).setDuration(1000).setStartDelay(2000)
        splashBG.animate().translationY(-4000F).setDuration(1000).setStartDelay(2000)


        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE)

        // Delay for a few seconds to show splash screen
        Handler().postDelayed({
            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

            if (isLoggedIn) {
                // User is logged in, navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // User is not logged in, navigate to LoginActivity
                startActivity(Intent(this, LoginPage::class.java))
            }

            // Close the SplashScreenActivity
            finish()
        }, 3000) // Delay time in milliseconds
    }
}