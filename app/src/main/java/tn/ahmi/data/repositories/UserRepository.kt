package tn.ahmi.data.repositories

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.LiveData
import okhttp3.ResponseBody
import tn.ahmi.data.db.AppDatabase
import tn.ahmi.data.db.entities.User
import tn.ahmi.data.network.MyApi
import tn.ahmi.data.network.SafeApiRequest
import tn.ahmi.data.network.responses.AuthResponse
import tn.ahmi.data.network.responses.VerifResponse
import tn.ahmi.data.preferences.PreferenceProvider


class UserRepository(
    private val api: MyApi,
    private val db: AppDatabase
) : SafeApiRequest() {

    suspend fun userLogin(phone: String, mac_address: String): AuthResponse {
        return apiRequest {
            var map : HashMap<String,String> = HashMap()
            map["phone"] = phone
            map["mac_address"] = mac_address
            api.userLogin(map)
        }
    }

    suspend fun updateMac(phone: String, mac_address: String): ResponseBody {
        return apiRequest {
            var map : HashMap<String,String> = HashMap()
            map["phone"] = phone
            map["mac_address"] = mac_address
            api.updateMacAdress(map)
        }
    }

    suspend fun updateProfil(health_info: ArrayList<String>, date_of_birth: String,gender: String, token : String): ResponseBody {
        return apiRequest {
            var map : HashMap<String,Any> = HashMap()
            map["health_info"] = health_info
            map["date_of_birth"] = date_of_birth
            map["gender"] = gender
            api.updateProfil(token,map)
        }
    }

    suspend fun updateData(data : Array<User>,token : String?): ResponseBody {
        return apiRequest {
            api.updateData(token,data)
        }
    }

    suspend fun verifCode(phone: String): VerifResponse {
        return apiRequest {
            var map : HashMap<String,String> = HashMap()
            map["phone"] = phone
            api.verificationCode(map)
        }
    }

    suspend fun userSignup(
        phone: String,
        mac_address: String,
        bluetooth_id: String = "",
        password: String
    ) : AuthResponse {
        var map : HashMap<String,String> = HashMap()
        map["phone"] = phone
        map["mac_address"] = mac_address
        map["bluetooth_id"] = bluetooth_id
        map["password"] = password
        return apiRequest{ api.userSignup(map)}
    }

    suspend fun saveUserData(user: User) = db.getUserDao().upsert(user)

    fun getUser() = db.getUserDao().getuser()
    fun getUserData() = db.getUserDao().getuserData()
    fun getlastDataUser(lastId : Int) = db.getUserDao().getuserLastData(lastId)
    fun deletelastDataUser(lastId : Int) = db.getUserDao().deleteAll(lastId)


}