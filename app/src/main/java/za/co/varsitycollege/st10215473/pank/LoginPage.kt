package za.co.varsitycollege.st10215473.pank

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.common.model.DownloadConditions
import za.co.varsitycollege.st10215473.pank.data.Profile
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import kotlinx.coroutines.launch

class LoginPage : AppCompatActivity() {
    //variable for going to dashboard page if user has a registered account
    lateinit var openDash: TextView
    //variables for firebase authentication
    private lateinit var authr: FirebaseAuth
    private lateinit var passwordEdit: EditText
    private lateinit var loginemail: EditText
    //variable for going to register page if user doesnt have an account
    private lateinit var goToLog: TextView
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var emailtextview: TextView
    private lateinit var login: TextView
    private lateinit var signuptextview: TextView
    private lateinit var passwordtextview: TextView
    private lateinit var loginbutton: Button
    private lateinit var registerButton: Button
    private lateinit var ortextview: TextView
    private lateinit var accountTextview: TextView


    private lateinit var sharedPreferences: SharedPreferences

    //Fingerprint manager
    private val promptManager by lazy {
        BiometricPromptManager(this)
    }
    // Register activity result launcher for biometric enrollment
    private val enrollLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle the result if needed (e.g., show a Toast if enrolled successfully)
        Toast.makeText(this, "Biometric setup result: $result", Toast.LENGTH_SHORT).show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)

        sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE)

        firebaseRef = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        // Find Register button, and Google Sign In button
        val openRegisterButton = findViewById<Button>(R.id.btnSignUp)
        val googleSignInButton = findViewById<Button>(R.id.btnGoogleSignIn)

        // Set onClickListener for Register button
        openRegisterButton.setOnClickListener {
            // Create an Intent to open the new activity
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }
        //Set onClickListener for Google Sign In
        googleSignInButton.setOnClickListener{
            signInWithGoogle()
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }

        //Firebase Authentication
        passwordEdit = findViewById(R.id.edtPasswordLogin)
        loginemail = findViewById(R.id.edtEmailLogin)
        openDash = findViewById(R.id.btnLogin)
        authr = com.google.firebase.Firebase.auth

        signuptextview = findViewById(R.id.tvSignin)
        login = findViewById(R.id.txtLogin)
        emailtextview = findViewById(R.id.tvEmail)
        passwordtextview = findViewById(R.id.tvPassword)
        loginbutton = findViewById(R.id.btnLogin)
        ortextview = findViewById(R.id.tvOr)
        registerButton = findViewById(R.id.btnSignUp)
        accountTextview = findViewById(R.id.tvAccount)

        // Load and apply the saved language when the activity opens
        val savedLanguage = loadLanguagePreference()
        if (savedLanguage != null) {
            applySavedLanguage(savedLanguage)
        }

        openDash.setOnClickListener(View.OnClickListener {
            val password = passwordEdit.text.toString()
            val email = loginemail.text.toString()

            if(password.isEmpty()) {
                passwordEdit.error = "Type a password"
                return@OnClickListener  // Return to prevent further execution
            }

            if(email.isEmpty()) {
                loginemail.error = "Type an email"
                return@OnClickListener  // Return to prevent further execution
            }
            if(password.isNotEmpty() && email.isNotEmpty()){
                LoginUser(email, password)
            }
        })
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
            "Sign-in to continue","Log-in",
            "Email","Password",
            "Login","Or",
            "Register","Don't Have an account"
        )

        val translatedTexts = mutableListOf<String>()

        for (text in textsToTranslate) {
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    translatedTexts.add(translatedText)
                    if (translatedTexts.size == textsToTranslate.size) {
                        signuptextview.text = translatedTexts[0]
                        login.text = translatedTexts[1]
                        emailtextview.text = translatedTexts[2]
                        passwordtextview.text = translatedTexts[3]
                        loginbutton.text = translatedTexts[4]
                        ortextview.text = translatedTexts[5]
                        registerButton.text = translatedTexts[6]
                        accountTextview.text = translatedTexts[7]
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

    private fun LoginUser(email: String, password: String) {
        authr.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("isLoggedIn", true)
                    editor.apply()


                    // Set up biometric prompt handling
                    setupBiometrics()

                    // Show biometric prompt when the activity is opened
                    promptManager.showBiometricPrompt(
                        title = "Biometric Authentication",
                        description = "Please authenticate to access PAN!K"
                    )

                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(baseContext, "Login Successful", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
    //OpenAI. (2024). Conversation on Signing in With Google. Available at https://www.openai.com
    private fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.apply()

                // After sign out is successful, proceed with the sign-in intent
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            } else {
                Toast.makeText(this, "Sign-out failed, try again", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result->
        if(result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updatedUI(account)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Sign In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatedUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    val displayName = account.displayName ?: "Unknown"
                    val email = account.email ?: "No email"
                    val phoneNumber = user.phoneNumber ?: "No phone number"

                    val userProfile = Profile(
                        id = user.uid,
                        name = displayName,
                        surname = "",
                        number = phoneNumber,
                        email = email,
                        profilePic = account.photoUrl.toString()
                    )

                    // Store user profile in Firestore
                    addUserToFirebase(userProfile)


                    // Set up biometric prompt handling
                    setupBiometrics()

                    // Show biometric prompt when the activity is opened
                    promptManager.showBiometricPrompt(
                        title = "Biometric Authentication",
                        description = "Please authenticate to access PAN!K"
                    )

                    // Redirect to ProfileActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "User data unavailable", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Google Sign-in Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //OpenAI. (2024). Conversation on Adding User To Firebase. Available at https://www.openai.com
    private fun addUserToFirebase(userProfile: Profile) {
        val uid = userProfile.id
        uid?.let {
            firebaseRef.collection("Profile")
                .document(it)
                .set(userProfile)
                .addOnSuccessListener {
                    Log.d("Firestore", "User profile added successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error adding profile", e)
                    Toast.makeText(this, "Failed to add profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Log.e("Firestore", "User ID is null")
            Toast.makeText(this, "User ID is null. Cannot add profile to database.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBiometrics() {
        lifecycleScope.launch {
            promptManager.promptResults.collect { result ->
                when (result) {
                    is BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                        if (Build.VERSION.SDK_INT >= 30) {
                            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                putExtra(
                                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                                )
                            }
                            enrollLauncher.launch(enrollIntent)
                        } else {
                            Toast.makeText(
                                this@LoginPage,
                                "Biometrics not set up",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()  // Redirect back if biometrics are not set
                        }
                    }

                    is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                        Toast.makeText(this@LoginPage, "Error: ${result.error}", Toast.LENGTH_SHORT)
                            .show()
                        finish()  // Redirect back to the previous page on authentication error
                    }

                    BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                        Toast.makeText(this@LoginPage, "Authentication Failed", Toast.LENGTH_SHORT)
                            .show()
                        finish()  // Redirect back on failed authentication
                    }

                    BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                        Toast.makeText(
                            this@LoginPage,
                            "Authentication Successful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
                        Toast.makeText(
                            this@LoginPage,
                            "Biometric feature unavailable",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                        Toast.makeText(
                            this@LoginPage,
                            "Biometric hardware unavailable",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        }
    }

}