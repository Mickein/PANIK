package za.co.varsitycollege.st10215473.pank

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.AuthFailureError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import za.co.varsitycollege.st10215473.pank.data.Profile
import android.Manifest
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream

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

        askNotificationPermission()
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
                        val userProfile = Profile(uid, name, surname, number, email, "", "")
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
                    getFcmTokenAndSaveToFirestore(uid)
                }
                .addOnFailureListener {
                    Toast.makeText(baseContext, "${it.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(baseContext, "User ID is null. Cannot add profile to database.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFcmTokenAndSaveToFirestore(uid: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result

            // Save the token in Firestore
            firebaseRef.collection("Profile").document(uid).update("fcmToken", token)
                .addOnSuccessListener {
                    sendWelcomeNotification(token) // Trigger the notification directly from the app
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Error saving token to Firestore", e)
                }
        }
    }

    private fun sendWelcomeNotification(fcmToken: String) {
        val url = "https://fcm.googleapis.com/v1/projects/pan-k-f6477/messages:send"

        val notificationData = JSONObject()
        val notificationDetails = JSONObject()

        // Create the notification payload
        notificationDetails.put("title", "Welcome!")
        notificationDetails.put("body", "Thanks for joining our app.")
        notificationData.put("message", JSONObject().put("token", fcmToken).put("notification", notificationDetails))

        // Get access token and send notification
        getAccessToken { accessToken ->
            if (accessToken != null) {
                // Send HTTP request using Volley
                val requestQueue = Volley.newRequestQueue(this)
                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.POST, url, notificationData,
                    { response ->
                        Toast.makeText(this, "Notification sent successfully: $response", Toast.LENGTH_LONG).show()
                    },
                    { error ->
                        val errorMessage = when {
                            error.networkResponse != null -> {
                                "Error code: ${error.networkResponse.statusCode}, Response: ${String(error.networkResponse.data)}"
                            }
                            else -> {
                                "Unknown error occurred: ${error.toString()}"
                            }
                        }
                        Toast.makeText(this, "$errorMessage", Toast.LENGTH_LONG).show()
                    }) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Authorization"] = "Bearer $accessToken"
                        headers["Content-Type"] = "application/json"
                        return headers
                    }
                }
                requestQueue.add(jsonObjectRequest)
            } else {
                Toast.makeText(this, "Failed to retrieve access token", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun askNotificationPermission() {
        // Check if the device is running Android 13 (API level 33) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the permission is already granted
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is granted, you can post notifications
                Log.d("Notification", "Permission already granted.")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show UI to explain why the app needs the permission (Optional)
                // For now, directly ask for the permission
                Log.d("Notification", "Showing permission rationale.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly request the permission
                Log.d("Notification", "Requesting permission directly.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
            Log.d("Notification", "Permission granted: You can now post notifications.")
        } else {

            Log.d("Notification", "Permission denied: Your app will not show notifications.")
        }
    }

    private fun getAccessToken(callback: (String?) -> Unit) {
        // Launch a coroutine on the IO thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val googleCredentials = GoogleCredentials.fromStream(
                    resources.openRawResource(R.raw.panik)
                ).createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

                googleCredentials.refreshIfExpired() // Refresh credentials if expired
                val accessToken = googleCredentials.accessToken?.tokenValue // Get the access token

                // Return the result on the Main thread
                withContext(Dispatchers.Main) {
                    callback(accessToken)
                }
            } catch (e: Exception) {
                // Handle exception
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }


}