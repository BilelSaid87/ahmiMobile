package tn.ahmi.ui.splachscrean

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_select_lang.*
import tn.ahmi.R
import tn.ahmi.ui.auth.LoginActivity
import tn.ahmi.util.LocaleHelper


class SelectLangActivity : AppCompatActivity() {

    var lang : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_lang)

        btnNext.setOnClickListener {
            LocaleHelper.setLocale(this,lang)
            showSignInScreen()
        }

        btnENG.setOnClickListener(listener)
        btnES.setOnClickListener(listener)
        btnFR.setOnClickListener(listener)

    }


    var listener = View.OnClickListener { v ->
        btnENG.noselected()
        btnES.noselected()
        btnFR.noselected()
        when (v.id) {
            R.id.btnENG -> {
                btnENG.selected()
                btnES.noselected()
                btnFR.noselected()
                lang = "en"
                btnNext.visibility = View.VISIBLE
            }
            R.id.btnES -> {
                btnENG.noselected()
                btnES.selected()
                btnFR.noselected()
                lang = "es"
                btnNext.visibility = View.VISIBLE

            }
            R.id.btnFR -> {
                btnENG.noselected()
                btnES.noselected()
                btnFR.selected()
                lang = "fr"
                btnNext.visibility = View.VISIBLE
            }
        }
    }


    private fun showSignInScreen() {
        startActivity(Intent(this, IntroActivity::class.java)).also {
                finish()
             }
    }
    private fun TextView.noselected()
    {
        this.setTextColor(ContextCompat.getColor(this@SelectLangActivity,R.color.white))
        this.background = ContextCompat.getDrawable(this@SelectLangActivity,R.drawable.bg_text_noselected)
    }

    private fun TextView.selected() {
        this.background = ContextCompat.getDrawable(this@SelectLangActivity,R.drawable.bg_text_selected)
        this.setTextColor(ContextCompat.getColor(this@SelectLangActivity,R.color.white))
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase!!))
    }

}
