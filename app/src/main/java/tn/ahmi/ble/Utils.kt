package tn.ahmi.ble

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.widget.Toast
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

object Utils {


    fun toast(context: Context, msg: String) {
        // make toast in context to show msg String
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun checkBluetooth(bluetoothAdapter: BluetoothAdapter?): Boolean {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        return !(bluetoothAdapter == null || !bluetoothAdapter.isEnabled)
    }

    fun rssi2distanceRampFcn(rssi: Int): Double {
        val dist: Double
        if (rssi > -30) {
            dist = 0.5
        } else if (rssi > -50) {
            dist = 1.0
        } else if (rssi > -60) {
            dist = 1.2
        } else if (rssi > -80) {
            dist = 1.5
        } else if (rssi > -100) {
            dist = 3.0
        } else if (rssi > -120) {
            dist = 5.0
        } else {
            dist = 8.0
        }
        return dist
    }

    fun rssi2distanceCntFcn(rssi: Int, txPower: Int): Double {
        var dist: Double
        dist = Math.pow(
            10.0,
            ((txPower.toDouble() - rssi.toDouble()) / (10.0 * Constants.blePropagationConstant))
        )
        dist = BigDecimal(dist).setScale(2, RoundingMode.HALF_EVEN).toDouble()
        return dist
    }

    fun smoothRssiUsingSlidingWindow(rssiList: List<Int>, meanWindow: Int): Int {
        val smth_rssi: Int
        val size = rssiList.size
        if (size > meanWindow) {
            rssiList.sorted()
            val start_index = size / 2 - meanWindow / 2
            val end_index = start_index + meanWindow
            val sub_rssi_list = rssiList.subList(start_index, end_index)
            smth_rssi =
                Math.round(sub_rssi_list.sum().toDouble() / sub_rssi_list.size.toDouble()).toInt()
        } else {
            smth_rssi = Math.round(rssiList.sum().toDouble() / size.toDouble()).toInt()
        }
        return smth_rssi
    }

    fun getDate(milliSeconds:Long, dateFormat:String):String
    {
        val formatter = SimpleDateFormat(dateFormat)
        val calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}