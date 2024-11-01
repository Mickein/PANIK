package za.co.varsitycollege.st10215473.pank

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.pank.adapter.ReportAdapter
import za.co.varsitycollege.st10215473.pank.data.Reports


class FeedFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var reportAdapter: ReportAdapter
    private val reportList = ArrayList<Reports>()  // List to store all reports
    private val firebaseRef = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feed, container, false)

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.FeedRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        reportAdapter = ReportAdapter(reportList, requireContext())
        recyclerView.adapter = reportAdapter

        // Fetch all reports from Firestore
        fetchAllReports()

        return view
    }

    private fun fetchAllReports() {
        firebaseRef.collection("reports").get()
            .addOnSuccessListener { querySnapshot ->
                reportList.clear()
                for (document in querySnapshot.documents) {
                    val report = document.toObject(Reports::class.java)
                    report?.let { reportList.add(it) }
                }
                reportAdapter.notifyDataSetChanged() // Update adapter to refresh RecyclerView
            }
            .addOnFailureListener { exception ->
                Log.e("FeedFragment", "Error fetching reports: ${exception.message}")
            }
    }

}