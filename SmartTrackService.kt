package `in`.vakrangee.crmCalling.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import `in`.vakrangee.crmCalling.NetworkUtil
import `in`.vakrangee.crmCalling.model.AppDatabase
import `in`.vakrangee.crmCalling.model.SmartTrackEntity
import `in`.vakrangee.hrms.R
import `in`.vakrangee.hrms.dto.repositories.DefaultPersistenceDataSource
import `in`.vakrangee.hrms.ui.stManager.data.InsertLocationRequest
import `in`.vakrangee.hrms.ui.stManager.data.TrackingRequest
import `in`.vakrangee.hrms.ui.stManager.events.PunchStateHolder
import `in`.vakrangee.hrms.ui.stManager.events.RetrofitExt.awaitResponse
import `in`.vakrangee.hrms.ui.stManager.events.SmartTrackEvents
import `in`.vakrangee.hrms.ui.stManager.viewmodel.InsertSmartTrackViewModel
import `in`.vakrangee.hrms.ui.travelReport.api.RemoteRepository
import `in`.vakrangee.hrms.ui.travelReport.utils.PrefManager
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class SmartTrackService : Service() {

    private var trackingJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var insertViewModel: InsertSmartTrackViewModel
    private var lastSavedLocation: android.location.Location? = null
    private var lastSavedTime: Long = 0L
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var activityRecognitionPendingIntent: PendingIntent
    private var statusCheckJob: Job? = null
    @Volatile
    private var isSyncRunning = false
    private var empId: String = ""

    private val STATUS_CHECK_INTERVAL = 5 * 60 * 1000L
    companion object {
        private const val TAG = "SmartTrackService"
        private const val CHANNEL_ID = "SmartTrackChannel"
        private const val NOTIFICATION_ID = 1001
        private const val MIN_DISTANCE_METERS = 100f
        private const val DEFAULT_ACTIVITY_TYPE = "STOP"
        const val EXTRA_VKID = "extra_vkid"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        insertViewModel = InsertSmartTrackViewModel()
        createNotificationChannel()
        val pref = PrefManager(this)
        empId = pref.getEmployeeId() ?: ""
    }

    private fun getActivityRecognitionPendingIntent(): PendingIntent {
        val intent = Intent(this, ActivityRecognitionReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        else
            PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(this, 0, intent, flags)
    }

    private fun startActivityRecognitionUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "❌ ACTIVITY_RECOGNITION permission missing — skipping activity detection")
            return
        }

        ActivityRecognitionReceiver.resetActivityType()

        activityRecognitionClient = ActivityRecognition.getClient(this)
        activityRecognitionPendingIntent = getActivityRecognitionPendingIntent()

        activityRecognitionClient
            .requestActivityUpdates(10_000L, activityRecognitionPendingIntent)
            .addOnSuccessListener { Log.d(TAG, "✅ Activity recognition updates started") }
            .addOnFailureListener { Log.e(TAG, "❌ Failed to start activity updates", it) }
    }

    private fun stopActivityRecognitionUpdates() {
        if (!::activityRecognitionClient.isInitialized) return

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "ACTIVITY_RECOGNITION permission missing")
            return
        }

        try {
            activityRecognitionClient
                .removeActivityUpdates(activityRecognitionPendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "🛑 Activity recognition updates stopped")
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to stop activity recognition updates", it)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while removing activity updates", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service start requested")

        if (intent == null) {
            startForegroundInternal()
            serviceScope.launch {
                val stillIn = verifyPunchStatusFromServer()
                if (!stillIn) {

                    Log.d(TAG, "Server says Punch OUT after restart")

                    if (NetworkUtil.isNetworkAvailable(this@SmartTrackService)) {
                        syncPendingLocations()
                    }

                    stopSelf()
                } else {
                    startActivityRecognitionUpdates()
                    SignalStrengthMonitor.start(this@SmartTrackService)
                    startLocationTracking()
                    startPunchStatusPolling()
                }
            }
            return START_STICKY
        }

        // Normal path: Activity told us to start because API said punchStatus == "IN"
        PunchStateHolder.isPunchedIn = true
        PunchStateHolder.vkid = intent.getStringExtra(EXTRA_VKID) ?: PunchStateHolder.vkid

        lastSavedLocation = null
        lastSavedTime = 0L

        startForegroundInternal()
        startActivityRecognitionUpdates()
        SignalStrengthMonitor.start(this)
        startLocationTracking()
        startPunchStatusPolling()
        return START_STICKY
    }


    private fun startForegroundInternal() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startLocationTracking() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
