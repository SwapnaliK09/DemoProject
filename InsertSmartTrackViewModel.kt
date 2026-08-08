package `in`.vakrangee.hrms.ui.stManager.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import `in`.vakrangee.hrms.ui.stManager.data.InsertLocationRequest
import `in`.vakrangee.hrms.ui.stManager.data.InsertLocationResponse
import `in`.vakrangee.hrms.ui.travelReport.api.BaseViewModel
import `in`.vakrangee.hrms.ui.travelReport.api.RemoteRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class InsertSmartTrackViewModel : BaseViewModel() {

    companion object {
        private const val TAG = "InsertSmartTrackViewModel"
    }

    val errorMessage = MutableLiveData<String>()

    private val _insertLocationData = MutableLiveData<InsertLocationResponse>()
    val insertLocationResponse: LiveData<InsertLocationResponse> get() = _insertLocationData

    private val callbackInsertLocation = object : Callback<InsertLocationResponse> {

        override fun onResponse(
            call: Call<InsertLocationResponse>,
            response: Response<InsertLocationResponse>
        ) {
            _loading.value = false

            if (!response.isSuccessful) {
                errorMessage.postValue("Server Error: ${response.code()}")
                return
            }

            val body = response.body()

            if (body == null) {
                errorMessage.postValue("Response body is null")
                return
            }

            _insertLocationData.postValue(response.body())
        }

        override fun onFailure(call: Call<InsertLocationResponse>, t: Throwable) {
            _loading.value = false
            handleFailure(t, errorMessage, TAG)
            Log.e(TAG, t.message ?: "Unknown error", t)
        }
    }

    /**
     * LiveData-based call — use this from Activities/Fragments that
     * have a LifecycleOwner to observe insertLocationResponse / errorMessage.
     */
    fun insertUserLocation(request: InsertLocationRequest) {
        _loading.value = true
        RemoteRepository.insertLocationData(request)
            .enqueue(callbackInsertLocation)
    }

    /**
     * Suspend version — use this from the background Service (SmartTrackService),
     * which has no LifecycleOwner to observe LiveData with. Returns the real
     * server result so the caller can decide whether to delete the local row.
     *
     * Deliberately does NOT touch _loading / _insertLocationData / errorMessage —
     * those stay UI-only so this call can run silently from a background service
     * without triggering loaders or toasts on whatever screen happens to be open.
     */
    suspend fun insertUserLocationSync(request: InsertLocationRequest): InsertLocationResponse? =
        suspendCancellableCoroutine { cont ->
            val call = RemoteRepository.insertLocationData(request)

            cont.invokeOnCancellation { call.cancel() }

            call.enqueue(object : Callback<InsertLocationResponse> {
                override fun onResponse(
                    call: Call<InsertLocationResponse>,
                    response: Response<InsertLocationResponse>
                ) {
                    if (!cont.isActive) return
                    if (response.isSuccessful) {
                        cont.resume(response.body())
                    } else {
                        Log.e(TAG, "Server error ${response.code()} on sync insert")
                        cont.resume(null)
                    }
                }

                override fun onFailure(call: Call<InsertLocationResponse>, t: Throwable) {
                    if (!cont.isActive) return
                    Log.e(TAG, "Sync insert failed: ${t.message}", t)
                    cont.resumeWithException(t)
                }
            })
        }
}