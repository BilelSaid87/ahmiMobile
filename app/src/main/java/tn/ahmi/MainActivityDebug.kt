package tn.ahmi

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import tn.ahmi.ble.Constants
import tn.ahmi.ble.DebugData
import tn.ahmi.ble.InteractionsAdapter
import tn.ahmi.ble.Utils
import tn.ahmi.ble.logging.CentralLog
import tn.ahmi.ble.logging.SDLog
import tn.ahmi.data.db.entities.User
import tn.ahmi.databinding.ActivityMainBinding
import tn.ahmi.service.Actions
import tn.ahmi.service.EndlessService
import tn.ahmi.service.ServiceState
import tn.ahmi.service.getServiceState
import tn.ahmi.ui.auth.AuthViewModel
import tn.ahmi.ui.auth.AuthViewModelFactory


class MainActivityDebug : AppCompatActivity(), KodeinAware {

    companion object {
        lateinit var interactionsList: ArrayList<DebugData>
        lateinit var adapter: InteractionsAdapter
    }

    private val TAG = "MainActivityDebug"
    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private val handler1 = Handler()
    private val handler2 = Handler()


    private val runnable = object : Runnable {
        override fun run() {
            Log.i(TAG, "Interactions Adapter notify data set changed called")
            adapter.notifyDataSetChanged()
            handler1.postDelayed(this, Constants.saveInteractionsToSQLiteTimeRate + 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        val viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivityDebug,
                    Manifest.permission.BLUETOOTH
                ) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(
                    this@MainActivityDebug,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_DENIED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 0
                )
            } else {

                btnActive.setOnCheckedChangeListener { _, check ->
                    if (check) {
                        if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
                            BluetoothAdapter.getDefaultAdapter().enable()
                        }
                        if (Utils.checkBluetooth(BluetoothAdapter.getDefaultAdapter())) {
                            actionOnService(Actions.START)
                        }
                    } else {
                        actionOnService(Actions.STOP)
                    }
                }
            }
        }

        // check service state
        btnActive.isChecked = (getServiceState(this) != Actions.STOP)

        interactionsList = ArrayList<DebugData>()
        adapter = InteractionsAdapter(
            this,
            R.layout.interaction_list_item,
            interactionsList
        )
        val lv = findViewById<ListView>(R.id.interactions_lv)
        lv.adapter = adapter
        handler1.post(runnable)
    }

    override fun onStart() {
        val path:String = Environment.getExternalStorageDirectory().absolutePath.toString()
        Utils.toast(this.applicationContext, "Write log in SD card ${SDLog.isWritable}")
        Utils.toast(this.applicationContext,"Absolute path is path $path")
        CentralLog.i(TAG, "Write log in SD card ${SDLog.isWritable}")
        CentralLog.i(TAG, "Absolute path is $path")
        super.onStart()
    }


    private fun actionOnService(action: Actions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return
        Intent(this, EndlessService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.i(TAG, "Starting the service in >=26 Mode")
                startForegroundService(it)
                return
            }
            Log.i(TAG, "Starting the service in < 26 Mode")
            startService(it)
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "Destroying MainActivity")
        interactionsList.clear()
        adapter.clear()
        handler1.removeCallbacksAndMessages(null)
        handler2.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

}
