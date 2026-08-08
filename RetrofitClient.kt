package `in`.vakrangee.hrms.ui.travelReport.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val UAT_URL = "https://vkmssit.vakrangee.in/vakrangee-connect/"
    private const val SIT_URL = "https://vkmssit.vakrangee.in/"
    private const val BASE_URL = "https://vkms.vakrangee.in/vakrangee-connect/"
    private const val PROD_URL = "https://vkms.vakrangee.in/"
//    private const val TOMCAT_PROD = "https://api.vakrangee.in/SmartTrack/api/"
    private const val TOMCAT_PROD = "https://api.vakrangee.in/SmartTrack/"
    private const val TOMCAT_UAT = "https://apisit.vakrangee.in/SmartTrack/"
    private const val VKMS_SIT = "https://vkmssit.vakrangee.in/SmartTrack/"
    private const val TOMCAT_UAT_URL = "https://apisit.vakrangee.in/SmartTrack/api/"
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
//        level = HttpLoggingInterceptor.Level.NONE

    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)   // server connect
            .readTimeout(15, TimeUnit.SECONDS)      // response read
            .writeTimeout(15, TimeUnit.SECONDS)     // request body
            .callTimeout(20, TimeUnit.SECONDS)      // full call
            .addInterceptor(loggingInterceptor)
            .retryOnConnectionFailure(true)
            .build()
    }

    val apiUAT: APIEndPoints by lazy {
        createRetrofit(UAT_URL)
    }

    val apiPROD: APIEndPoints by lazy {
        createRetrofit(BASE_URL)
    }
    val apiSIT: APIEndPoints by lazy {
        createRetrofit(SIT_URL)
    }

    val api_PROD: APIEndPoints by lazy {
        createRetrofit(PROD_URL)
    }

    val tomcat_PROD: APIEndPoints by lazy {
        createRetrofit(TOMCAT_PROD)
    }

    val tomcat_UAT: APIEndPoints by lazy {
        createRetrofit(TOMCAT_UAT)
    }
    val tomcat_UAT_URL: APIEndPoints by lazy {
        createRetrofit(TOMCAT_UAT_URL)
    }

    val vkmsSIT: APIEndPoints by lazy {
        createRetrofit(VKMS_SIT)
    }

    private fun createRetrofit(baseUrl: String): APIEndPoints {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIEndPoints::class.java)
    }
}
