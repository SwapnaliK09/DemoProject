package `in`.vakrangee.hrms.ui.travelReport.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import `in`.vakrangee.hrms.ui.salesTracker.util.NetworkErrorUtils

abstract class BaseViewModel : ViewModel() {

    protected val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    protected fun showLoader() {
        _loading.postValue(true)
    }

    protected fun hideLoader() {
        _loading.postValue(false)
    }

    protected fun handleFailure(
        t: Throwable,
        errorMessage: MutableLiveData<String>,
        tag: String
    ) {
        hideLoader()
        errorMessage.postValue(NetworkErrorUtils.getErrorMessage(t))
        Log.e(tag, "API Failure", t)
    }
}