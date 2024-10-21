package za.co.varsitycollege.st10215473.pank

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.DialogInterface
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.Uri
import android.util.Log
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import za.co.varsitycollege.st10215473.pank.data.ReportEntity
import java.io.File
import java.util.Locale
import java.util.UUID

class ReportFragment : Fragment() {

    private lateinit var title: TextView
    private lateinit var description: EditText
    private lateinit var location: EditText
    private lateinit var locationCheckBox: CheckBox
    private lateinit var pictureCheckBox: CheckBox
    private lateinit var image: ImageView
    private lateinit var reportButton: Button
    private lateinit var wildfireButton: Button
    private lateinit var suspiciousButton: Button
    private lateinit var petButton: Button
    private lateinit var crimeButton: Button
    private lateinit var missingPersonButton: Button
    private lateinit var vandalismButton: Button
    private lateinit var noiseButton: Button
    private lateinit var otherButton: Button
    private lateinit var helpButton: Button
    private lateinit var panicButton: Button
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var uri: Uri? = null
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private var currentLat: Double? = null
    private var currentLng: Double? = null
    private lateinit var pickImage: ActivityResultLauncher<String>
    private var uploadedImage: Boolean = false;
    private lateinit var reportDao: ReportDao

    companion object {
        const val LOCATION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        reportDao = db.reportDao()

        // Initialize Firebase and Location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        firebaseRef = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        // Register activity result for location permission
        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                checkLocation()
            } else {
                Toast.makeText(context, "Fine location permission denied", Toast.LENGTH_LONG).show()
            }
        }

        // Register activity result for taking a picture
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
                if (isSuccess) {
                    uri?.let { image.setImageURI(it) }
                    image.setBackgroundResource(0)
                }
            }

        pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            image.setImageURI(uri)
            this.uri = uri
            image.setBackgroundResource(0)
        }

        uri = createUri()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_report, container, false)
        val reportForm = layoutInflater.inflate(R.layout.activity_report, null)

        //Report form components
        title = reportForm.findViewById(R.id.txtReportTitle)
        description = reportForm.findViewById(R.id.edtDescription)
        location = reportForm.findViewById(R.id.edtLocation)
        locationCheckBox = reportForm.findViewById(R.id.ckbCurrentLocation)
        pictureCheckBox = reportForm.findViewById(R.id.ckbUploadPicture)
        image = reportForm.findViewById(R.id.imgReportPicture)
        reportButton = reportForm.findViewById(R.id.btnReport)

        //fragment_report components
        suspiciousButton = view.findViewById(R.id.btnSus)
        wildfireButton = view.findViewById(R.id.btnWildfire)
        petButton = view.findViewById(R.id.btnPet)
        crimeButton = view.findViewById(R.id.btnCrime)
        missingPersonButton = view.findViewById(R.id.btnMissingPerson)
        vandalismButton = view.findViewById(R.id.btnVandalism)
        noiseButton = view.findViewById(R.id.btnNoise)
        otherButton = view.findViewById(R.id.btnOther)
        helpButton = view.findViewById(R.id.btnHelp)
        panicButton = view.findViewById(R.id.btnPanic)

        wildfireButton.setOnClickListener {
            showDialog(wildfireButton, reportForm)
        }
        suspiciousButton.setOnClickListener {
            showDialog(suspiciousButton, reportForm)
        }
        petButton.setOnClickListener {
            showDialog(petButton, reportForm)
        }
        crimeButton.setOnClickListener {
            showDialog(crimeButton, reportForm)
        }
        missingPersonButton.setOnClickListener {
            showDialog(missingPersonButton, reportForm)
        }
        vandalismButton.setOnClickListener {
            showDialog(vandalismButton, reportForm)
        }
        noiseButton.setOnClickListener {
            showDialog(noiseButton, reportForm)
        }
        otherButton.setOnClickListener {
            showDialog(otherButton, reportForm)
        }

        helpButton.setOnClickListener {
            Toast.makeText(requireContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }
        panicButton.setOnClickListener {
            Toast.makeText(requireContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show()
        }


        locationCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // When the checkbox is checked, check fine location permissions and get current location
                checkLocationPermissionsAndDisplay()
            }
        }

        pictureCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                uploadedImage = true
                image.visibility = View.VISIBLE
                image.setBackgroundResource(R.drawable.image_upload)

            } else {
                uploadedImage = false
                image.setImageResource(0)
                image.visibility = View.INVISIBLE
            }
        }

        image.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Select Option")
            builder.setItems(options) { dialogInterface: DialogInterface, which: Int ->
                when (which) {
                    0 -> {
                        checkCameraPermissionAndOpen()//Some code by CodingZest on Youtube: https://www.youtube.com/watch?v=9XSlbZN1yFg&t=761s
                    }

                    1 -> pickImage.launch("image/*")
                }
                dialogInterface.dismiss()
            }
            builder.show()
        }


        reportButton.setOnClickListener {
            submitReport()
        }

        return view
    }

    private fun checkCameraPermissionAndOpen() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            takePictureLauncher.launch(uri)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePictureLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createUri(): Uri {
        val imageFile = File(requireActivity().application.filesDir, "camera_photo.jpg")
        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            imageFile
        )
    }

    fun showDialog(button: Button, reportForm: View) {
        val formTitle = button.text.toString()
        title.text = formTitle

        // Check if the reportForm already has a parent and remove it
        val parent = reportForm.parent as? ViewGroup
        parent?.removeView(reportForm)

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(reportForm)
        val dialog = dialogBuilder.create()
        dialog.show()
    }


    private fun checkLocationPermissionsAndDisplay() {
        // Check if fine location permission is granted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkLocation()  // Permission already granted, proceed to check location
        } else {
            // Request fine location permission using the ActivityResultLauncher
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkLocation() {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getCurrentLocation()
        } else {
            showDialogBox()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, proceed with getting the location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    currentLat = it.latitude
                    currentLng = it.longitude
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDialogBox() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("GPS is disabled. Please enable GPS to get the current location.")
            .setPositiveButton("Enable") { dialog, id ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel") { dialog, id ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun submitReport() {
        val reportTitle = title.text.toString()
        val reportDescription = description.text.toString()

        if (reportDescription.isEmpty()) {
            Toast.makeText(context, "Please enter a description", Toast.LENGTH_SHORT).show()
            return
        }

        if (locationCheckBox.isChecked && (currentLat != null && currentLng != null)) {
            // Use current location
            saveReport(reportTitle, reportDescription, currentLat!!, currentLng!!)
        } else {
            // Use manually entered location
            val address = location.text.toString()
            convertAddressToCoordinatesAndSave(reportTitle, reportDescription, address)
        }
    }

    private fun convertAddressToCoordinatesAndSave(
        title: String,
        description: String,
        address: String
    ) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addressList = geocoder.getFromLocationName(address, 1)
        if (addressList != null && addressList.isNotEmpty()) {
            val location = addressList[0]
            val lat = location.latitude
            val lng = location.longitude
            saveReport(title, description, lat, lng)
        } else {
            Toast.makeText(context, "Invalid address", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveReport(title: String, description: String, lat: Double, lng: Double) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // Prepare report data
            val reportData = hashMapOf<String, Any>(
                "title" to title,
                "description" to description,
                "location" to GeoPoint(lat, lng),  // Store location as a GeoPoint
                "userId" to user.uid,
                "timestamp" to System.currentTimeMillis(),
                "imageUrl" to ""  // Initialize image URL as null
            )
            if(isOnline()){
                if (uploadedImage == true) {
                    // If the user added an image, upload it and store its URL
                    uploadImageAndStoreData(uri!!, reportData)
                } else {
                    // If no image, directly store the report data with imageUrl set to null
                    firebaseRef.collection("reports").add(reportData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Report successfully submitted", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Failed to submit report: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            else {
                // Device is offline, save the report locally in Room
                val reportEntity = ReportEntity(
                    title = title,
                    description = description,
                    location = GeoPoint(lat, lng),
                    userId = user.uid,
                    timestamp = System.currentTimeMillis(),
                    imageUrl = null // Set to null initially if we don't have the image path yet
                )

                // If the user has uploaded an image, save it locally
                if (uploadedImage == true) {
                    // Create a file path for the image
                    val imageFile = File(requireContext().filesDir, "images/${UUID.randomUUID()}.jpg")

                    // Save the image to the file
                    saveImageToLocalFile(uri!!, imageFile)

                    // Update the reportEntity with the local image path
                    reportEntity.imageUrl = imageFile.absolutePath  // Save the local image path
                }

                saveReportToRoom(reportEntity)
            }
        }
    }

    private fun saveImageToLocalFile(imageUri: Uri, imageFile: File) {
        try {
            requireContext().contentResolver.openInputStream(imageUri).use { inputStream ->
                imageFile.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
            }
            Toast.makeText(context, "Image saved locally", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("ImageSaveError", "Failed to save image: ${e.message}")
            Toast.makeText(context, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    private fun saveReportToRoom(reportEntity: ReportEntity) {
        // Use a coroutine or viewModelScope to save to Room
        CoroutineScope(Dispatchers.IO).launch {
            reportDao.insertReport(reportEntity)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Report saved offline", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageAndStoreData(imageUri: Uri, reportData: HashMap<String, Any>) {
        val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        imageRef.putFile(imageUri).addOnSuccessListener {
            // Once image is uploaded, get the download URL
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                // Update the report data with the image URL
                reportData["imageUrl"] = uri.toString()

                // Store report data with the image URL
                firebaseRef.collection("reports").add(reportData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Report successfully submitted with image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Failed to submit report: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Register a network callback to listen for connectivity changes
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        connectivityManager.registerNetworkCallback(builder.build(), object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Sync reports from Room to Firestore
                syncReportsWithFirestore()
            }
        })
    }

    private fun syncReportsWithFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser
        CoroutineScope(Dispatchers.IO).launch {
            val offlineReports = reportDao.getReportsByUserId(userId?.uid) // Fetch all reports from Room
            for (report in offlineReports) {
                val reportData = hashMapOf<String, Any>(
                    "title" to report.title,
                    "description" to report.description,
                    "location" to GeoPoint(report.location.latitude, report.location.longitude),
                    "userId" to report.userId,
                    "timestamp" to report.timestamp,
                    "imageUrl" to (report.imageUrl ?: "")  // Provide a default if null
                )

                try {
                    // If the image URL is not null, upload the image first
                    if (report.imageUrl != null) {
                        val imageUri = Uri.fromFile(File(report.imageUrl)) // Use the appropriate way to get the Uri
                        val imageUrl = uploadImageAndGetUrl(imageUri) // You need to implement this method
                        reportData["imageUrl"] = imageUrl // Update the report data with the new image URL
                    }

                    // Try to add the report to Firestore
                    firebaseRef.collection("reports").add(reportData).await() // Use await to wait for completion
                    // If successful, delete the report from Room
                    reportDao.deleteReport(report.id)
                } catch (e: Exception) {
                    // Handle any errors (e.g., log them)
                    Log.e("SyncError", "Failed to submit report: ${e.message}")
                }
            }
            // Notify user after processing all reports
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Sync complete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to upload the image and return the URL
    private suspend fun uploadImageAndGetUrl(imageUri: Uri): String {
        val imageRef =
            storageRef.child("images/${UUID.randomUUID()}.jpg") // Create a unique file name
        imageRef.putFile(imageUri).await() // Wait for the upload to complete
        return imageRef.downloadUrl.await().toString() // Get and return the download URL
    }

}


