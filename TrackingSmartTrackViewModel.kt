package `in`.vakrangee.hrms.ui.stManager.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import `in`.vakrangee.hrms.ui.stManager.data.TrackingRequest
import `in`.vakrangee.hrms.ui.stManager.data.TrackingResponse
import `in`.vakrangee.hrms.ui.travelReport.api.BaseViewModel
import `in`.vakrangee.hrms.ui.travelReport.api.RemoteRepository
import `in`.vakrangee.smartTrack.request.PunchINOUTData
import `in`.vakrangee.smartTrack.request.TrackingINandOUTRequest
import `in`.vakrangee.smartTrack.request.TrackingPunchINandOUTResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrackingSmartTrackViewModel : BaseViewModel() {

    private companion object {
        const val TAG = "TrackingSmartTrackViewModel"
    }

    val errorMessage: MutableLiveData<String> = MutableLiveData()

    private val _trackingSmartTrackData = MutableLiveData<TrackingResponse>()
    val trackingSmartTrackResponse: LiveData<TrackingResponse> get() = _trackingSmartTrackData

    private val _punchStatus = MutableLiveData<String>()
    val punchStatus: LiveData<String> = _punchStatus
    private val callbackTracking = object : Callback<TrackingResponse> {

        override fun onResponse(
            call: Call<TrackingResponse>,
            response: Response<TrackingResponse>
        ) {

            _loading.postValue(false)

            if (!response.isSuccessful) {
                errorMessage.postValue("Server Error : ${response.code()}")
                return
            }

            val body = response.body() ?: run {
                errorMessage.postValue("Empty response from server.")
                return
            }

            // Save latest punch status
            _punchStatus.postValue(body.punchStatus?:"")

            // Always pass the response so UI can display employee/task/punch details
            _trackingSmartTrackData.postValue(body)

            // Show error only when tracking points are not available
            if (body.data.isNullOrEmpty()) {
                errorMessage.postValue(body.punchStatusMsg ?: "No tracking data found.")
            }
        }

        override fun onFailure(call: Call<TrackingResponse>, t: Throwable) {

            _loading.postValue(false)
            handleFailure(t, errorMessage, TAG)
        }
    }
    fun getSmartTrackTrackingData(request: TrackingRequest) {
        _loading.postValue(true)
        RemoteRepository.fetchTrackingSmartTrackData(request).enqueue(callbackTracking)
    }


    private val _trackingPunchResponse = MutableLiveData<TrackingPunchINandOUTResponse>()

    val trackingPunchResponse: LiveData<TrackingPunchINandOUTResponse> get() = _trackingPunchResponse
    private val callbackTrackingPunchInOut =
        object : Callback<TrackingPunchINandOUTResponse> {

            override fun onResponse(
                call: Call<TrackingPunchINandOUTResponse>,
                response: Response<TrackingPunchINandOUTResponse>
            ) {

                _loading.postValue(false)

                if (!response.isSuccessful) {
                    errorMessage.postValue("Server Error : ${response.code()}")
                    return
                }

                val body = response.body()

                if (body == null) {
                    errorMessage.postValue("Empty response from server.")
                    return
                }

                _trackingPunchResponse.postValue(body!!)
            }

            override fun onFailure(
                call: Call<TrackingPunchINandOUTResponse>,
                t: Throwable
            ) {
                _loading.postValue(false)
                handleFailure(t, errorMessage, TAG)
            }
        }
    fun callPunchINandOUTSmartTrack(request: TrackingINandOUTRequest) {
        _loading.postValue(true)
        RemoteRepository.callPunchINandOUTSmartTrack(request).enqueue(callbackTrackingPunchInOut)
    }




}