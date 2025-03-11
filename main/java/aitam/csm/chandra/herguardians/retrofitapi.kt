package aitam.csm.chandra.herguardians

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Body

interface EmotionApiService {
    @Multipart
    @POST("/predict")
    fun uploadAudio(@Part file: MultipartBody.Part): Call<EmotionResponse>

    @POST("/send_emergency")
    fun sendEmergencyMessage(@Body message: RequestBody): Call<Void>
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.19.52:5000/"  // Ensure Flask server is running here

    val instance: EmotionApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmotionApiService::class.java)
    }
}
