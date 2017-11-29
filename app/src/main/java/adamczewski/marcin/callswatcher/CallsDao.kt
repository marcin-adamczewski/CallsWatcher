package adamczewski.marcin.callswatcher

import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.moczul.ok2curl.logger.Loggable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST


class CallsDao {

    private val apiService: ApiService

    init {
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        val httpLoggingInterceptor = HttpLoggingInterceptor(AndroidLogger("lol")).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        apiService = Retrofit.Builder()
                .baseUrl("http://192.168.0.10/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build())
                .build()
                .create(ApiService::class.java)
    }

    fun sendPhoneNumber(incomingCallRequest: IncomingCallRequest, callback: Callback<Void>) {
        apiService.sendIncomingCall(incomingCallRequest).enqueue(callback)
    }

    class AndroidLogger (tag: String) : HttpLoggingInterceptor.Logger, Loggable {
        private val tag: String = checkNotNull(tag)
        override fun log(message: String) {
            Log.v(tag, message)
        }
    }
}

data class IncomingCallRequest(val calledId: String?)

interface ApiService {

    @POST("incoming_call")
    @retrofit2.http.Headers("Accept: application/json")
    fun sendIncomingCall(@Body incomingCallRequest: IncomingCallRequest): Call<Void>
}