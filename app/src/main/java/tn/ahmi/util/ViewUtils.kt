package tn.ahmi.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.net.NetworkInterface
import java.util.*


fun Context.toast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_LONG ).show()
}

fun ProgressBar.show(){
    visibility = View.VISIBLE
}

fun ProgressBar.hide(){
    visibility = View.GONE
}

fun View.snackbar(message: String){
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackbar ->
        snackbar.setAction("Ok") {
            snackbar.dismiss()
        }
    }.show()
}

fun <T> Context.openActivity(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    var intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun log(msg: String) {
    Log.d("ENDLESS-SERVICE", msg)
}

fun getMacAddr(): String? {
    try {
        val all: List<NetworkInterface> =
            Collections.list(NetworkInterface.getNetworkInterfaces())
        for (nif in all) {
            if (!nif.name.equals("wlan0" , true)) continue
            val macBytes: ByteArray = nif.hardwareAddress ?: return ""
            val res1 = StringBuilder()
            for (b in macBytes) { //res1.append(Integer.toHexString(b & 0xFF) + ":");
                res1.append(String.format("%02X:", b))
            }
            if (res1.length > 0) {
                res1.deleteCharAt(res1.length - 1)
            }
            return res1.toString()
        }
    } catch (ex: Exception) {
    }
    return "02:00:00:00:00:00"
}