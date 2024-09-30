package za.co.varsitycollege.st10215473.pank.data

import com.google.firebase.firestore.GeoPoint

class Reports (
val title: String? = null,
val description: String? = null,
val currentLocation: GeoPoint? = null,
val address: String? = null,
val userId: String? = null,
val timestamp: Long? = null,
val imageUrl: String? = null // Nullable since it might not always be present
){
    constructor(): this("","",null,"","",0,"")
}