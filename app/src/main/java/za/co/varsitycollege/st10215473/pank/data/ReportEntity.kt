package za.co.varsitycollege.st10215473.pank.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.GeoPoint

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val location: GeoPoint,
    val userId: String,
    val timestamp: Long,
    var imageUrl: String?
)

