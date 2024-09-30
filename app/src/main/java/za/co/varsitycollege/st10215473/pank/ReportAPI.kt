package za.co.varsitycollege.st10215473.pank


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import za.co.varsitycollege.st10215473.pank.data.Reports

interface ReportApi {

    @GET("api/report/{userId}")
    fun getReports(@Path("userId") userId: String): Call<List<Reports>>

    @POST("api/report")
    fun createReport(@Body report: Reports): Call<Reports>
}
