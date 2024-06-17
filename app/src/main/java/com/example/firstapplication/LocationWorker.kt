package com.example.firstapplication

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.firstapplication.AppModule_GetTimeoutFactory.getTimeout
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltWorker
class LocationWorker(
      context: Context,
   workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private lateinit var repository: Repository


    override suspend fun doWork(): Result {
        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)
        val address = inputData.getString("Address")

        val driverid = inputData.getInt("driverid",0)
        val schoolid = inputData.getInt("schoolid", 0)
        val drivertoken = inputData.getString("drivertoken")


        Log.e("Latitude",latitude.toString())
        Log.e("Longitude",longitude.toString())

        Log.d("LocationWorker", "Received Latitude: $latitude, Longitude: $longitude")

        val httpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofitClient = Retrofit.Builder()
            .baseUrl(ApiServices.BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiServices= retrofitClient.create(ApiServices::class.java)

        repository = RepositoryImpl(apiServices)

        return withContext(Dispatchers.IO) {

            val response = repository.
            updateLocation(AppConst.API_TOKEN+drivertoken,LocationData(driverid,latitude, longitude,address,schoolid))
            if (response.isSuccessful) {
                Log.d("LocationApi", "Location update successful")
                Result.success()

            } else {
                Log.d("LocationApi", "Location update failed with code: ${response.code()}")
                Result.retry()
            }

        }
    }
}
