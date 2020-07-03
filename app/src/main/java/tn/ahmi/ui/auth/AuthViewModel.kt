package tn.ahmi.ui.auth

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModel
import tn.ahmi.ble.Utils
import tn.ahmi.data.db.entities.User
import tn.ahmi.data.preferences.PreferenceProvider
import tn.ahmi.data.repositories.UserRepository
import tn.ahmi.util.ApiException
import tn.ahmi.util.Coroutines
import tn.ahmi.util.NoInternetException
import tn.ahmi.util.getMacAddr


class AuthViewModel(
    private val repository: UserRepository
) : ViewModel() {

    var code: String? = null
    var inputcode: String? = null

    var mac_adress: String? = null
    var ble_id: String? = null
    var phone: String? = null
    var password: String? = null
    var type: String? = null

    //SignUpProfil
    var day: String? = null
    var year: String? = null
    var mounth: String? = null
    var gender: String? = null
    var healthStats = ArrayList<String>()

    // health state update info
    var isHeart : Boolean = false
    var isAsmathic : Boolean = false
    var isOther : Boolean = false
    var isNone : Boolean = false
    //end


    var authListener: AuthListener? = null
    var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    fun getLoggedInUser() = repository.getUser()
    fun getUserData() = repository.getUserData()


    fun saveUserdata(user : User) {
        Coroutines.io {
            try {
                repository.saveUserData(user)
            }catch(e: ApiException){
                authListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                authListener?.onFailure(e.message!!)
            }
        }
    }


    fun onLoginButtonClick(view: View){
       // var macAddress: String = Settings.Secure.getString(view.context.contentResolver, "bluetooth_address")

        password = getMacAddr()

        authListener?.onStarted()
        if(phone.isNullOrEmpty() || password.isNullOrEmpty()){
            authListener?.onFailure("Invalid phone")
            return
        }

        Coroutines.main {
            try {
                val authResponse = repository.userLogin(phone!!, password!!)
                authResponse.token?.let {
                    authListener?.onSuccess(it)
                   // repository.saveUser(it)
                    return@main
                }
                authListener?.onFailure(authResponse.result!!)
            }catch(e: ApiException){
                authListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                authListener?.onFailure(e.message!!)
            }
        }

    }

    fun onLogin(view: View){
        Intent(view.context, VerificationCodeActivity::class.java).also {
            view.context.startActivity(it)
        }
    }


    fun onSendData(view: View){
        Coroutines.io() {
            try {
                var lastUser = getLoggedInUser()
                var lastData = repository.getlastDataUser(lastUser!!.uid)
                val authResponse = repository.updateData(lastData,PreferenceProvider(view.context).getToken())
                authResponse?.let {
                    authListener?.onSuccess(it.string())
                    repository.deletelastDataUser(lastUser!!.uid)
                    return@io
                }
                authListener?.onFailure("")
            }catch(e: ApiException){
                authListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                authListener?.onFailure(e.message!!)
            }
        }
    }

    fun onSignupFinsh(view: View){

        // update healthstats array
        if (isNone) {
            this.healthStats.add("none")
        }else{
            if (isHeart){
                this.healthStats.add("heart")
            }
            if (isAsmathic){
                this.healthStats.add("asmathic")
            }
            if (isOther){
                this.healthStats.add("other")
            }
        }

        if(healthStats.isNullOrEmpty() || day.isNullOrEmpty() || mounth.isNullOrEmpty() || year.isNullOrEmpty() || gender.isNullOrEmpty()){
            authListener?.onFailure("Invalid data")
            return
        }


        Coroutines.main {
            try {
                val authResponse = repository.updateProfil(healthStats!!,day+"-"+mounth+"-"+year,gender!!,PreferenceProvider(view.context).getToken()!!)
                authResponse?.let {
                    authListener?.onSuccess(it.string())
                    return@main
                }
                authListener?.onFailure("")
            }catch(e: ApiException){
                authListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                authListener?.onFailure(e.message!!)
            }
        }
    }


    fun onSignupButtonClick(view: View){
        authListener?.onStarted()

        if(phone.isNullOrEmpty()){
            authListener?.onFailure("phone is required")
            return
        }

        mac_adress =  getMacAddr()
        password = mac_adress

        if (!inputcode.isNullOrEmpty() && inputcode!!.length == 4 && inputcode == code){
            when(type){
                "0"->{
                    Coroutines.main {
                        try {
                            val authResponse = repository.userSignup(phone!!.replace("216",""), mac_adress!!, mac_adress!!,password!!)
                            authResponse.token?.let {
                                authListener?.onSuccess(it)
                                return@main
                            }
                            authListener?.onFailure("")
                        }catch(e: ApiException){
                            authListener?.onFailure(e.message!!)
                        }catch (e: NoInternetException){
                            authListener?.onFailure(e.message!!)
                        }
                    }
                }
                "2"->{
                    Coroutines.main {
                        try {
                            val authResponse = repository.updateMac(phone!!.replace("216",""),mac_adress!!)
                            authResponse.let {
                                try {
                                    val authResponse = repository.userLogin(phone!!.replace("216",""), password!!)
                                    authResponse.token?.let {
                                        authListener?.onSuccess(it)
                                        return@main
                                    }
                                    authListener?.onFailure(authResponse.result!!)
                                }catch(e: ApiException){
                                    authListener?.onFailure(e.message!!)
                                }catch (e: NoInternetException){
                                    authListener?.onFailure(e.message!!)
                                }
                                return@main
                            }
                            authListener?.onFailure("")
                        }catch(e: ApiException){
                            authListener?.onFailure(e.message!!)
                        }catch (e: NoInternetException){
                            authListener?.onFailure(e.message!!)
                        }
                    }
                }
            }
        }

    }

    fun getCode(){
        Coroutines.main {
            try {
                val authResponse = repository.verifCode(phone!!)
                authResponse.code?.let {
                    code = it
                    return@main
                }
                authListener?.onFailure("Error")
            }catch(e: ApiException){
                authListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                authListener?.onFailure(e.message!!)
            }
        }
    }

}