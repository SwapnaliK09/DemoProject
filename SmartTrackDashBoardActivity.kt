package `in`.vakrangee.smartTrack.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import `in`.vakrangee.crmCalling.IOUtils
import `in`.vakrangee.crmCalling.NetworkUtil
import `in`.vakrangee.crmCalling.service.SmartTrackService
import `in`.vakrangee.hrms.R
import `in`.vakrangee.hrms.databinding.ActivitySmartTrackDashboardBinding
import `in`.vakrangee.hrms.databinding.LayoutReportFilterBottomSheetBinding
import `in`.vakrangee.hrms.dto.repositories.DefaultPersistenceDataSource
import `in`.vakrangee.hrms.ui.stManager.activity.EmployeeTimeLineActivity
import `in`.vakrangee.hrms.ui.stManager.constant.IntentConstants
import `in`.vakrangee.hrms.ui.stManager.data.EmployeeDetailsData
import `in`.vakrangee.hrms.ui.stManager.data.TimeFilterData
import `in`.vakrangee.hrms.ui.stManager.data.TrackingPlotData
import `in`.vakrangee.hrms.ui.stManager.data.TrackingRequest
import `in`.vakrangee.hrms.ui.stManager.data.TrackingResponse
import `in`.vakrangee.hrms.ui.stManager.dynamicform.activity.DynamicFormCreateTaskSmartTrackActivity
import `in`.vakrangee.hrms.ui.stManager.events.PunchStateHolder
import `in`.vakrangee.hrms.ui.stManager.events.SmartTrackEvents
import `in`.vakrangee.hrms.ui.stManager.viewmodel.SmartTrackSummaryViewModel
import `in`.vakrangee.hrms.ui.stManager.viewmodel.TrackingSmartTrackViewModel
import `in`.vakrangee.hrms.ui.travelReport.utils.DialogUtils.showCustomDialog
import `in`.vakrangee.hrms.ui.travelReport.utils.PrefManager
import `in`.vakrangee.smartTrack.request.TrackingINandOUTRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean


class SmartTrackDashBoardActivity : AppCompatActivity(), OnMapReadyCallback {

