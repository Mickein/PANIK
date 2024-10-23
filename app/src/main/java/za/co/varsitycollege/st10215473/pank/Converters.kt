package za.co.varsitycollege.st10215473.pank

import androidx.room.TypeConverter
import com.google.firebase.firestore.GeoPoint

class Converters {
    @TypeConverter
    fun fromGeoPoint(geoPoint: GeoPoint?): String {
        return "${geoPoint?.latitude},${geoPoint?.longitude}"
    }

    @TypeConverter
    fun toGeoPoint(data: String?): GeoPoint {
        val latlong = data?.split(",") ?: listOf("0.0", "0.0")
        return GeoPoint(latlong[0].toDouble(), latlong[1].toDouble())
    }
}
