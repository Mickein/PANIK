package za.co.varsitycollege.st10215473.pank.data

import com.google.firebase.firestore.GeoPoint

class Reports (
val title: String? = null,
val description: String? = null,
val location: GeoPoint? = null,
val userId: String? = null,
val timestamp: Long? = null,
val imageUrl: String? = null
){
    constructor(): this("","",null,"",0,"")
}