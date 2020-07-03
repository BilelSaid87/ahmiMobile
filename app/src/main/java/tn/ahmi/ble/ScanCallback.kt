package tn.ahmi.ble

import android.bluetooth.BluetoothDevice

interface ScanDeviceCallback {

    fun done(name: String?, address:String , rssi: Int, txPower: Int?, build_model: String?)

}

interface saveDataCallback {
    fun done()
}