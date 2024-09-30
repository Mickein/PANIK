package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileActivity : AppCompatActivity() {

    private lateinit var backToProfilePage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        // Find the EditText or TextView fields where the user's info will be displayed
        val nameText = findViewById<TextView>(R.id.txtProfileName)
        val emailText = findViewById<TextView>(R.id.txtProfileEmail)
        val profilePicView = findViewById<ImageView>(R.id.imgProfilePageImage) // For profile picture

        // Retrieve the data from the Intent
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val profilePic = intent.getStringExtra("profilePic")

        // Set the data to the UI components
        nameText.setText(name)
        emailText.setText(email)

        backToProfilePage = findViewById(R.id.btnBackToProfile)
        backToProfilePage.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
    }
}