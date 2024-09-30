package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private lateinit var backToProfilePage: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseRef: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseRef = FirebaseFirestore.getInstance()

        // Find the EditText or TextView fields where the user's info will be displayed
        val nameText = findViewById<TextView>(R.id.txtProfileName)
        val emailText = findViewById<TextView>(R.id.txtProfileEmail)
        val surnameText = findViewById<TextView>(R.id.edtSurnameProfile)
        val numberText = findViewById<TextView>(R.id.edtNumberProfile)
        val profilePicView = findViewById<ImageView>(R.id.imgProfilePageImage) // For profile picture

        // Get the current user's UID
        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val uid = user.uid

            // Fetch profile data from Firestore using the UID
            firebaseRef.collection("Profile").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get the fields from the document
                        val name = document.getString("name") ?: "Unknown Name"
                        val email = document.getString("email") ?: "Unknown Email"
                        val surname = document.getString("surname") ?: "Unknown Surmame"
                        val number = document.getString("number") ?: "Unknown Number"
                        val profilePicUrl = document.getString("profilePic") ?: ""

                        // Set the data to the UI components
                        nameText.text = name
                        emailText.text = email
                        surnameText.text = surname
                        numberText.text = number

                        // Load the profile picture using Picasso (or other image loading library)
                        if (profilePicUrl.isNotEmpty()) {
                            Picasso.get().load(profilePicUrl).into(profilePicView)
                        }
                    } else {
                        Toast.makeText(this, "Profile data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to load profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        // Handle the Back to Profile Page button click
        backToProfilePage = findViewById(R.id.btnBackToProfile)
        backToProfilePage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // Restore button visibility when going back to the main activity
            backToProfilePage.visibility = View.GONE
        }

    }

}
