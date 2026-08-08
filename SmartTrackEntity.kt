package `in`.vakrangee.crmCalling.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_track")
data class SmartTrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vkid: String,
    val latitude: Double,
    val longitude: Double,
    val batteryPercentage: Int,
    val address: String,
    val macID: String,
    val deviceName: String,
    val device: String,
    val OSVersion: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val mobileDateTime: String,

    // ✅ NEW — full request context, captured at the moment of the location fix,
    // not re-computed later at sync time. Nullable + defaulted so existing
    // SmartTrackEntity(...) constructor calls elsewhere still compile unchanged.
    val speed: Double? = null,
    val accuracy: Double? = null,
    val bearing: Double? = null,
    val altitude: Double? = null,
    val provider: String? = null,
    val activityType: String? = null,
    val isMockLocation: Boolean? = null,
    val isGpsEnabled: Boolean? = null,
    val deviceID: String? = null,
    val appVersion: String? = null,
    val networkType: String? = null,
    val signalStrength: Int? = null
)

