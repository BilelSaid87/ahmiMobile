package tn.ahmi.service

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.widget.Toast

import kotlinx.coroutines.*
import tn.ahmi.MainActivityDebug
import tn.ahmi.MainActivityRelease
import tn.ahmi.R
import tn.ahmi.ble.Constants
import tn.ahmi.ble.InteractionWork
import tn.ahmi.ble.Session
import tn.ahmi.ble.Utils
import tn.ahmi.ble.logging.CentralLog
import tn.ahmi.data.db.AppDatabase
import tn.ahmi.data.network.MyApi
import tn.ahmi.data.network.NetworkConnectionInterceptor
import tn.ahmi.data.preferences.PreferenceProvider
import tn.ahmi.util.log


class EndlessService : Service() {

    private val TAG = "EndlessService"
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private lateinit var work: InteractionWork


    override fun onBind(intent: Intent): IBinder? {
        log("Some component want to bind with the service")
        // We don't provide binding, so return null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            log("using an intent with action $action")
            when (action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else -> log("This should never happen. No action in the received intent")
            }
        } else {
            log(
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        log("The service has been created".toUpperCase())
        val notification = createNotification()
        startForeground(1, notification)
        work = InteractionWork(this.applicationContext, Constants.interactionUUID)
    }

    override fun onDestroy() {
        super.onDestroy()
        log("The service has been destroyed".toUpperCase())
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
    }

    private fun startService() {
        if (isServiceStarted) return
        log("Starting the foreground service task")
        Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }

        // we're starting a loop for ble work in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    CentralLog.i(TAG, "Start Ble Work service periodically")
                    startBleWork()
                }
                delay(Constants.bleWorkServiceWakeTimeRate)
            }
            CentralLog.i(TAG, "End of the work loop in the the service")
        }

        // we're starting a loop for sending interaction data from SQLite to the serverin a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    CentralLog.i(TAG, "Start sending interaction data from SQLite to server db")
                    startSendingData()
                }
                delay(Constants.sendInteractionsToBackEndTimeRate)
            }
            CentralLog.i(TAG, "End of the interaction data sending loop in the service")
        }
    }

    private fun stopService() {
        log("Stopping the foreground service")
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            CentralLog.e(TAG, "Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
        work.stopWork()
    }

    private suspend fun startSendingData() {
        try {
            val lastUser = AppDatabase.invoke(this).getUserDao().getuser()
            if (lastUser != null) {
                CentralLog.d(
                    TAG,
                    "Interactions found in SQLite. Last interaction ID is " + lastUser.uid.toString()
                )
                val lastData = AppDatabase.invoke(this).getUserDao().getuserLastData(lastUser.uid)
                if (MyApi.invoke(NetworkConnectionInterceptor(this)).updateData(
                        PreferenceProvider(
                            this
                        ).getToken(), lastData
                    ).isSuccessful
                ) {
                    CentralLog.d(TAG, "Interactions found sent successfully to the Back-end DB :) ")
                    AppDatabase.invoke(this).getUserDao().deleteAll(lastUser.uid)
                }
            } else {
                CentralLog.d(
                    TAG,
                    "No interactions found in SQLite to be sent to the Back-end interactions DB"
                )
            }
        } catch (e: Exception) {
            CentralLog.e(TAG, "Database Exp: $e")

        }
    }

    private fun startBleWork() {
        if (Utils.checkBluetooth(BluetoothAdapter.getDefaultAdapter())) {
            work.startWork(false)
        } else {
            stopService()
        }

    }

    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "Endless Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Endless Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }
        var pendingIntent: PendingIntent
        if (Constants.sessionVer == Session.RELEASE) {
            pendingIntent =
                Intent(this, MainActivityRelease::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(this, 0, notificationIntent, 0)
                }

        } else {
            pendingIntent = Intent(this, MainActivityDebug::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        }


        val builder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
            ) else Notification.Builder(this)

        return builder
            .setContentTitle("Ahmi Service")
            .setContentText("Ahmi is working now")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ahmi")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }
}
