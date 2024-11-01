package za.co.varsitycollege.st10215473.pank

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var openSettingsButton: Button
    private lateinit var openAboutDevsButton: Button
    private lateinit var openReportBugsButton: Button
    private lateinit var openLogoutButton: Button
    private lateinit var openProfileActivity: Button
    private lateinit var sharedPreferences: SharedPreferences

    private val REQUEST_CODE_TRANSLATION = 1001  // Request code for SettingsPage

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
        val profilePicView =
            view.findViewById<ImageView>(R.id.imgProfileImage) // For profile picture

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
                        Toast.makeText(
                            requireContext(),
                            "Profile data not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to load profile: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } ?: run {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }


        sharedPreferences = requireActivity().getSharedPreferences("myPrefs", MODE_PRIVATE)

        // Find the buttons in the layout
        openSettingsButton = view.findViewById(R.id.btnSettings)
        openAboutDevsButton = view.findViewById(R.id.btnAboutDevs)
        openReportBugsButton = view.findViewById(R.id.btnReportBugs)
        openLogoutButton = view.findViewById(R.id.btnlogout)
        openProfileActivity = view.findViewById(R.id.btnGoToProfilePage)

        sharedPreferences = requireActivity().getSharedPreferences("myPrefs", MODE_PRIVATE)

        // Load and apply the saved language when the fragment opens
        val savedLanguage = loadLanguagePreference()
        if (savedLanguage != null) {
            applySavedLanguage(savedLanguage)
        }

        // Set an onClickListener to open SettingsPage and handle language change
        openSettingsButton.setOnClickListener {
            val intent = Intent(context, SettingsPage::class.java)
            startActivityForResult(intent, REQUEST_CODE_TRANSLATION)  // Launch SettingsPage for result
        }
        openLogoutButton.setOnClickListener {
            val intent = Intent(context, LoginPage::class.java)
            startActivity(intent)

            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.apply()
        }


        // Other button functionalities can be added here...
        openAboutDevsButton.setOnClickListener {
            Toast.makeText(requireContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }

        openReportBugsButton.setOnClickListener {
            Toast.makeText(requireContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }


        openProfileActivity.setOnClickListener { val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    // Override onActivityResult to check if language update was made
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_TRANSLATION && resultCode == AppCompatActivity.RESULT_OK) {
            // Reapply translation when coming back from SettingsPage
            val savedLanguage = loadLanguagePreference()
            if (savedLanguage != null) {
                applySavedLanguage(savedLanguage)  // Reapply the language
            }
        }
    }

    // Method to load the saved language from SharedPreferences
    private fun loadLanguagePreference(): String? {
        val sharedPref: SharedPreferences = requireActivity().getSharedPreferences("AppSettings", AppCompatActivity.MODE_PRIVATE)
        return sharedPref.getString("selectedLanguage", null)
    }

    // Apply the translation based on the saved language
    private fun applySavedLanguage(languageCode: String) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(languageCode)
            .build()

        val translator = com.google.mlkit.nl.translate.Translation.getClient(options)
        val conditions = DownloadConditions.Builder().requireWifi().build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translateProfileFragmentTexts(translator)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to apply saved language", Toast.LENGTH_SHORT).show()
            }
    }

    // Translate the text of the buttons in ProfileFragment
    private fun translateProfileFragmentTexts(translator: Translator) {
        val textsToTranslate = listOf(
            "Settings", "About Devs", "Report Bugs", "Log-out"
        )

        val translatedTexts = mutableListOf<String>()

        for (text in textsToTranslate) {
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    translatedTexts.add(translatedText)
                    if (translatedTexts.size == textsToTranslate.size) {
                        // Apply translations to the respective buttons
                        openSettingsButton.text = translatedTexts[0]
                        openAboutDevsButton.text = translatedTexts[1]
                        openReportBugsButton.text = translatedTexts[2]
                        openLogoutButton.text = translatedTexts[3]

                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Translation failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}