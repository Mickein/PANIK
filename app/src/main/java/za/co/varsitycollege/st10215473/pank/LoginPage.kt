package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find Register and Login button
        val openLoginButton = findViewById<Button>(R.id.btnLogin)
        val openRegisterButton = findViewById<Button>(R.id.btnSignUp)

        // Set onClickListener for Login button
        openLoginButton.setOnClickListener {
            // Create an Intent to open the new activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        // Set onClickListener for Register button
        openRegisterButton.setOnClickListener {
            // Create an Intent to open the new activity
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }
    }
}