//            while (isActive) {
//                if (!PunchStateHolder.isPunchedIn) {
//                    Log.w(TAG, "🚫 Punch status became false — stopping service")
//                    stopActivityRecognitionUpdates()
//                    stopSelf()
//                    break
//                }
//                try {
//                    Log.d(TAG, "Employee punched IN - capturing location")
//                    insertAndSyncLocation()
//                } catch (e: Exception) {
//                    Log.e(TAG, "Tracking loop error", e)
//                }
//                delay(8000L)
//            }

            while (isActive) {

                if (!PunchStateHolder.isPunchedIn) {

                    Log.d(TAG, "Punch Out detected")

                    if (NetworkUtil.isNetworkAvailable(this@SmartTrackService)) {
                        syncPendingLocations()
                    }

                    statusCheckJob?.cancel()
                    stopActivityRecognitionUpdates()
                    SignalStrengthMonitor.stop()
                    stopSelf()
                    break
                }

                insertAndSyncLocation()

                delay(8000L)
            }
        }
    }

    private suspend fun syncPendingLocations() {

        if (isSyncRunning) {
            Log.d(TAG, "🔄 Sync already running, skipping...")
            return
        }

        isSyncRunning = true

        try {

            val db = AppDatabase.getDatabase(this)
            val unsynced = db.smartTrackDao().getUnsyncedData()

            Log.d(TAG, "📦 Found ${unsynced.size} unsynced records")

            for (item in unsynced) {

                if (!NetworkUtil.isNetworkAvailable(this)) {
                    Log.w(TAG, "🌐 Lost internet during sync")
                    break
                }

                try {

                    val request = InsertLocationRequest(
                        vkid = item.vkid,
                        latitude = item.latitude,
                        longitude = item.longitude,
                        batteryPercentage = item.batteryPercentage,
                        address = item.address,
                        macID = item.macID,
                        deviceName = item.deviceName,
                        device = item.device,
                        OSVersion = item.OSVersion,
                        speed = item.speed,
                        accuracy = item.accuracy,
                        bearing = item.bearing,
                        altitude = item.altitude,
                        provider = item.provider,
                        activityType = item.activityType,
                        isMockLocation = item.isMockLocation,
                        isGpsEnabled = item.isGpsEnabled,
                        isInternetAvailable = true,
                        deviceID = item.deviceID,
                        appVersion = item.appVersion,
                        networkType = item.networkType,
                        signalStrength = item.signalStrength,
                        mobileDateTime = item.mobileDateTime
                    )

                    val response = insertViewModel.insertUserLocationSync(request)

                    if (response?.status == true) {
                        db.smartTrackDao().deleteById(item.id)
                        Log.d(TAG, "✅ Synced ID: ${item.id}")
                    } else {
                        Log.w(TAG, "❌ Server rejected ID: ${item.id}")
                        break
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Sync failed ID: ${item.id}", e)
                    break
                }
            }

            Log.d(TAG, "🏁 Remaining unsynced: ${db.smartTrackDao().getUnsyncedCount()}")

        } finally {
            isSyncRunning = false
        }
    }
    private fun startPunchStatusPolling() {
        statusCheckJob?.cancel()
        statusCheckJob = serviceScope.launch {
            while (isActive) {
                delay(STATUS_CHECK_INTERVAL)
                val stillIn = verifyPunchStatusFromServer()
                if (!stillIn) {

                    Log.w(TAG, "🌙 Server says punch closed")

                    if (NetworkUtil.isNetworkAvailable(this@SmartTrackService)) {
                        syncPendingLocations()
                    }

                    SmartTrackEvents.autoPunchOut.tryEmit("OUT")

                    stopActivityRecognitionUpdates()
                    SignalStrengthMonitor.stop()
                    stopSelf()

                    break
                }
            }
        }
    }


    private suspend fun verifyPunchStatusFromServer(): Boolean {
        val vkid = PunchStateHolder.vkid.ifBlank { empId}
        if (vkid.isBlank()) return PunchStateHolder.isPunchedIn // nothing to check against

        if (!NetworkUtil.isNetworkAvailable(this@SmartTrackService)) {
            // Offline: can't confirm either way. Case 3 — keep whatever we last knew,
            // location loop keeps saving locally, sync resumes once network returns.
            Log.d(TAG, "🌐 Offline — skipping server verification this cycle")
            return PunchStateHolder.isPunchedIn
        }

        return try {
            val request =
                TrackingRequest(vkid = vkid, startDate = "", endDate = "", filter = "MTD")
            val response = RemoteRepository.fetchTrackingSmartTrackData(request).awaitResponse()

            if (!response.isSuccessful) return PunchStateHolder.isPunchedIn

            val status = response.body()?.punchStatus
            val stillIn = status == "IN"
            PunchStateHolder.isPunchedIn = stillIn
            stillIn
        } catch (e: Exception) {
            Log.e(TAG, "Punch verification failed", e)
            PunchStateHolder.isPunchedIn // network hiccup — don't kill tracking on a single failure
        }
    }


    private fun getNetworkType(): String {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return "NONE"
            val caps = cm.getNetworkCapabilities(network) ?: return "NONE"

            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "MOBILE"
                else -> "OTHER"
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = cm.activeNetworkInfo

            @Suppress("DEPRECATION")
            when {
                networkInfo == null || !networkInfo.isConnected -> "NONE"

                networkInfo.type == ConnectivityManager.TYPE_WIFI -> "WIFI"

                networkInfo.type == ConnectivityManager.TYPE_MOBILE -> "MOBILE"

                else -> "OTHER"
            }
        }
    }

    private fun isGpsEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun sanityCheckActivityType(rawActivity: String, speedKmph: Double): String {
        return when {
            speedKmph >= 25.0 -> "IN_VEHICLE"
            speedKmph >= 12.0 && (rawActivity == "STILL" || rawActivity == "WALKING" || rawActivity == "ON_FOOT") ->
                "IN_VEHICLE"
            speedKmph < 3.0 && rawActivity == "IN_VEHICLE" -> "STILL"
            speedKmph < 8.0 && rawActivity == "RUNNING" -> "WALKING"
            else -> rawActivity.ifBlank { DEFAULT_ACTIVITY_TYPE }
        }
    }
    private suspend fun insertAndSyncLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this@SmartTrackService, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "❌ Location permission missing")
                return
            }

            // Always request a fresh high-accuracy location
            val location = fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await() ?: run {
                Log.w(TAG, "❌ No location available")
                return
            }

            // ---------- Speed calculation ----------
            var speedKmph = 0.0
            if (location.hasSpeed() && location.speed > 0f) {
                speedKmph = location.speed * 3.6
            } else if (lastSavedLocation != null && lastSavedTime > 0L) {
                val distanceMeters = location.distanceTo(lastSavedLocation!!)
                val timeSeconds = (location.time - lastSavedTime) / 1000.0
                if (timeSeconds > 0) {
                    speedKmph = (distanceMeters / timeSeconds) * 3.6
                }
            }
            if (speedKmph.isNaN() || speedKmph.isInfinite() || speedKmph < 0) {
                speedKmph = 0.0
            }

            val accuracy = location.accuracy
            if (accuracy > 65f) {
                Log.w(TAG, "❌ Skipped: Low accuracy ($accuracy m)")
                return
            }

            // ---------- 100 m + time filter ----------
            val now = System.currentTimeMillis()
            val timeSinceLastSave = now - lastSavedTime
            val MAX_STATIONARY_INTERVAL_MS = 5 * 60 * 1000L   // 5 minutes heartbeat

            if (lastSavedLocation != null) {
                val distance = location.distanceTo(lastSavedLocation!!)

                val shouldSkipBecauseOfDistance = distance < MIN_DISTANCE_METERS
                val shouldForceBecauseOfTime = timeSinceLastSave >= MAX_STATIONARY_INTERVAL_MS

                if (shouldSkipBecauseOfDistance && !shouldForceBecauseOfTime) {
                    Log.d(
                        TAG,
                        "⏭️ Still within ${MIN_DISTANCE_METERS}m (moved ${"%.1f".format(distance)} m) " +
                                "and only ${timeSinceLastSave / 1000}s since last save — skipping"
                    )
                    return
                }

                if (shouldForceBecauseOfTime) {
                    Log.d(TAG, "⏰ Force send: stationary for ${timeSinceLastSave / 1000}s")
                } else {
                    Log.d(TAG, "📍 Moved ${"%.1f".format(distance)} m — will save")
                }
            }

            // ---------- Rest of the method stays the same ----------
            val speed = String.format(Locale.US, "%.2f", speedKmph)
            val latitude = location.latitude
            val longitude = location.longitude

            val address = withContext(Dispatchers.IO) {
                try {
                    Geocoder(this@SmartTrackService, Locale.getDefault())
                        .getFromLocation(latitude, longitude, 1)
                        ?.firstOrNull()
                        ?.getAddressLine(0)
                        ?: "Unknown Location"
                } catch (e: Exception) {
                    Log.e(TAG, "Geocoder error", e)
                    "Unable to fetch address"
                }
            }

            val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPercentage =
                if (level > 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 0

            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            val vkid = empId
            val mobileDateTime =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val appVersion = try {
                packageManager.getPackageInfo(packageName, 0).versionName ?: ""
            } catch (e: Exception) {
                ""
            }

            val db = AppDatabase.getDatabase(this@SmartTrackService)
            val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                location.isMock
            else
                @Suppress("DEPRECATION") location.isFromMockProvider

            val rawActivityType = ActivityRecognitionReceiver.lastActivityType
            val currentActivityType = sanityCheckActivityType(rawActivityType, speedKmph)

            val entity = SmartTrackEntity(
                vkid = vkid,
                latitude = latitude,
                longitude = longitude,
                batteryPercentage = batteryPercentage,
                address = address,
                macID = deviceId,
                deviceName = "${Build.BRAND} ${Build.MODEL}",
                speed = speed.toDoubleOrNull() ?: 0.0,
                device = "Android",
                OSVersion = "Android ${Build.VERSION.RELEASE}",
                mobileDateTime = mobileDateTime,
                isSynced = false,
                accuracy = accuracy.toDouble(),
                bearing = location.bearing.toDouble(),
                altitude = location.altitude,
                provider = location.provider,
                activityType = currentActivityType,
                isMockLocation = isMock,
                isGpsEnabled = isGpsEnabled(),
                deviceID = deviceId,
                appVersion = appVersion,
                networkType = getNetworkType(),
                signalStrength = SignalStrengthMonitor.lastSignalLevel
            )

            db.smartTrackDao().insertTrackData(entity)
            Log.d(
                TAG,
                "💾 Saved locally: vkid=$vkid, lat=$latitude, lng=$longitude, " +
                        "speed=${entity.speed} km/h, activity=$currentActivityType"
            )

            lastSavedLocation = location
            lastSavedTime = now          // use wall-clock time for the interval check

            // ---------- Sync part (unchanged) ----------
            if (!NetworkUtil.isNetworkAvailable(this@SmartTrackService)) {
                Log.d(TAG, "🌐 No internet — staying offline")
                return
            }
//
//            val unsynced = db.smartTrackDao().getUnsyncedData()
//            Log.d(TAG, "📦 Found ${unsynced.size} unsynced records")
//
//            for (item in unsynced) {
//                if (!PunchStateHolder.isPunchedIn) {
//                    Log.w(TAG, "🚫 Punched out mid-sync — aborting remaining sync")
//                    break
//                }
//                if (!NetworkUtil.isNetworkAvailable(this)) {
//                    Log.w(TAG, "🌐 Lost internet mid-sync — will retry next cycle")
//                    break
//                }
//                try {
//                    val request = InsertLocationRequest(
//                        vkid = item.vkid,
//                        latitude = item.latitude,
//                        longitude = item.longitude,
//                        batteryPercentage = item.batteryPercentage,
//                        address = item.address,
//                        macID = item.macID,
//                        deviceName = item.deviceName,
//                        device = item.device,
//                        OSVersion = item.OSVersion,
//                        speed = item.speed,
//                        accuracy = item.accuracy,
//                        bearing = item.bearing,
//                        altitude = item.altitude,
//                        provider = item.provider,
//                        activityType = item.activityType,
//                        isMockLocation = item.isMockLocation,
//                        isGpsEnabled = item.isGpsEnabled,
//                        isInternetAvailable = true,
//                        deviceID = item.deviceID,
//                        appVersion = item.appVersion,
//                        networkType = item.networkType,
//                        signalStrength = item.signalStrength,
//                        mobileDateTime = item.mobileDateTime
//                    )
//
//                    val response = insertViewModel.insertUserLocationSync(request)
//                    if (response?.status == true) {
//                        db.smartTrackDao().deleteById(item.id)
//                        Log.d(TAG, "✅ Synced ID: ${item.id}")
//                    } else {
//                        Log.w(TAG, "❌ Sync rejected by server for ID: ${item.id} - ${response?.message}")
//                        break
//                    }
//                } catch (e: Exception) {
//                    Log.e(TAG, "❌ Sync failed ID: ${item.id} - ${e.message}")
//                    break
//                }
//            }

            if (NetworkUtil.isNetworkAvailable(this)) {
                syncPendingLocations()
            }
//            Log.d(TAG, "🏁 Sync done. Unsynced remaining: ${db.smartTrackDao().getUnsyncedCount()}")

        } catch (e: Exception) {
            Log.e(TAG, "💥 insertAndSyncLocation error", e)
        }
    }
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SmartTrack Active")
            .setContentText("Tracking location (Punched IN)")
            .setSmallIcon(R.drawable.ic_location_fetching)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "SmartTrack Service", NotificationManager.IMPORTANCE_LOW).apply { description = "Location tracking service" }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "🛑 Service destroyed")
        statusCheckJob?.cancel()          // ⬅️ ADD
        stopActivityRecognitionUpdates()
        SignalStrengthMonitor.stop()
        trackingJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}