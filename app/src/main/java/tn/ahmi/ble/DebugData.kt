package tn.ahmi.ble

class DebugData {
    var BleMac: String = ""
    var startTimestamp: Long = 0
    var avgDist: Double = Double.NaN
    var minDist: Double = Double.NaN
    var interactionTime: Long = 0
    var txPower:Int? = null
    var buildModel:String? = null
}