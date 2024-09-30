package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseRef: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseRef = FirebaseFirestore.getInstance()

        nameText = view.findViewById(R.id.lblName)
        emailText = view.findViewById(R.id.lblEmail)
        val profilePicView = view.findViewById<ImageView>(R.id.imgProfileImage) // For profile picture

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
                        val profilePicUrl = document.getString("profilePic") ?: ""

                        // Set the data to the UI components
                        nameText.text = name
                        emailText.text = email
                        // Load the profile picture using Picasso
                        if (profilePicUrl.isNotEmpty()) {
                            Picasso.get().load(profilePicUrl).into(profilePicView)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Profile data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to load profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }

        // Find the Buttons in the fragment layout
        val openSettingsButton = view.findViewById<Button>(R.id.btnSettings)
        val openAboutDevsButton = view.findViewById<Button>(R.id.btnAboutDevs)
        val openReportBugsButton = view.findViewById<Button>(R.id.btnReportBugs)
        val openLogoutButton = view.findViewById<Button>(R.id.btnlogout)
        val openProfileActivity = view.findViewById<Button>(R.id.btnGoToProfilePage)

        openSettingsButton.setOnClickListener {
            // Create an Intent to open the new activity
            val intent = Intent(requireContext(), SettingsPage::class.java)
            startActivity(intent)
        }

        openAboutDevsButton.setOnClickListener {
            Toast.makeText(requireContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }
        openReportBugsButton.setOnClickListener {
            Toast.makeText(requireContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }
        openLogoutButton.setOnClickListener {
            Toast.makeText(requireContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }

        openProfileActivity.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
