package `in`.vakrangee.crmCalling.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SmartTrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackData(data: SmartTrackEntity)

//    @Query("SELECT * FROM smart_track WHERE isSynced = 0")
//    suspend fun getUnsyncedData(): List<SmartTrackEntity>

    @Query("DELETE FROM smart_track WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE smart_track SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    @Query("SELECT COUNT(*) FROM smart_track WHERE isSynced = 0") // Update table name
    suspend fun getUnsyncedCount(): Int

    @Query("SELECT * FROM smart_track WHERE isSynced = 0 ORDER BY id ASC")
    suspend fun getUnsyncedData(): List<SmartTrackEntity>
}
