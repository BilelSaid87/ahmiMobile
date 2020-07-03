package tn.ahmi.ble

class InteractionData(macAddress: String) {

    private var BleMac: String
    private var startTimestamp: Long
    private var avgDist: Double = Double.NaN
    private var minDist: Double = Double.NaN
    private var interactionTime: Long = 0

    init {
        BleMac = macAddress
        startTimestamp = System.currentTimeMillis()
    }


    fun setInteractionTime(timePeriod: Long) {
        this.interactionTime = timePeriod
    }

    fun setInteractionAvgDistance(avgDistance: Double) {
        this.avgDist = avgDistance
    }

    fun setInteractionMinDistance(minDistance: Double) {
        this.minDist = minDistance
    }

    fun getInteractionMac(): String {
        return this.BleMac
    }

    fun getInteractionStartTimestamp(): Long {
        return this.startTimestamp
    }

    fun getInteractionTime(): Long {
        return this.interactionTime
    }

    fun getInteractionAvgDistance(): Double {
        return this.avgDist
    }

    fun getInteractionMinDistance(): Double {
        return this.minDist
    }
}