package za.co.varsitycollege.st10215473.pank

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.varsitycollege.st10215473.pank.data.ReportEntity

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReports(reports: List<ReportEntity>)

    @Query("SELECT * FROM reports WHERE userId = :userId")
    suspend fun getReportsByUserId(userId: String?): List<ReportEntity>

    @Query("DELETE FROM reports WHERE id = :reportId")
    suspend fun deleteReport(reportId: Int)

    @Query("DELETE FROM reports")
    suspend fun deleteAllReports()
}