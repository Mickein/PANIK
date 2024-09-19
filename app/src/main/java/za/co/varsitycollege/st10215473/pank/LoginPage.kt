package za.co.varsitycollege.st10215473.pank

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        // Find Register and Login button, and Google Sign In button
        val openLoginButton = findViewById<Button>(R.id.btnLogin)
        val openRegisterButton = findViewById<Button>(R.id.btnSignUp)
        val googleSignInButton = findViewById<Button>(R.id.btnGoogleSignIn)

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

        //Set onClickListener for Google Sign In
        googleSignInButton.setOnClickListener{ signInWithGoogle() }

    }

    private fun signInWithGoogle(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
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
        auth.signInWithCredential(credential).addOnCompleteListener{
            if (it.isSuccessful){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Successfully Logged in With Google Account" , Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Can't Login Currently, Try Again" , Toast.LENGTH_SHORT).show()
            }
        }
    }


}