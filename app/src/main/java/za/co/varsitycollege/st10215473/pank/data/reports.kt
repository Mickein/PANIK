package za.co.varsitycollege.st10215473.pank.data

class Report(
    val title: String,
    val description: String,
    val location: LocationData,
    val userId: String,
    val timestamp: Long,
    val imageUrl: String? = null // Nullable since it might not always be present
)

class LocationData(
    val latitude: Double,
    val longitude: Double
)