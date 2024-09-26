import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.pank.R

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: MapView
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    // Spinner fields
    private lateinit var radiusDropdown: Spinner
    private lateinit var categoryDropdown: Spinner

    companion object {
        const val LOCATION_REQUEST_CODE = 1000
        const val MAX_DISTANCE_KM = 50 // Default maximum distance
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize dropdowns
        radiusDropdown = view.findViewById(R.id.spnRadius) // For radius
        categoryDropdown = view.findViewById(R.id.spnReport) // For category

        // Set up radius dropdown
        val radiusOptions = arrayOf("10km", "50km", "1000km", "50000km")
        val radiusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, radiusOptions)
        radiusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        radiusDropdown.adapter = radiusAdapter
        radiusDropdown.setSelection(0) // Default to 10km

        // Set up category dropdown
        val categoryOptions = arrayOf("All Reports", "Wildfire", "Suspicious Activity", "Lost Pet", "Crime", "Vandalism", "Excessive Noise", "Missing Person", "Other")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryOptions)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoryDropdown.adapter = categoryAdapter
        categoryDropdown.setSelection(0) // Default to All Reports

        map = view.findViewById(R.id.mapView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        map.onCreate(savedInstanceState)
        map.getMapAsync(this)

        // Register ActivityResultLauncher for permissions
        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                checkLocation()
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_LONG).show()
            }
        }

        // Add listeners for spinner changes
        radiusDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Update map based on the new radius
                refreshMapWithSelectedOptions()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        categoryDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Update map based on the new category
                refreshMapWithSelectedOptions()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun refreshMapWithSelectedOptions() {
        // Get the selected radius and category
        val selectedRadius = radiusDropdown.selectedItem.toString().removeSuffix("km").toDouble()
        val selectedCategory = categoryDropdown.selectedItem.toString()

        // Check if location permissions are granted before updating the map
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    mMap.clear()

                    // Add the current location marker back
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("Current Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )

                    // Fetch and display reports based on the selected radius and category
                    getReports(it, selectedRadius, selectedCategory)
                }
            }
        }
    }


    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        val sydney = LatLng(-34.0, 151.0)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f))
        checkLocationPermissionsAndDisplay()
    }

    private fun checkLocationPermissionsAndDisplay() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            checkLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun checkLocation() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getCurrentLocation()
        } else {
            showDialogBox()
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))

                // Current location marker
                mMap.addMarker(
                    MarkerOptions()
                        .position(currentLatLng)
                        .title("Current Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )

                // Fetch and display reports based on selected radius and category
                displayNearbyReports(it)
            }
        }
    }

    private fun displayNearbyReports(currentLocation: Location) {
        val selectedRadius = radiusDropdown.selectedItem.toString().removeSuffix("km").toDouble()
        val selectedCategory = categoryDropdown.selectedItem.toString()

        getReports(currentLocation, selectedRadius, selectedCategory) // Fetch reports based on filters
    }

    private fun showDialogBox() {
        AlertDialog.Builder(requireContext()).setTitle("Enable Location Services")
            .setMessage("Location services required").setPositiveButton("Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel", null).show()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        map.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map.onSaveInstanceState(outState)
    }

    private fun getReports(currentLocation: Location, selectedRadius: Double, selectedCategory: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("reports").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val description = document.getString("description") ?: "No Description"
                    val title = document.getString("title") ?: "Other"
                    val geoPoint = document.getGeoPoint("location")

                    if (geoPoint != null) {
                        val latitude = geoPoint.latitude
                        val longitude = geoPoint.longitude

                        val reportLocation = Location("").apply {
                            this.latitude = latitude
                            this.longitude = longitude
                        }

                        val distanceInMeters = currentLocation.distanceTo(reportLocation)
                        val distanceInKm = distanceInMeters / 1000

                        // Check if the report is within the selected radius and matches the category
                        if (distanceInKm <= selectedRadius && (title.contains(selectedCategory) || selectedCategory == "All Reports")) {
                            val reportLatLng = LatLng(latitude, longitude)

                            val icon = when (title) {
                                "Report a Wildfire" -> BitmapDescriptorFactory.fromBitmap(
                                    getBitmapFromDrawable(R.drawable.fire_emoji, 90, 90)
                                )
                                "Report Suspicious Activity" -> BitmapDescriptorFactory.fromBitmap(
                                    getBitmapFromDrawable(R.drawable.suspicious, 90, 90)
                                )
                                "Report Lost Pet" -> BitmapDescriptorFactory.fromBitmap(
                                    getBitmapFromDrawable(R.drawable.pawprint, 90, 90)
                                )
                                "Report A Crime" -> BitmapDescriptorFactory.fromBitmap(
                                    getBitmapFromDrawable(R.drawable.crime, 90, 90)
                                )
                                "Report Missing Person" -> BitmapDescriptorFactory.fromBitmap(
                                    getBitmapFromDrawable(R.drawable.missing, 90, 90)
                                )
                                "Report Vandalism" -> BitmapDescriptorFactory.fromBitmap(
                                    getBitmapFromDrawable(R.drawable.vandalism, 90, 90)
                                )
                                "Report Excessive Noise" -> BitmapDescriptorFactory.fromBitmap(
                                    getBitmapFromDrawable(R.drawable.noisy, 90, 90)
                                )
                                "Other" -> BitmapDescriptorFactory.fromBitmap(
                                    getBitmapFromDrawable(R.drawable.menu, 90, 90)
                                )
                                else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                            }

                            // Add marker for the report
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(reportLatLng)
                                    .title(description)
                                    .icon(icon)
                            )
                        }
                    }
                }
            }
    }

    private fun getBitmapFromDrawable(drawableId: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableId) ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
