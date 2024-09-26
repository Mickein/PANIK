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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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

    companion object {
        const val LOCATION_REQUEST_CODE = 1000
        const val MAX_DISTANCE_KM = 50 // 50 km radius
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map = view.findViewById(R.id.mapView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        map.onCreate(savedInstanceState)
        map.getMapAsync(this)

        // Register the ActivityResultLauncher for permission requests
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

                // Fetch and display reports within 50 km
                displayNearbyReports(it)
            }
        }
    }


    private fun displayNearbyReports(currentLocation: Location) {
        getReports(currentLocation) // Fetch reports and display them
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

    private fun getReports(currentLocation: Location) {
        val db = FirebaseFirestore.getInstance()

        db.collection("reports").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val description = document.getString("description") ?: "No Description"
                    val title = document.getString("title")
                    val geoPoint = document.getGeoPoint("location")

                    // Check if the geoPoint is not null
                    if (geoPoint != null) {
                        val latitude = geoPoint.latitude
                        val longitude = geoPoint.longitude

                        val reportLocation = Location("").apply {
                            this.latitude = latitude
                            this.longitude = longitude
                        }

                        // Calculate distance from current location
                        val distanceInMeters = currentLocation.distanceTo(reportLocation)
                        val distanceInKm = distanceInMeters / 1000

                        // If the report is within 50 km, add it to the map
                        if (distanceInKm <= MAX_DISTANCE_KM) {
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

                            mMap.addMarker(
                                MarkerOptions()
                                    .position(reportLatLng)
                                    .title(description)
                                    .icon(icon)
                            )
                        }
                    } else {
                        // Log or handle the case where location is missing or not a GeoPoint
                        Log.e("MapFragment", "Document ${document.id} does not have a valid GeoPoint for 'location'")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error fetching reports: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun getBitmapFromDrawable(resId: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(requireContext(), resId)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        // Resize the bitmap to match the size of a default marker
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }





}
