package za.co.varsitycollege.st10215473.pank

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.pank.adapter.ReportAdapter
import za.co.varsitycollege.st10215473.pank.data.Reports
import za.co.varsitycollege.st10215473.pank.decorator.SpacesItemDecoration

class ReportHistoryActivity : AppCompatActivity() {

    private lateinit var rvReportHistory: RecyclerView
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var reportAdapter: ReportAdapter
    private val reportList = ArrayList<Reports>()  // List to store reports

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_history)

        // Set window insets for proper layout padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firestore and RecyclerView
        firebaseRef = FirebaseFirestore.getInstance()
        rvReportHistory = findViewById(R.id.rvHistory)
        rvReportHistory.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list initially
        reportAdapter = ReportAdapter(reportList, this)
        rvReportHistory.adapter = reportAdapter

        // Fetch current user's reports from Firestore
        fetchUserReports()

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_between_items)
        rvReportHistory.addItemDecoration(SpacesItemDecoration(spacingInPixels))

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvReportHistory.layoutManager = linearLayoutManager
    }

    private fun fetchUserReports() {
        // Get the current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Query Firestore for reports where userId matches the current user
            firebaseRef.collection("Reports")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val report = document.toObject(Reports::class.java)
                        reportList.add(report)
                    }
                    // Notify the adapter that the data has changed
                    reportAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }
}
