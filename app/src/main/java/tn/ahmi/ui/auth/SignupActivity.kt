package tn.ahmi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_signup.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import tn.ahmi.MainActivityDebug
import tn.ahmi.MainActivityRelease
import tn.ahmi.R
import tn.ahmi.ble.Constants
import tn.ahmi.ble.Session
import tn.ahmi.databinding.ActivitySignupBinding
import tn.ahmi.util.hide
import tn.ahmi.util.show
import tn.ahmi.util.snackbar
import java.util.*
import kotlin.collections.ArrayList


class SignupActivity : AppCompatActivity(), AuthListener, KodeinAware {

    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()

    private var gender : String = ""
    private var isHeart : Boolean = false
    private var isAsmathic : Boolean = false
    private var isOther : Boolean = false
    private var isNone : Boolean = false
    lateinit var  viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        val binding: ActivitySignupBinding = DataBindingUtil.setContentView(this, R.layout.activity_signup)
        binding.viewmodel = viewModel
        viewModel.authListener = this
        spYear.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.year = spYear.getItemAtPosition(position).toString()
            }

        }
        spDay.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.day = spDay.getItemAtPosition(position).toString()
            }

        }
        spMonth.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.mounth = spMonth.getItemAtPosition(position).toString()
            }

        }

        maleSpinnerforyear()
        makeGander()
        makeIssues()
    }

    fun maleSpinnerforyear(){
        val years = ArrayList<String>()
        val thisYear: Int = Calendar.getInstance().get(Calendar.YEAR)
        years.add(getString(R.string.year))
        for (i in 1900..thisYear) {
            years.add(i.toString())
        }
        val adapter = ArrayAdapter(this, R.layout.simple_text, years)
        spYear.adapter = adapter


    }

    fun makeGander(){
        btnMan.setOnClickListener {
            btnMan.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_selected)
            btnWomen.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_noselected)
            gender = "male"
            viewModel.gender = gender
        }

        btnWomen.setOnClickListener {
            btnWomen.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_selected)
            btnMan.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_noselected)
            gender = "female"
            viewModel.gender = gender
        }
    }

    fun makeIssues(){
        btnHeart.setOnClickListener {
            if (!isHeart){
                btnHeart.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_selected)
                btnNone.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_noselected)
                isNone = false
                isHeart = true
            }else{
                btnHeart.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_noselected)
                isHeart = false
            }
            viewModel.isHeart = isHeart
        }
        btnAsmathic.setOnClickListener {
            if (!isAsmathic){
                btnAsmathic.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_selected)
                isAsmathic = true
                btnNone.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_noselected)
                isNone = false
            }else{
                btnAsmathic.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_noselected)
                isAsmathic = false
            }
            viewModel.isAsmathic = isAsmathic

        }
        btnOther.setOnClickListener {
            if (!isOther){
                btnOther.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_selected)
                isOther = true
                btnNone.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_noselected)
                isNone = false
            }else{
                btnOther.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_noselected)
                isOther = false
            }
            viewModel.isOther = isOther

        }
        btnNone.setOnClickListener {
            if (!isNone){
                btnNone.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_selected)
                btnHeart.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_noselected)
                btnAsmathic.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_noselected)
                btnOther.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_noselected)
                isNone = true
                isOther = false
                isHeart = false
                isAsmathic = false
            }else{
                btnNone.background = ContextCompat.getDrawable(this@SignupActivity,R.drawable.bg_signup_noselected)
                isNone = false
            }
            viewModel.isNone = isNone

        }


    }

    override fun onStarted() {
        progress_bar.show()
    }

    override fun onSuccess(result: String) {
        if (Constants.sessionVer == Session.RELEASE){
            Intent(this@SignupActivity, MainActivityRelease::class.java).also {
                startActivity(it)
                finish()
            }
        }else{
            Intent(this@SignupActivity, MainActivityDebug::class.java).also {
                startActivity(it)
                finish()
            }
        }
        progress_bar.hide()
    }

    override fun onFailure(message: String) {
        progress_bar.hide()
        root_layout.snackbar(message)
    }
}
