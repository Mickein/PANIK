package za.co.varsitycollege.st10215473.pank

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.pank.adapter.ReportAdapter
import za.co.varsitycollege.st10215473.pank.data.Reports
import za.co.varsitycollege.st10215473.pank.decorator.SpacesItemDecoration
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.varsitycollege.st10215473.pank.data.ReportEntity

class ReportHistoryActivity : AppCompatActivity() {

    private lateinit var rvReportHistory: RecyclerView
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var reportAdapter: ReportAdapter

    private val reportList = ArrayList<Reports>()  // List to store reports
    private lateinit var titleHistory: TextView
    private lateinit var Description: TextView
    private lateinit var LocationHistory: TextView


    private val reportList = ArrayList<Reports>()
    private lateinit var reportDao: ReportDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = AppDatabase.getDatabase(this)
        reportDao = db.reportDao()

        // Initialize Firestore and RecyclerView
        firebaseRef = FirebaseFirestore.getInstance()
        rvReportHistory = findViewById(R.id.rvHistory)
        rvReportHistory.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list initially
        reportAdapter = ReportAdapter(reportList, this)
        rvReportHistory.adapter = reportAdapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        // Fetch current user's reports from Firestore
        fetchUserReports(userId)

        // Add spacing between items in RecyclerView
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_between_items)
        rvReportHistory.addItemDecoration(SpacesItemDecoration(spacingInPixels))
    }

    //OpenAI. (2024). Conversation on Report History. Available at https://www.openai.com
    private fun fetchUserReports(userId: String?) {
        if(isOnline()){
            val reportApi = ApiClient.getClient().create(ReportApi::class.java)
            if (userId != null) {
                reportApi.getReports(userId).enqueue(object : Callback<List<Reports>> {
                    override fun onResponse(call: Call<List<Reports>>, response: Response<List<Reports>>) {
                        if (response.isSuccessful && response.body() != null) {
                            reportList.clear()  // Clear previous reports
                            reportList.addAll(response.body()!!)  // Add new reports
                            Log.d("ReportHistoryActivity", "Reports fetched: ${reportList.size}")

                            // Convert Reports to ReportEntity and insert them into Room
                            CoroutineScope(Dispatchers.IO).launch {
                                val reportEntities = response.body()!!.map { report ->
                                    ReportEntity(
                                        title = report.title ?: "Untitled",  // Default title
                                        description = report.description ?: "No description",  // Default description
                                        location = report.location ?: GeoPoint(0.0, 0.0),  // Default to a GeoPoint (0, 0)
                                        userId = report.userId ?: "Unknown user",  // Default userId
                                        timestamp = report.timestamp ?: System.currentTimeMillis(),  // Default to current timestamp
                                        imageUrl = report.imageUrl ?: ""  // Default to empty string for image URL
                                    )
                                }
                                reportDao.insertAllReports(reportEntities)  // Insert all reports into Room
                            }
                        } else {
                            Toast.makeText(this@ReportHistoryActivity, "Failed to retrieve reports: ${response.message()}", Toast.LENGTH_SHORT).show()
                            Log.e("MainActivity", "Failed to retrieve reports: ${response.message()}")
                        }
                        runOnUiThread { reportAdapter.notifyDataSetChanged() }
                    }

                    override fun onFailure(call: Call<List<Reports>>, t: Throwable) {
                        Log.e("MainActivity", "Error: ${t.message}")
                    }
                })
            }
        }else {
            CoroutineScope(Dispatchers.IO).launch {
                val localReports = reportDao.getReportsByUserId(userId)  // Fetch reports from Room
                if (localReports.isNotEmpty()) {
                    // If there are local reports, update the UI on the main thread
                    reportList.clear()
                    reportList.addAll(localReports.map { reportEntity ->
                        Reports(
                            title = reportEntity.title,
                            description = reportEntity.description,
                            location = reportEntity.location,
                            userId = reportEntity.userId,
                            timestamp = reportEntity.timestamp,
                            imageUrl = reportEntity.imageUrl
                        )
                    })
                    // Notify the adapter to update the RecyclerView
                    runOnUiThread { reportAdapter.notifyDataSetChanged() }
                } else {
                    // Optionally, show a message or handle the case of no local reports
                    Log.d("ReportHistoryActivity", "No local reports found.")
                }

            }
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}

