package tn.ahmi.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_verification_code.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import tn.ahmi.MainActivityDebug
import tn.ahmi.R
import tn.ahmi.data.preferences.PreferenceProvider
import tn.ahmi.databinding.ActivityVerificationCodeBinding
import tn.ahmi.util.hide
import tn.ahmi.util.show
import tn.ahmi.util.snackbar

class VerificationCodeActivity : AppCompatActivity(), AuthListener, KodeinAware {

    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityVerificationCodeBinding = DataBindingUtil.setContentView(this, R.layout.activity_verification_code)
        val viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.authListener = this
        viewModel.phone = intent.getStringExtra("phone")
        viewModel.type = intent.getStringExtra("type")
        viewModel.getCode()
        btnResend.setOnClickListener {
            viewModel.getCode()
            btnResend.isEnabled = false
            btnResend.setTextColor(resources.getColor(R.color.gray))
        }
    }

    override fun onStarted() {
        progress_bar.show()
    }

    override fun onSuccess(result: String) {
        PreferenceProvider(this).saveToken(result)
        progress_bar.hide()
        if (intent.getStringExtra("type") == "0"){
            Intent(this, SignupActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
        }else{
            PreferenceProvider(this).saveToken(result)
            Intent(this, MainActivityDebug::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
        }

    }

    override fun onFailure(message: String) {
        progress_bar.hide()
        root_layout.snackbar(message)
    }
}
