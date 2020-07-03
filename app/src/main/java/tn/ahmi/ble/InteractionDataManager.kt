package tn.ahmi.ble

import android.util.Log
import tn.ahmi.ble.logging.CentralLog
import kotlin.collections.ArrayList

class InteractionDataManager(
    val macAddress: String,
    val txPower: Int?,
    val model: String?
) {

    private val TAG = "InteractionDataManager"
    private val interactionData = InteractionData(macAddress)
    private val tStart = this.interactionData.getInteractionStartTimestamp()
    private val rssiOverTime = ArrayList<List<Any>>()

    fun appendRssiData(rssi: Int) {
        val new: List<Any>
        if (this.rssiOverTime.isNotEmpty()) {
            val now = System.currentTimeMillis()
            new = listOf(now, rssi)
        } else {
            new = listOf(tStart, rssi)
        }
        this.rssiOverTime.add(new)
    }

    fun calcInteractionMetrics(
        maxDeviceWaitingTime: Long,
        smth_time_window: Long,
        meanCalcWindow: Int,
        isMaxWaitingTimeHystConsidered: Boolean
    ): InteractionData? {
        if (isMaxWaitingTimeHystConsidered && !checkWaitingTimeExpiration(maxDeviceWaitingTime)) {
            CentralLog.i(
                TAG,
                "Device $macAddress: Interaction time not expired yet --> Metrics calc. skipped !!"
            )
            return null
        } else {
            CentralLog.i(
                TAG,
                "Device $macAddress: Interaction time expired --> Start calculating distance and time metrics !!"
            )
            val smth_rssi_list = smoothRssiData(smth_time_window, meanCalcWindow)
            val avg_rssi = calcAvgRssi(smth_rssi_list)
            val max_rssi = smth_rssi_list.max()!!
            val avg_dist = calcDistance(avg_rssi, txPower, model)
            val min_dist = calcDistance(max_rssi, txPower, model)
            this.interactionData.setInteractionAvgDistance(avg_dist)
            this.interactionData.setInteractionMinDistance(min_dist)
            this.interactionData.setInteractionTime(((this.rssiOverTime.last())[0] as Long) - tStart)
            return this.interactionData
        }
    }

    private fun calcDistance(rssi: Int, txPower: Int?, model: String?): Double {
        var dist: Double = Double.MAX_VALUE
        val dist_ramp = Utils.rssi2distanceRampFcn(rssi)
        if (txPower != null) {
            if (txPower != 127) {
                dist = Utils.rssi2distanceCntFcn(rssi, txPower)
                CentralLog.i(
                    TAG, "Device $macAddress: Distance calculated based on received txPower " +
                            "$txPower  dist: $dist "
                )
            }
        } else {
            val model_based_txPower = getModelCalibratedTxpower(model)
            if (model_based_txPower != null) {
                dist = Utils.rssi2distanceCntFcn(rssi, model_based_txPower)
                CentralLog.i(
                    TAG, "Device $macAddress: Distance calculated based on model calibrated " +
                            "txPower $model_based_txPower dist: $dist "
                )
            }
        }

        if (dist == Double.MAX_VALUE || dist > 1.1 * dist_ramp) {
            CentralLog.i(
                TAG,
                "Device $macAddress: overwrite continious distance $dist with ramp distance $dist_ramp"
            )
            dist = dist_ramp
        }
        return dist
    }

    private fun smoothRssiData(
        smooth_time_window: Long,
        meanCalcWindow: Int
    ): ArrayList<Int> {
        CentralLog.i(TAG, "Device $macAddress: Smoothing RSSI signal over time")
        val smth_rssi_list = ArrayList<Int>()
        val splitted_rssi_list = splitRssiListBySmoothTimeWindow(smooth_time_window)
        for (ii in 0 until splitted_rssi_list.size) {
            val smth_rssi =
                Utils.smoothRssiUsingSlidingWindow(splitted_rssi_list.get(ii), meanCalcWindow)
            smth_rssi_list.add(smth_rssi)
        }
        return smth_rssi_list
    }

    private fun splitRssiListBySmoothTimeWindow(smoothingTimeWindow: Long): ArrayList<List<Int>> {
        val rssi_splitted = ArrayList<List<Int>>()
        var sub_list = this.rssiOverTime
        var t0 = this.tStart
        while (sub_list.isNotEmpty()) {
            val tmp = sub_list.partition { ((it[0] as Long) - t0) <= smoothingTimeWindow }
            sub_list = tmp.second as ArrayList<List<Any>>
            rssi_splitted.add(tmp.first.map { it[1] as Int })
            if (sub_list.isNotEmpty()) {
                rssi_splitted.add(tmp.second.map { it[1] as Int })
                t0 = tmp.second.first()[0] as Long
            }
        }
        return rssi_splitted
    }

    private fun checkWaitingTimeExpiration(time_threshold: Long): Boolean {
        CentralLog.i(TAG, "Device $macAddress: Check waiting time expiration condition")
        val last_timestamp = this.rssiOverTime.last()[0] as Long
        val now = System.currentTimeMillis()
        val delta_time = now - last_timestamp
        return delta_time >= time_threshold
    }

    private fun calcAvgRssi(rssi_list: ArrayList<Int>): Int {
        return Math.round(rssi_list.sum().toDouble() / rssi_list.size.toDouble()).toInt()
    }

    private fun getModelCalibratedTxpower(model: String?): Int? {
        if ((model.isNullOrEmpty()) || (model.equals("N.A",true))) {
            return null
        } else {
            // Put calibration data here. Will return null for now !!
            return null
        }
    }
}

