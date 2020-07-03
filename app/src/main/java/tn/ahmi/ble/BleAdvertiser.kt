package tn.ahmi.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.appcompat.widget.ViewUtils
import tn.ahmi.ble.logging.CentralLog
import tn.ahmi.util.getMacAddr
import java.util.*

class BleAdvertiser(val context: Context, private var serviceUUID: UUID) {

    private val TAG = "BleAdvertiser"
    private var isAdvertising = false
    private val advertisingResetRate = Constants.advertisingResetRate
    private var handler = Handler()
    private var count = 0
    private val reset = Constants.isAdvertisingResetEnabled
    private val advertiseSettings = AdvertiseSettings.Builder()
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        .setConnectable(true)
        .setTimeout(0)
        .build()
    private var advertiser: BluetoothLeAdvertiser? =
        BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser

    private var advertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            count++
            CentralLog.i(
                TAG,
                "Advertising started successfully-> Advertising settings: " + settingsInEffect.toString()
            )
            CentralLog.i(TAG, "Number of current active advertisers is $count")
            isAdvertising = true
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)

            val reason: String
            when (errorCode) {
                ADVERTISE_FAILED_ALREADY_STARTED -> {
                    reason = "ADVERTISE_FAILED_ALREADY_STARTED"
                    isAdvertising = true
                }
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> {
                    reason = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED"
                    isAdvertising = false
                }
                ADVERTISE_FAILED_INTERNAL_ERROR -> {
                    reason = "ADVERTISE_FAILED_INTERNAL_ERROR"
                    isAdvertising = false
                }
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> {
                    reason = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS"
                    isAdvertising = false
                }
                ADVERTISE_FAILED_DATA_TOO_LARGE -> {
                    reason = "ADVERTISE_FAILED_DATA_TOO_LARGE"
                    isAdvertising = false
                }
                else -> {
                    reason = "UNDOCUMENTED"
                }
            }
            CentralLog.d(TAG, "Advertising onStartFailure: $errorCode - $reason")
        }
    }

    var resetRunnable = Runnable {
        CentralLog.i(TAG, "Reseting BLE advertisement after $advertisingResetRate ms")
        stopAdvertising()
        startAdvertising()
    }

    fun startAdvertisingLegacy() {
        CentralLog.i(
            TAG,
            "Start preparing the advertising legacy --> Default ServiceUUID is ${serviceUUID.toString()}"
        )
        var pUUID = ParcelUuid(serviceUUID)
        if (Constants.injectDeviceMacInUUID) {
            CentralLog.i(TAG, "Inject device MAC address into service UUID")
            var mac_str = getMacAddr()
            var service_uuid_str = serviceUUID.toString()
            CentralLog.i(TAG, "Device Mac address $mac_str will be injected into the service UUID")
            mac_str = mac_str!!.replace(":", "", true)
            val start_index = 24
            val end_index = service_uuid_str.length
            service_uuid_str = service_uuid_str.replaceRange(start_index, end_index, mac_str)
            serviceUUID = UUID.fromString(service_uuid_str)
            pUUID = ParcelUuid(serviceUUID)
            CentralLog.i(
                TAG,
                "Mac address injected into serviceUUID --> New ServiceUUID is ${serviceUUID.toString()}"
            )
        }
        val build_model: String = android.os.Build.MODEL
        val advertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(pUUID)
            .addManufacturerData(1023, build_model.toByteArray())
            .build()
        CentralLog.i(TAG, "Build model added to advertising data: $build_model")
        try {
            Log.d(TAG, "Start advertising...")
            if (reset) {
                handler.postDelayed(resetRunnable, advertisingResetRate)
            }
            advertiser = advertiser ?: BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
            advertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        } catch (e: Throwable) {
            CentralLog.e(TAG, "Failed to start advertising legacy: ${e.message}")
        }
    }

    fun checkAdvertising(): Boolean {
        return this.isAdvertising
    }

    fun startAdvertising() {
        if (!isAdvertising) {
            isAdvertising = true
            startAdvertisingLegacy()
        }
        /*
        else{
            stopAdvertising()
        }
         */
    }

    fun stopAdvertising() {
        try {

            advertiser?.stopAdvertising(advertiseCallback)
            isAdvertising = false
            count--
            handler.removeCallbacksAndMessages(null)
            CentralLog.i(
                TAG,
                "Advertiser stopped successfully. Number of current active advertisers is $count"
            )
        } catch (e: Throwable) {
            CentralLog.e(
                TAG,
                "Failed to stop advertising: ${e.message}. Number of current active advertisers is $count"
            )
        }

    }
}