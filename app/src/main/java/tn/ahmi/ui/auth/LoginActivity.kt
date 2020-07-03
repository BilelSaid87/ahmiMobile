package tn.ahmi.ui.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import tn.ahmi.MainActivityDebug
import tn.ahmi.R
import tn.ahmi.data.preferences.PreferenceProvider
import tn.ahmi.databinding.ActivityLoginBinding
import tn.ahmi.util.hide
import tn.ahmi.util.show
import tn.ahmi.util.snackbar


class LoginActivity : AppCompatActivity(), AuthListener, KodeinAware {

    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        val viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.authListener = this

    }

    override fun onStarted() {
        progress_bar.show()
    }

    override fun onSuccess(result: String) {
        PreferenceProvider(this).saveToken(result)
        Intent(this@LoginActivity,MainActivityDebug::class.java)
        progress_bar.hide()
    }

    override fun onFailure(message: String) {
        when(message){
            "0"->{
                Intent(this@LoginActivity, VerificationCodeActivity::class.java).also {
                    it.putExtra("phone","216"+inputPhoneNumber.text.toString())
                    it.putExtra("type",message)
                    startActivity(it)
                }
            }
            "2"->{
                Intent(this@LoginActivity, VerificationCodeActivity::class.java).also {
                    it.putExtra("phone","216"+inputPhoneNumber.text.toString())
                    it.putExtra("type",message)
                    startActivity(it)
                }
            }
        }
        progress_bar.hide()
        root_layout.snackbar(message)
    }

}