    private var arePermissionsGranted = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        if (allGranted) {
            arePermissionsGranted = true
            onAllPermissionsGranted()
        } else {
            arePermissionsGranted = false
            // Check if any permission was permanently denied ("Don't ask again")
            val permanentlyDenied = result.keys.any { perm ->
                !ActivityCompat.shouldShowRequestPermissionRationale(this, perm)
            }
            showPermissionDeniedDialog(permanentlyDenied)
        }
    }


    private fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        } else {
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }
        return permissions.toTypedArray()
    }

    private fun hasAllRequiredPermissions(): Boolean =
        getRequiredPermissions().all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    private lateinit var binding: ActivitySmartTrackDashboardBinding
    private lateinit var trackingSmartTrackViewModel: TrackingSmartTrackViewModel
    private var currentTrackingPoints: List<TrackingPlotData> = emptyList()
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationFetchDialog: Dialog? = null
    private lateinit var sharedPreference: DefaultPersistenceDataSource
    private var isPunchedIn = false
    private var locationTimer: CountDownTimer? = null
    private var locationTimeoutJob: kotlinx.coroutines.Job? = null   // ADD THIS
    private var isUserDataReady = false

    // Pref keys
    companion object {
        const val TAG = "Smart Track Tracking Activity"
        private const val PREFS_NAME = "SmartTrackTrackingActivityPref"
        private const val KEY_START_DATE = "startDate"
        private const val KEY_END_DATE = "endDate"
        private const val DURATION = "duration"
    }

    private val suppressDurationListener = AtomicBoolean(true)
    private var bottomSheetDurationSpinner: Spinner? = null
    private var durationList: List<TimeFilterData> = emptyList()
    private var selectedDuration = "MTD"
    private var start: String = ""
    private var end: String = ""
    private var empId: String = ""
    private lateinit var stSummaryViewModel: SmartTrackSummaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartTrackDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDependencies()
        initUI()
        resetFilterValues()
        setOnClickListeners()
        setupMap()
        observeViewModel()
        observeAutoPunchOut()
        checkPermissionsOnStart()
    }


    private fun checkPermissionsOnStart() {
        if (hasAllRequiredPermissions()) {
            arePermissionsGranted = true
            onAllPermissionsGranted()
        } else {
            arePermissionsGranted = false
            // Disable punch button until permissions are granted
            binding.punchInButtonImage.isEnabled = false
            permissionLauncher.launch(getRequiredPermissions())
        }
    }

    private fun onAllPermissionsGranted() {
        binding.punchInButtonImage.isEnabled = true
        // any setup that needs permissions immediately (e.g. move camera to current location)
    }

    private fun showPermissionDeniedDialog(permanentlyDenied: Boolean) {
        val message = if (permanentlyDenied) {
            "Location, Activity and Phone permissions are required for Punch In/Out and tracking. " +
                    "Please enable them from Settings."
        } else {
            "This app needs Location, Activity Recognition and Phone permissions to track your work. Please allow them to continue."
        }

        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(if (permanentlyDenied) "Open Settings" else "Allow") { _, _ ->
                if (permanentlyDenied) {
                    openAppSettings()
                } else {
                    permissionLauncher.launch(getRequiredPermissions())
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                // keep punch button disabled
                binding.punchInButtonImage.isEnabled = false
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun initDependencies() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreference = DefaultPersistenceDataSource.newInstance(application)!!

    }

    private fun initUI() {

        stSummaryViewModel = ViewModelProvider(this)[SmartTrackSummaryViewModel::class.java]
        trackingSmartTrackViewModel = ViewModelProvider(this)[TrackingSmartTrackViewModel::class.java]
        val pref = PrefManager(this)
        empId = pref.getEmployeeId() ?: ""

        getTrackingData(start,end,selectedDuration)
        binding.tbSmartTrackDashboard.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.punchInButtonImage.setOnClickListener { onPunchClick() }
        binding.cardDistance.setOnClickListener {
            val locationData = EmployeeDetailsData(
                employeeId = empId,
                latitude = 0.0,
                longitude = 0.0
            )
            val intent = Intent(this, EmployeeTimeLineActivity::class.java)
            intent.putExtra(IntentConstants.LOCATION_DATA, locationData)
            startActivity(intent)
        }
        binding.mcvPending.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            intent.putExtra(IntentConstants.TASK_STATUS, IntentConstants.TASK_PENDING)
            intent.putExtra(IntentConstants.DATE, selectedDuration)
            intent.putExtra(IntentConstants.EXTRA_START_DATE, start)
            intent.putExtra(IntentConstants.EXTRA_END_DATE, end)
            startActivity(intent)
        }
        binding.mcvCompleted.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            intent.putExtra(IntentConstants.TASK_STATUS, IntentConstants.TASK_COMPLETE)
            intent.putExtra(IntentConstants.DATE, selectedDuration)
            intent.putExtra(IntentConstants.EXTRA_START_DATE, start)
            intent.putExtra(IntentConstants.EXTRA_END_DATE, end)
            startActivity(intent)
        }
        binding.mcvFutureTask.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            intent.putExtra(IntentConstants.TASK_STATUS, IntentConstants.TASK_FUTURE)
            intent.putExtra(IntentConstants.DATE, selectedDuration)
            intent.putExtra(IntentConstants.EXTRA_START_DATE, start)
            intent.putExtra(IntentConstants.EXTRA_END_DATE, end)
            startActivity(intent)
        }
        binding.fabCreateTask.setOnClickListener {
            startActivity(Intent(this, DynamicFormCreateTaskSmartTrackActivity::class.java))
        }

    }


    private fun setupMap() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private var hasShownNoDataToast = false

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isMyLocationButtonEnabled = false
            isCompassEnabled = false
            isMapToolbarEnabled = false
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true   // blue dot only (no marker)

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }

        setMapClickListener()
    }
    private fun setMapClickListener() {
        googleMap.setOnMapClickListener {
            if (currentTrackingPoints.isEmpty()) {
                if (!hasShownNoDataToast) {
                    hasShownNoDataToast = true
                    showDialog("Alert", "No tracking data available")
                }
                return@setOnMapClickListener
            }

            val trackingPoints = currentTrackingPoints
                .sortedBy { convertToMillis(it.insertDateTime) }
                .map {
                    FullScreenMapDialog.TrackingPoint(
                        it.latitude ?: 0.0,
                        it.longitude ?: 0.0,
                        it.insertDateTime ?: ""
                    )
                }

            val dialog = FullScreenMapDialog.newInstance(ArrayList(trackingPoints))
            dialog.show(supportFragmentManager, "FullScreenMapDialog")
        }
    }
    private fun observeViewModel() {

        trackingSmartTrackViewModel.loading.observe(this) {
            binding.loader.root.visibility = if (it) View.VISIBLE else View.GONE
        }

        trackingSmartTrackViewModel.trackingSmartTrackResponse.observe(this) { response ->
            binding.loader.root.visibility = View.GONE
            response?.let { res ->
                binding.tvUserName.text = response.employeeName ?: "Unknown User"
                binding.tvTaskTotalCount.text = "Total Task : ${response.totalTasks}"
                binding.tvPendingCount.text = response.pendingTasks.toString()
                binding.tvCompletedCount.text = response.completedTasks.toString()
                binding.tvFutureCount.text = response.futureCount.toString()
                binding.tvDistance.text = "${response.totaldistance ?: "0.00"} km"
                loadPunchState(response)
                isUserDataReady = true


                currentTrackingPoints = res.data ?: emptyList()

                lifecycleScope.launch {
                    plotRoutePath(ArrayList(currentTrackingPoints))
                }
            }
        }

        trackingSmartTrackViewModel.errorMessage.observe(this) { message ->
            showDialog("Alert", message)
        }
        stSummaryViewModel.errorMessageLiveData.observe(this) { message ->
            showDialog("Alert", message)
        }
        stSummaryViewModel.dateFilterResponse.observe(this) { response ->
            durationList = response.data ?: emptyList()

            bottomSheetDurationSpinner?.let {
                setDurationAdapter(it)
            }

            if (isFilterDialogRequested) {

                isFilterDialogRequested = false

                if (durationList.isNotEmpty()) {

                    showFilterBottomSheet()

                } else {

                    showDialog(
                        "Alert",
                        "Duration data not found."
                    )
                }
            }
        }

        trackingSmartTrackViewModel.trackingPunchResponse.observe(this) { response ->

            if (response.status == true) {

                // Punch API successful
                refreshTrackingData()

            } else {

                binding.loader.root.visibility = View.GONE

                showDialog(
                    "Alert",
                    response.message ?: "Punch Failed"
                )
            }
        }    }


    fun convertToMillis(dateStr: String?): Long {
        if (dateStr.isNullOrEmpty()) return 0L
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            val date = sdf.parse(dateStr)
            date?.time ?: 0L

        } catch (e: Exception) {
            0L
        }
    }


    private fun plotRoutePath(trackingPoints: ArrayList<TrackingPlotData>) {

        googleMap.clear()

        if (trackingPoints.isEmpty()) {
            moveCameraToCurrentLocation()
            return
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

        val sortedPoints = trackingPoints.sortedBy { point ->
            point.insertDateTime?.let {
                try {
                    formatter.parse(it)?.time ?: Long.MAX_VALUE
                } catch (e: Exception) {
                    Long.MAX_VALUE
                }
            } ?: Long.MAX_VALUE
        }

        val filteredPoints = mutableListOf<TrackingPlotData>()
        var lastLocation: Location? = null

        for (point in sortedPoints) {
            val current = Location("").apply {
                latitude = point.latitude ?: 0.0
                longitude = point.longitude ?: 0.0
            }

            if (lastLocation != null) {
                val distance = current.distanceTo(lastLocation)
                if (distance < 20) continue
            }

            filteredPoints.add(point)
            lastLocation = current
        }

        if (filteredPoints.isEmpty()) {
            moveCameraToCurrentLocation()
            return
        }

        // Draw polyline
        val latLngList = filteredPoints.map {
            LatLng(it.latitude ?: 0.0, it.longitude ?: 0.0)
        }

        googleMap.addPolyline(
            PolylineOptions()
                .addAll(latLngList)
                .width(10f)
                .color(Color.BLUE)
                .geodesic(true)
        )

        val startPoint = filteredPoints.first()
        val endPoint = filteredPoints.last()

        // Get address in background and then add markers
        lifecycleScope.launch {
            val startAddress = getAddressFromLatLng(
                startPoint.latitude ?: 0.0,
                startPoint.longitude ?: 0.0
            )
            val endAddress = getAddressFromLatLng(
                endPoint.latitude ?: 0.0,
                endPoint.longitude ?: 0.0
            )

            // Add Start Marker
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(startPoint.latitude ?: 0.0, startPoint.longitude ?: 0.0))
                    .title("Start")
                    .snippet("${formatTrackingTime(startPoint.insertDateTime)}\n${startPoint.address}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )

            // Add End Marker
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(endPoint.latitude ?: 0.0, endPoint.longitude ?: 0.0))
                    .title("End")
                    .snippet("${formatTrackingTime(endPoint.insertDateTime)}\n${endPoint.address}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }

        // Camera
        if (latLngList.size == 1) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngList[0], 16f))
        } else {
            val boundsBuilder = LatLngBounds.Builder()
            latLngList.forEach { boundsBuilder.include(it) }
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 70))
        }
    }

    private suspend fun getAddressFromLatLng(lat: Double, lng: Double): String =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@SmartTrackDashBoardActivity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    // Prefer full address line
                    address.getAddressLine(0)
                        ?: buildString {
                            address.subLocality?.let { append("$it, ") }
                            address.locality?.let { append("$it, ") }
                            address.adminArea?.let { append(it) }
                        }.ifBlank { "Address not found" }
                } else {
                    "Address not found"
                }
            } catch (e: Exception) {
                Log.e("TAG", "Geocoder failed", e)
                "Address not available"
            }
        }

    private fun formatTrackingTime(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "--:--"

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            date?.let { outputFormat.format(it) } ?: "--:--"
        } catch (e: Exception) {
            "--:--"
        }
    }
    @SuppressLint("MissingPermission")
    private fun moveCameraToCurrentLocation() {

        fusedLocationClient.lastLocation
            .addOnSuccessListener {

                it?.let { location ->

                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ),
                            17f
                        )
                    )
                }
            }
    }


    private fun onPunchClick() {
        val type = if (!isPunchedIn) "in" else "out"
        showPunchConfirmationDialog(type)
    }

    private fun showPunchConfirmationDialog(type: String) {
        val dialog = Dialog(this)
        dialog.setContentView(if (type == "in") R.layout.dialog_punch_in_confirmation else R.layout.dialog_punch_out_confirmation)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val btnYes: Button = dialog.findViewById(R.id.btnYes)
        val btnNo: Button = dialog.findViewById(R.id.btnNo)

        btnYes.setOnClickListener {
            dialog.dismiss()
            checkLocationPermissionAndPunch(type)
        }
        btnNo.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }


    private fun showLocationFetchingDialog(type: String) {
        // Cancel previous timer if exists
        locationTimer?.cancel()

        // Safely dismiss any previous dialog

        locationFetchDialog?.dismiss()

        // Create new dialog
        locationFetchDialog = Dialog(this)
        locationFetchDialog?.setContentView(R.layout.dialog_location_fetching)
        locationFetchDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        locationFetchDialog?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val tvTimer = locationFetchDialog?.findViewById<TextView>(R.id.tvTimer)
        val tvStatus = locationFetchDialog?.findViewById<TextView>(R.id.tvStatus)

        locationTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000).toInt()
                tvTimer?.text = "$sec"
                if (sec <= 25) {
                    tvStatus?.text = "Processing! Kindly wait."
                }
            }

            override fun onFinish() {
                tvStatus?.text = "Finalizing Punch ${if (type == "in") "In" else "Out"}..."
            }
        }

        locationTimer?.start()

        // Show dialog
        locationFetchDialog?.show()
    }


    private fun startLocationFetching(type: String) {

        // Explicit permission check right at the call site (satisfies lint + protects against
        // permission being revoked between the tap and this call actually running)
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            hideLoadingAndDialog()
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_LONG).show()
            arePermissionsGranted = false
            checkPermissionsOnStart() // re-request so the user can fix it
            return
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            8000
        ).setWaitForAccurateLocation(false)
            .build()

        val locationCallback = object : LocationCallback() {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                fusedLocationClient.removeLocationUpdates(this)

                if (location == null) {
                    hideLoadingAndDialog()
                    Toast.makeText(this@SmartTrackDashBoardActivity, "Could not get current location", Toast.LENGTH_LONG).show()
                    return
                }

                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f)
                )

                lifecycleScope.launch {
                    val addressText = getAddressAsync(location.latitude, location.longitude)

                    locationFetchDialog?.dismiss()
                    binding.loader.root.visibility = View.VISIBLE

                    val smartRequest = TrackingINandOUTRequest(
                        vkid = sharedPreference.employeeId ?: "",
                        punchInLatitude = if (type == "in") location.latitude.toString() else null,
                        punchInLongitude = if (type == "in") location.longitude.toString() else null,
                        punchOutLatitude = if (type == "out") location.latitude.toString() else null,
                        punchOutLongitude = if (type == "out") location.longitude.toString() else null,
                        punchInAddress = if (type == "in") addressText else null,
                        punchOutAddress = if (type == "out") addressText else null,
                        deviceId = sharedPreference.deviceId ?: "",
                        deviceName = sharedPreference.deviceName ?: "Android Device",
                        deviceType = "Mobile",
                        activity = type
                    )

                    trackingSmartTrackViewModel.callPunchINandOUTSmartTrack(smartRequest)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    fusedLocationClient.removeLocationUpdates(this)
                    hideLoadingAndDialog()
                    Toast.makeText(
                        this@SmartTrackDashBoardActivity,
                        "Location services not available right now",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }
    private suspend fun getAddressAsync(lat: Double, lng: Double): String =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@SmartTrackDashBoardActivity, Locale.getDefault())
                val list = geocoder.getFromLocation(lat, lng, 1)

                if (!list.isNullOrEmpty()) {
                    list[0].getAddressLine(0)?.let {
                        return@withContext it
                    }
                }
            } catch (e: Exception) {
                Log.e("Geocoder", "Address fetch failed", e)
            }

            return@withContext "Lat:$lat, Lng:$lng"
        }

    private fun hideLoadingAndDialog() {
        locationTimer?.cancel()
        locationFetchDialog?.let {
            if (it.isShowing) it.dismiss()
        }
        binding.loader.root.visibility = View.GONE
    }

    private fun refreshTrackingData() {
        getTrackingData("", "", selectedDuration)
    }


    private fun startSmartTrackService() {
        PunchStateHolder.isPunchedIn = true
        PunchStateHolder.vkid = empId

        val intent = Intent(this, SmartTrackService::class.java)
        intent.putExtra(SmartTrackService.EXTRA_VKID, empId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else
            startService(intent)
    }
    private fun stopSmartTrackService() {
       PunchStateHolder.isPunchedIn = false
        val intent = Intent(this, SmartTrackService::class.java)
        stopService(intent)
    }

    private fun loadPunchState(response: TrackingResponse) {

        val punchInTime = formatTo12Hour(response.punchInTime)
        val punchOutTime = formatTo12Hour(response.punchOutTime)

        binding.tvInTime.text = punchInTime
        binding.tvOutTime.text = punchOutTime
        binding.tvPunchStatusOut.text = response.punchStatusMsg.orEmpty()

        when (response.punchStatus) {

            "NOT_PUNCHED" -> {
                isPunchedIn = false
                binding.tvPunchButtonText.text = "Punch In"
                binding.lnInTime.visibility = View.GONE
                binding.lnOutTime.visibility = View.GONE

                stopSmartTrackService()
            }

            "IN" -> {
                isPunchedIn = true
                binding.tvPunchButtonText.text = "Punch Out"
                binding.lnInTime.visibility = View.VISIBLE
                binding.lnOutTime.visibility = View.GONE

                startSmartTrackService()
            }

            "OUT" -> {
                isPunchedIn = false
                binding.tvPunchButtonText.text = "Punch In"
                binding.lnInTime.visibility = View.VISIBLE
                binding.lnOutTime.visibility = View.VISIBLE

                stopSmartTrackService()
            }
        }
    }

    private fun observeAutoPunchOut() {
        lifecycleScope.launch {
            SmartTrackEvents.autoPunchOut.collect {
                refreshTrackingData()
//                showDialog("Auto Punch-Out", "You were automatically punched out at day end.")
            }
        }
    }
    fun formatTo12Hour(input: String?): String {

        if (input.isNullOrEmpty()) return ""

        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())

            val date = inputFormat.parse(input)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun formatMillisSafe(millis: Long): String {
        return if (millis <= 0L) "--" else formatMillis(millis)
    }


    @SuppressLint("SetTextI18n")
    private fun showFilterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)

        val sheetBinding = LayoutReportFilterBottomSheetBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(sheetBinding.root)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        val savedDuration = prefs.getString(DURATION, "MTD") ?: "MTD"
        val savedStartDate = prefs.getString(KEY_START_DATE, "") ?: ""
        val savedEndDate = prefs.getString(KEY_END_DATE, "") ?: ""

        selectedDuration = savedDuration

        bottomSheetDurationSpinner = sheetBinding.spinnerDuration

        if (durationList.isNotEmpty()) {
            setDurationAdapter(bottomSheetDurationSpinner!!)
        }

        val todayApi = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val todayUI = SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(Date())

        val startDateTxt = arrayOf(todayApi)
        val endDateTxt = arrayOf(todayApi)

        sheetBinding.startDateBTN.text = todayUI
        sheetBinding.endDateBTN.text = todayUI

        // ✅ restore saved dates
        if (savedStartDate.isNotEmpty()) {
            startDateTxt[0] = savedStartDate
            sheetBinding.startDateBTN.text =
                convertDateFormat(savedStartDate, "dd-MM-yyyy", "dd MMM yy")
        }

        if (savedEndDate.isNotEmpty()) {
            endDateTxt[0] = savedEndDate
            sheetBinding.endDateBTN.text =
                convertDateFormat(savedEndDate, "dd-MM-yyyy", "dd MMM yy")
        }

        val calendar = Calendar.getInstance()

        val startPicker = DatePickerDialog(
            this,
            R.style.MyDatePickerTheme,
            { _, y, m, d ->
                val cal = Calendar.getInstance()
                cal.set(y, m, d)

                startDateTxt[0] = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cal.time)
                sheetBinding.startDateBTN.text = SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(cal.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val endPicker = DatePickerDialog(
            this,
            R.style.MyDatePickerTheme,
            { _, y, m, d ->
                val cal = Calendar.getInstance()
                cal.set(y, m, d)

                endDateTxt[0] = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cal.time)
                sheetBinding.endDateBTN.text = SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(cal.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        sheetBinding.startDateBTN.setOnClickListener {
            startPicker.datePicker.maxDate = System.currentTimeMillis()
            startPicker.show()
        }

        sheetBinding.endDateBTN.setOnClickListener {
            endPicker.datePicker.maxDate = System.currentTimeMillis()
            endPicker.show()
        }

        sheetBinding.spinnerDuration.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (suppressDurationListener.get()) return

                    val selected = durationList.getOrNull(position)

                    selectedDuration = selected?.filterDate ?: "MTD"

                    Log.d(TaskActivity.TAG, "Duration : $selectedDuration")


                    val apiStart = startDateTxt[0]
                    val apiEnd = endDateTxt[0]

                    if (selectedDuration.equals("Custom Date", true)) {
                        start = apiStart
                        end = apiEnd
                    } else {
                        start = ""
                        end = ""
                    }

                    // Show custom date layout only for CUSTOM
                    if (selectedDuration.equals("Custom Date", true)) {
                        sheetBinding.linCustomDate.visibility = View.VISIBLE
                    } else {
                        sheetBinding.linCustomDate.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        sheetBinding.btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        sheetBinding.btnApply.setOnClickListener {
            val apiStart = startDateTxt[0]
            val apiEnd = endDateTxt[0]

            if (selectedDuration.equals("Custom Date", true)) {
                start = apiStart
                end = apiEnd
            } else {
                start = ""
                end = ""
            }
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            if (sdf.parse(apiStart)!!.after(sdf.parse(apiEnd)!!)) {
                showDialog("Validation Error", "Start date cannot be after end date")
                return@setOnClickListener
            }

            prefs.edit().apply {
                putString(KEY_START_DATE, apiStart)
                putString(KEY_END_DATE, apiEnd)
                putString(DURATION, selectedDuration)
                apply()
            }

            if (NetworkUtil.isNetworkAvailable(this)) {

                getTrackingData(
                    startDate = if (selectedDuration.equals("Custom Date", true)) apiStart else "",
                    endDate = if (selectedDuration.equals("Custom Date", true)) apiEnd else "",
                   duration = selectedDuration
                )


            } else {
                IOUtils.showMessage("Please check your network connection.!!", this)
            }


            bottomSheetDialog.dismiss()
        }

        sheetBinding.btnReset.setOnClickListener {
            resetFilterValues()

            selectedDuration = "MTD"


            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            sheetBinding.startDateBTN.text = SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(Date())
            sheetBinding.endDateBTN.text = SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(Date())
            getTrackingData("","",selectedDuration)
            IOUtils.showMessage("Filter reset", this)

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun setDurationAdapter(spinner: Spinner) {
        suppressDurationListener.set(true)

        val accountTypeNames = durationList.map { it.filterDate }

        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.custom_filter_spinner_layout,
            accountTypeNames
        ) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view =
                    layoutInflater.inflate(R.layout.custom_spinner_dropdown_item, parent, false)
                view.findViewById<TextView>(android.R.id.text1).text = getItem(position)
                view.findViewById<View>(R.id.divider_line).visibility =
                    if (position == count - 1) View.GONE else View.VISIBLE
                return view
            }
        }

        spinner.adapter = adapter

        val index = durationList.indexOfFirst {
            it.filterDate.equals(selectedDuration, true)
        }

        if (index >= 0) {
            spinner.setSelection(index)
        }

        // IMPORTANT
        spinner.post {
            suppressDurationListener.set(false)
        }
    }

    private fun convertDateFormat(date: String?, fromFormat: String, toFormat: String): String {
        if (date.isNullOrEmpty()) return SimpleDateFormat(
            toFormat,
            Locale.getDefault()
        ).format(Date())
        return try {
            val from = SimpleDateFormat(fromFormat, Locale.getDefault())
            val to = SimpleDateFormat(toFormat, Locale.getDefault())
            val parsed =
                from.parse(date) ?: return SimpleDateFormat(toFormat, Locale.getDefault()).format(
                    Date()
                )
            to.format(parsed)
        } catch (e: Exception) {
            SimpleDateFormat(toFormat, Locale.getDefault()).format(Date())
        }
    }

    private fun callFilterAPI() {

        if (!NetworkUtil.isNetworkAvailable(this)) {
            showDialog("Network Issue!", "Please check your network connection.")
            return
        }

        stSummaryViewModel.getFilterDateList()
    }

    private fun getfilter(
        selectedLabel: String,
        startDateTxt: Array<String>,
        endDateTxt: Array<String>,
        startDate: Array<String>,
        endDate: Array<String>
    ): String? {
        return when (selectedLabel) {
            "Month till date" -> "Month till date"
            "Quarter till date" -> "Quarter till date"
            "Year till date" -> "Year till date"
            "Last 3 months" -> "Last 3 Months"
            "Custom Date" -> {
                startDate[0] = startDateTxt[0]
                endDate[0] = endDateTxt[0]
                "0"
            }

            else -> selectedLabel
        }
    }

    private fun resetFilterValues() {
        val editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
        editor.remove(DURATION)
        editor.remove(KEY_START_DATE)
        editor.remove(KEY_END_DATE)
        editor.apply()
    }

    private fun convertToApiDate(input: String): String {
        if (input.isBlank()) return ""
        val ddMMyyyyRegex = Regex("""\d{2}-\d{2}-\d{4}""")
        if (ddMMyyyyRegex.matches(input)) return input
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            input
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun convertIsoToMillis(raw: String): Long {
        return try {
            var value = raw
            if (value.contains(".")) {
                val parts = value.split(".")
                if (parts.size == 2) {
                    val timePart = parts[0]
                    var ms = parts[1]

                    // trim timezone if any (rare)
                    ms = ms.takeWhile { it.isDigit() }

                    // pad to 3 digits
                    ms = ms.padEnd(3, '0')

                    value = "$timePart.$ms"
                }
            }

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            val dt = LocalDateTime.parse(value, formatter)
            dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }


    fun formatMillis(millis: Long): String {
        if (millis == 0L) return "--"
        val sdf = SimpleDateFormat("HH:mm:ss a", Locale.getDefault())
        return sdf.format(Date(millis))
    }


    private fun checkLocationPermissionAndPunch(type: String) {
        if (!arePermissionsGranted) {
            checkPermissionsOnStart()  // re-trigger flow instead of duplicating logic
            return
        }

        showLocationFetchingDialog(type)
        startLocationFetching(type)
    }

    override fun onResume() {
        super.onResume()
        if (!arePermissionsGranted && hasAllRequiredPermissions()) {
            arePermissionsGranted = true
            onAllPermissionsGranted()
        }
        if (isUserDataReady) {
            refreshTrackingData()      // ⬅️ ADD
        }
    }

    private fun showDialog(title: String, message: String) {
        showCustomDialog(this, title, message, "OK") { }
    }

    private var isFilterDialogRequested = false

    private fun setOnClickListeners() {

        binding.filterSection.setOnClickListener {

            if (durationList.isNotEmpty()) {

                showFilterBottomSheet()

            } else {

                isFilterDialogRequested = true
                callFilterAPI()
            }
        }
    }

    private fun getTrackingData(startDate: String, endDate: String, duration: String) {

        if (!NetworkUtil.isNetworkAvailable(this)) {
            showDialog("Network", "Please check your internet connection.")
            return
        }


        val request = TrackingRequest(
            vkid = empId,
            startDate = startDate,
            endDate = endDate,
            filter = duration
        )

        trackingSmartTrackViewModel.getSmartTrackTrackingData(request)
    }
}






