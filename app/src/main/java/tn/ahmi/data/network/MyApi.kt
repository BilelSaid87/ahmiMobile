package tn.ahmi.data.network

import androidx.lifecycle.LiveData
import tn.ahmi.data.network.responses.AuthResponse
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import tn.ahmi.data.db.entities.User
import tn.ahmi.data.network.responses.VerifResponse
import tn.ahmi.data.preferences.PreferenceProvider

interface MyApi {

    @POST("users/signin_mobile")
    suspend fun userLogin(
        @Body phone: HashMap<String,String>
    ) : Response<AuthResponse>

    @POST("users/verification_code")
    suspend fun verificationCode(
        @Body phone: HashMap<String,String>
    ) : Response<VerifResponse>

    @POST("users/signup")
    suspend fun userSignup(
        @Body data: HashMap<String,String>
    ) : Response<AuthResponse>


    @POST("users/update_mac_address")
    suspend fun updateMacAdress(
        @Body data: HashMap<String,String>
    ) : Response<ResponseBody>

    @POST("users/update_profile_info")
    suspend fun updateProfil(
        @Header("x-auth-token") token: String,
        @Body data: HashMap<String,Any>
    ) : Response<ResponseBody>

    @POST("interactions/store_data")
    suspend fun updateData(
        @Header("x-auth-token") token: String?,
        @Body data: Array<User>
    ) : Response<ResponseBody>


    companion object{

        const val BASE_URL = "http://5.196.67.11:5000/api/"

        operator fun invoke(
            networkConnectionInterceptor: NetworkConnectionInterceptor
        ) : MyApi {

            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)


            val okkHttpclient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(networkConnectionInterceptor)
                .build()

            return Retrofit.Builder()
                .client(okkHttpclient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(MyApi::class.java)
        }
    }

}

