import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import za.co.varsitycollege.st10215473.pank.R

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: MapView
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var radiusDropdown: Spinner
    private lateinit var categoryDropdown: Spinner
    private lateinit var distance : TextView
    private lateinit var category: TextView
    private val REQUEST_CODE_TRANSLATION = 1001  // Request code for SettingsPage

    companion object {
        const val LOCATION_REQUEST_CODE = 1000
        const val MAX_DISTANCE_KM = 50
    }

    //OpenAI. (2024). Conversation on Displaying Map Locations. Available at https://www.openai.com
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        distance = view.findViewById(R.id.tvDistance)
        category = view.findViewById(R.id.tvCategory)

        radiusDropdown = view.findViewById(R.id.spnRadius) // For radius
        categoryDropdown = view.findViewById(R.id.spnReport) // For category

// Load and apply the saved language when the fragment opens
        val savedLanguage = loadLanguagePreference()
        if (savedLanguage != null) {
            applySavedLanguage(savedLanguage)
        }

        return view

    }

    data class CategoryItem(val name: String, val iconResId: Int)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize dropdowns
        radiusDropdown = view.findViewById(R.id.spnRadius) // For radius
        categoryDropdown = view.findViewById(R.id.spnReport) // For category

        // Set up radius dropdown
        val radiusOptions = arrayOf("Nearby", "Suburb", "Province", "Country")
        val radiusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, radiusOptions)
        radiusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        radiusDropdown.adapter = radiusAdapter
        radiusDropdown.setSelection(0) // Default to 10km

        // Set up category dropdown with icons
        val categoryOptions = arrayOf(
            CategoryItem("All Reports", R.drawable.logo),
            CategoryItem("Wildfire", R.drawable.fire_emoji),
            CategoryItem("Suspicious Activity", R.drawable.suspicious),
            CategoryItem("Lost Pet", R.drawable.pawprint),
            CategoryItem("Crime", R.drawable.crime),
            CategoryItem("Vandalism", R.drawable.vandalism),
            CategoryItem("Excessive Noise", R.drawable.noisy),
            CategoryItem("Missing Person", R.drawable.missing),
            CategoryItem("Other", R.drawable.menu)
        )

        // Custom adapter for the spinner
        val categoryAdapter = object : ArrayAdapter<CategoryItem>(requireContext(), R.layout.dropdown_item, categoryOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getCustomView(position, convertView, parent)
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getCustomView(position, convertView, parent)
            }

            private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
                val inflater = LayoutInflater.from(context)
                val view = convertView ?: inflater.inflate(R.layout.dropdown_item, parent, false)

                val icon = view.findViewById<ImageView>(R.id.spinnerIcon)
                val text = view.findViewById<TextView>(R.id.spinnerText)

                val categoryItem = getItem(position)

                // Set the icon and text for each category item
                categoryItem?.let {
                    icon.setImageResource(it.iconResId)
                    text.text = it.name
                }

                return view
            }
        }

        // Set the adapter to the category spinner
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

    // Override onActivityResult to check if language update was made
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_TRANSLATION && resultCode == AppCompatActivity.RESULT_OK) {
            // Reapply translation when coming back from SettingsPage
            val savedLanguage = loadLanguagePreference()
            if (savedLanguage != null) {
                applySavedLanguage(savedLanguage)  // Reapply the language
            }
        }
    }

    // Method to load the saved language from SharedPreferences
    private fun loadLanguagePreference(): String? {
        val sharedPref: SharedPreferences = requireActivity().getSharedPreferences("AppSettings", AppCompatActivity.MODE_PRIVATE)
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
                translateProfileFragmentTexts(translator)
                translateDropdownTexts(translator) // Call the method to translate dropdown texts
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to apply saved language", Toast.LENGTH_SHORT).show()
            }
    }

    // Translate the text of the buttons in ProfileFragment
    private fun translateProfileFragmentTexts(translator: Translator) {
        val textsToTranslate = listOf(
            "Select a Distance", "Select a Category"
        )

        val translatedTexts = mutableListOf<String>()

        for (text in textsToTranslate) {
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    translatedTexts.add(translatedText)
                    if (translatedTexts.size == textsToTranslate.size) {
                        // Apply translations to the respective buttons
                        distance.text = translatedTexts[0]
                        category.text = translatedTexts[1]
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Translation failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Translate dropdown texts
    private fun translateDropdownTexts(translator: Translator) {
        val radiusOptions = arrayOf("Nearby", "Suburb", "Province", "Country")
        val categoryOptions = arrayOf(
            "All Reports",
            "Wildfire",
            "Suspicious Activity",
            "Lost Pet",
            "Crime",
            "Vandalism",
            "Excessive Noise",
            "Missing Person",
            "Other"
        )

        val allOptionsToTranslate = radiusOptions + categoryOptions
        val translatedTexts = mutableListOf<String>()

        for (text in allOptionsToTranslate) {
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    translatedTexts.add(translatedText)
                    // Update dropdowns after translating all options
                    if (translatedTexts.size == allOptionsToTranslate.size) {
                        updateDropdowns(radiusOptions, translatedTexts.take(radiusOptions.size).toTypedArray(),
                            translatedTexts.drop(radiusOptions.size).toTypedArray())
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Translation failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val originalCategoryItems = arrayOf(
        CategoryItem("All Reports", R.drawable.logo),
        CategoryItem("Wildfire", R.drawable.fire_emoji),
        CategoryItem("Suspicious Activity", R.drawable.suspicious),
        CategoryItem("Lost Pet", R.drawable.pawprint),
        CategoryItem("Crime", R.drawable.crime),
        CategoryItem("Vandalism", R.drawable.vandalism),
        CategoryItem("Excessive Noise", R.drawable.noisy),
        CategoryItem("Missing Person", R.drawable.missing),
        CategoryItem("Other", R.drawable.menu)
    )

    private fun updateDropdowns(originalRadiusOptions: Array<String>, translatedRadiusOptions: Array<String>, translatedCategoryOptions: Array<String>) {
        // Update radius dropdown
        val radiusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, translatedRadiusOptions)
        radiusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        radiusDropdown.adapter = radiusAdapter

        // Update category dropdown with translated options while retaining the original icons
        val categoryAdapter = object : ArrayAdapter<CategoryItem>(requireContext(), R.layout.dropdown_item, originalCategoryItems.mapIndexed { index, item ->
            CategoryItem(translatedCategoryOptions[index], item.iconResId)
        }) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getCustomView(position, convertView, parent)
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getCustomView(position, convertView, parent)
            }

            private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
                val inflater = LayoutInflater.from(context)
                val view = convertView ?: inflater.inflate(R.layout.dropdown_item, parent, false)

                val icon = view.findViewById<ImageView>(R.id.spinnerIcon)
                val text = view.findViewById<TextView>(R.id.spinnerText)

                val categoryItem = getItem(position)

                // Set the icon and text for each category item
                categoryItem?.let {
                    icon.setImageResource(it.iconResId)
                    text.text = it.name
                }

                return view
            }
        }

        // Set the adapter to the category spinner
        categoryDropdown.adapter = categoryAdapter
    }

    private fun refreshMapWithSelectedOptions() {
        // Define the radius options
        val radiusOptions = listOf("Nearby", "Suburb", "Province", "Country")
        val radiusValues = listOf(10.0, 50.0, 1000.0, 50000.0)

        // Get the selected radius index from the dropdown
        val selectedRadiusIndex = radiusDropdown.selectedItemPosition
        val selectedRadius = radiusValues.getOrElse(selectedRadiusIndex) { 10.0 } // Default to 10km

        // Get the selected category index from the dropdown
        val selectedCategoryIndex = categoryDropdown.selectedItemPosition
         val selectedCategory = categoryDropdown.getItemAtPosition(selectedCategoryIndex)

        // Check permissions and update the map
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    mMap.clear()
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("Current Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )
                    getReports(it, selectedRadius, selectedCategoryIndex)
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
        val radiusOptions = arrayOf(10.0, 50.0, 1000.0, 50000.0) // In kilometers

        val selectedRadius = if (radiusDropdown.selectedItemPosition in radiusOptions.indices) {
            radiusOptions[radiusDropdown.selectedItemPosition]
        } else {
            10.0
        }

        val selectedCategoryIndex = categoryDropdown.selectedItemPosition

        getReports(currentLocation, selectedRadius, selectedCategoryIndex)
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

    private fun getReports(currentLocation: Location, selectedRadius: Double, selectedCategoryIndex: Int) {
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

                        val categoryNames = originalCategoryItems.map { it.name }

                        val selectedCategoryName = if (selectedCategoryIndex > 0 && selectedCategoryIndex < categoryNames.size) {
                            categoryNames[selectedCategoryIndex]
                        } else {
                            "Report a Wildfire"
                        }

                        if (distanceInKm <= selectedRadius &&
                            (selectedCategoryIndex == 0 || title.contains(selectedCategoryName))) {

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