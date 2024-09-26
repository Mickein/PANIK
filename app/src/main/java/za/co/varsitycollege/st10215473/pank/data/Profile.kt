package za.co.varsitycollege.st10215473.pank.data

class Profile (
    val id: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val number: String? = null,
    val email: String? = null,
    val profilePic: String? = null,
    ){
    constructor():this("", "", "", "", "", "")
}