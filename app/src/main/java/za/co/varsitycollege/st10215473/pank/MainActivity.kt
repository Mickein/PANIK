package za.co.varsitycollege.st10215473.pank

import MapFragment
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

class MainActivity : AppCompatActivity() {
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



}