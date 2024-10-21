package za.co.varsitycollege.st10215473.pank

import BaseActivity
import MapFragment
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import java.util.Locale

class MainActivity : BaseActivity() {
    private lateinit var bottomNavBar: ChipNavigationBar
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        rootView = findViewById(R.id.main) // Root view for layout listener
        bottomNavBar = findViewById(R.id.bottomNav)

        setupWindowInsets()
        setupBottomNavigation()
        replaceFragment(FeedFragment())

        observeKeyboardVisibility()

        // Load saved language preference
        val sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("selectedLanguage", "en")  // Default to English if not set

        // Apply the saved language
        applyLanguage(savedLanguage!!)


    }
    private fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        // Update the current context with the new language settings
        resources.updateConfiguration(config, resources.displayMetrics)

    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupBottomNavigation() {

        bottomNavBar.setOnItemSelectedListener { menuItem ->
            when(menuItem) {
                R.id.map -> {
                    replaceFragment(MapFragment())
                }
                R.id.feed -> {
                    replaceFragment(FeedFragment())
                }
                R.id.report -> {
                    replaceFragment(ReportFragment())
                }
                R.id.profile -> replaceFragment(ProfileFragment())
                else -> false
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }

    private fun observeKeyboardVisibility() {
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - r.bottom

            bottomNavBar.visibility = if (keypadHeight > screenHeight * 0.15) View.GONE else View.VISIBLE
        }
    }

//    override fun onResume() {
//        super.onResume()
//
//        // Check if the intent has the "openProfile" extra and replace the fragment accordingly
//        if (intent?.getBooleanExtra("openProfile", false) == true) {
//            replaceFragment(ProfileFragment()) // Open ProfileFragment when returning
//            bottomNavBar.setItemSelected(R.id.profile) // Update the navigation bar state
//        }
//    }


}