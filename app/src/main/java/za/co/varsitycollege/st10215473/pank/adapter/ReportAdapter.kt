package za.co.varsitycollege.st10215473.pank.adapter

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.GeoPoint
import com.squareup.picasso.Picasso
import za.co.varsitycollege.st10215473.pank.R
import za.co.varsitycollege.st10215473.pank.data.Reports
import za.co.varsitycollege.st10215473.pank.databinding.ReportHistoryCardviewBinding
import java.io.IOException
import java.util.Locale

class ReportAdapter(
    private val reportList: ArrayList<Reports>,
    private val context: Context  // Add Context as a constructor parameter
) : RecyclerView.Adapter<ReportAdapter.ViewHolder>() {

    class ViewHolder(val binding: ReportHistoryCardviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ReportHistoryCardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return reportList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = reportList[position]
        holder.apply {
            binding.apply {
                // Set title and description
                txtTitleHistory.text = currentItem.title
                txtDescriptionHistory.text = currentItem.description

                // Set image if available, otherwise set a default image
                if (!currentItem.imageUrl.isNullOrEmpty()) {
                    Picasso.get().load(currentItem.imageUrl).into(imgReportHistory)
                } else {
                    imgReportHistory.setImageResource(R.drawable.logo)
                }

                // Display address based on whether GeoPoint or manually entered address is available
                if (currentItem.currentLocation != null) {
                    // Convert GeoPoint to address using reverse geocoding
                    val geoPoint = currentItem.currentLocation
                    val address = getAddressFromGeoPoint(geoPoint)
                    txtLocationHistory.text = address ?: "Unknown location"
                } else if (!currentItem.address.isNullOrEmpty()) {
                    // Display manually entered address
                    txtLocationHistory.text = currentItem.address
                } else {
                    txtLocationHistory.text = "No address available"
                }
            }
        }
    }

    // Reverse geocode the GeoPoint to get the address as a String
    private fun getAddressFromGeoPoint(geoPoint: GeoPoint): String? {
        val geocoder = Geocoder(context, Locale.getDefault())  // Use the passed context
        return try {
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                address.getAddressLine(0)  // Get the full address
            } else {
                null  // No address found
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
