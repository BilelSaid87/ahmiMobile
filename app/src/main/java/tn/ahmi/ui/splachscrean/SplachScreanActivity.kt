package tn.ahmi.ui.splachscrean

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import tn.ahmi.R
import tn.ahmi.databinding.ActivitySplashScreenBinding
import tn.ahmi.ui.auth.AuthViewModel
import tn.ahmi.ui.auth.AuthViewModelFactory
import tn.ahmi.ui.auth.LoginActivity
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import tn.ahmi.MainActivityDebug
import tn.ahmi.MainActivityRelease
import tn.ahmi.ble.Constants
import tn.ahmi.ble.Session
import tn.ahmi.data.preferences.PreferenceProvider
import tn.ahmi.util.LocaleHelper


class SplachScreanActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySplashScreenBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)
        val viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel
        Handler().postDelayed({
            if (PreferenceProvider(this).getToken() == null) {
                if (LocaleHelper.getLanguage(this@SplachScreanActivity) == "en" || LocaleHelper.getLanguage(
                        this@SplachScreanActivity
                    ) == "fr" || LocaleHelper.getLanguage(this@SplachScreanActivity) == "ar"
                ) {
                    Intent(this, LoginActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                } else {
                    Intent(this, SelectLangActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                }

            } else {
                if (Constants.sessionVer == Session.RELEASE) {
                    Intent(this, MainActivityRelease::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }

                } else {
                    Intent(this, MainActivityDebug::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }

                }

            }
        }, 2000)

    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase!!))
    }
}
