package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import za.co.varsitycollege.st10215473.pank.data.Profile

class RegisterPage : AppCompatActivity() {
    //variable for going back to Login page if user has an existing account
    lateinit var openLog: TextView
    //variable for storing the users registration details
    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var firstName: EditText
    private lateinit var surname: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var registerButton: TextView
    private lateinit var authReg: FirebaseAuth
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var confirmPassword: EditText
    private lateinit var RegisterTextview: TextView

    private lateinit var emailtextview: TextView
    private lateinit var passwordtextview: TextView
    private lateinit var firstNametextview: TextView
    private lateinit var surnametextview: TextView
    private lateinit var phoneNumbertextview: TextView
    private lateinit var confirmPasswordtextview: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_page)
        openLoginPage()

        firstName = findViewById(R.id.edtNameRegister)
        surname = findViewById(R.id.edtSurnameRegister)
        phoneNumber = findViewById(R.id.edtNumberRegister)
        emailEdit = findViewById(R.id.edtEmailRegister)
        passwordEdit = findViewById(R.id.edtPasswordRegister)
        confirmPassword = findViewById(R.id.edtPasswordConfirmPasswordRegister)
        registerButton = findViewById(R.id.btnRegister)
        firebaseRef = FirebaseFirestore.getInstance()
        RegisterTextview = findViewById(R.id.txtRegister)


        firstNametextview = findViewById(R.id.txtRegisterName)
        surnametextview = findViewById(R.id.txtRegisterSurname)
        phoneNumbertextview = findViewById(R.id.txtRegisterNumber)
        emailtextview = findViewById(R.id.txtRegisterEmail)
        passwordtextview = findViewById(R.id.txtRegisterPassword)
        confirmPasswordtextview = findViewById(R.id.txtRegisterConfirmPassword)

        // Load and apply the saved language when the activity opens
        val savedLanguage = loadLanguagePreference()
        if (savedLanguage != null) {
            applySavedLanguage(savedLanguage)
        }

        //Firebase authentication
        authReg = Firebase.auth
        registerButton.setOnClickListener()
        {
            val name = firstName.text.toString()
            val surname = surname.text.toString()
            val email = emailEdit.text.toString()
            val number = phoneNumber.text.toString()
            val password = passwordEdit.text.toString()
            val confirm = confirmPassword.text.toString()

            if(password.length < 8){

                passwordEdit.setText("")
                passwordEdit.error = "Password must be min 8 characters!"
                confirmPassword.setText("")
            }
            else if(password != confirm){
                passwordEdit.setText("")
                confirmPassword.setText("")
                Toast.makeText(this, "Password does not match!", Toast.LENGTH_SHORT).show()
            }
            else{
                RegisterUser(email, password, name, surname, number)
            }
        }
    }
    // Method to load the saved language from SharedPreferences
    private fun loadLanguagePreference(): String? {
        val sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
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
                translateSettingsActivityText(translator)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to apply saved language", Toast.LENGTH_SHORT).show()
            }
    }

    private fun translateSettingsActivityText(translator: Translator) {
        val textsToTranslate = listOf(
            "Register","Name","Surname",
            "Phone Number","Email Address",
            "Password","Confirm Password",
            "Register"
        )

        val translatedTexts = mutableListOf<String>()

        for (text in textsToTranslate) {
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    translatedTexts.add(translatedText)
                    if (translatedTexts.size == textsToTranslate.size) {
                        RegisterTextview.text = translatedTexts[0]
                        firstNametextview.text = translatedTexts[1]
                        surnametextview.text = translatedTexts[2]
                        phoneNumbertextview.text = translatedTexts[3]
                        emailtextview.text = translatedTexts[4]
                        passwordtextview.text = translatedTexts[5]
                        confirmPasswordtextview.text = translatedTexts[6]
                        registerButton.text = translatedTexts[7]
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Translation failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    companion object {
        const val REQUEST_CODE_TRANSLATION = 1001
    }

    fun openLoginPage()
    {
        openLog = findViewById(R.id.btnRegister)
        openLog.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        })
    }

    private fun RegisterUser(email: String, password: String, name: String, surname: String, number: String) {
        authReg.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(baseContext, "Registration Successful", Toast.LENGTH_LONG).show()
                    val user = authReg.currentUser
                    val uid = user?.uid
                    if (user != null) {
                        val userProfile = Profile(uid, name, surname, number, email, "")
                        addUserToFirebase(userProfile)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun addUserToFirebase(userProfile: Profile) {
        val uid = userProfile.id
        uid?.let {
            firebaseRef.collection("Profile")
                .document(it) // Use uid as the document ID
                .set(userProfile)
                .addOnSuccessListener {
                    // On success, navigate to Login page
                    val intent = Intent(this@RegisterPage, LoginPage::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(baseContext, "${it.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(baseContext, "User ID is null. Cannot add profile to database.", Toast.LENGTH_SHORT).show()
        }
    }

}