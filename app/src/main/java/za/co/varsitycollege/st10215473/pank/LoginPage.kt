package za.co.varsitycollege.st10215473.pank

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

class LoginPage : AppCompatActivity() {
    //variable for going to dashboard page if user has a registered account
    lateinit var openDash: TextView

    lateinit var btnLanguageTranslate:Button
    lateinit  var txtLogin:TextView

    var originalText:String=""
    private lateinit var translator: Translator

    private lateinit var textViewsToTranslate: List<TextView>
    //variables for firebase authentication
    private lateinit var authr: FirebaseAuth
    private lateinit var passwordEdit: EditText
    private lateinit var loginemail: EditText
    //variable for going to register page if user doesnt have an account
    private lateinit var goToReg: TextView
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)

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
        googleSignInButton.setOnClickListener{ signInWithGoogle() }


        //Firebase Authentication
        passwordEdit = findViewById(R.id.edtPasswordLogin)
        loginemail = findViewById(R.id.edtEmailLogin)
        openDash = findViewById(R.id.btnLogin)
        authr = com.google.firebase.Firebase.auth

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

    private fun LoginUser(email: String, password: String) {
        authr.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
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

                    // Redirect to ProfileActivity
                    val intent = Intent(this, ProfileActivity::class.java)
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



}