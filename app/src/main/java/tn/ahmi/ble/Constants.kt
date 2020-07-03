package tn.ahmi.ble

import java.util.*

object Constants {

    // set session version
    val sessionVer = Session.RELEASE

    // service UUID
    val interactionUUID: UUID = UUID.fromString("2DC8BB2B-6EDF-455B-A14A-5CD90EFD3A55")
    val interactionMaskUUID: UUID = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-000000000000")

    // flags
    val filterScanResultsByUUID = FilterEnum.UUID
    var injectDeviceMacInUUID = true
    const val isAdvertisingResetEnabled: Boolean = false
    const val isScanningResetEnabled: Boolean = false

    // relevant time constants and rates
    const val min2sec: Long = 60
    const val sec2millisec: Long = 1000
    const val advertisingResetRate: Long = 60 * sec2millisec
    const val scanningResetRate: Long = 60 * sec2millisec
    const val advertisingTimeGap: Long = 10 * sec2millisec
    const val scanningMinTimeGap: Long = 5 * sec2millisec
    const val scanningMaxTimeGap: Long = 20 * sec2millisec
    const val rssiSmthTimeWindow: Long = 10 * sec2millisec
    const val rssiMeanCalcStampsWindow: Int = 10
    const val deviceMaxWaitingTimeHyst: Long = 2 * sec2millisec
    const val bleWorkServiceWakeTimeRate: Long = 1 * min2sec * sec2millisec
    const val sendInteractionsToBackEndTimeRate: Long = 90* sec2millisec
    const val saveInteractionsToSQLiteTimeRate: Long = 1 * min2sec * sec2millisec
    const val numCyclesToSaveInSQLite:Int = 2

    // rssi propagation constant
    const val blePropagationConstant: Double = 2.0
}

enum class FilterEnum {
    NONE,               // no filter is set (filters == null)
    EMPTY,              // an empty filter is set
    UUID                // filter with specific service UUID is set
}

enum class Session {
    RELEASE,
    DEBUG
}