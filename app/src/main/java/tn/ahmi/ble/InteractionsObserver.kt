package tn.ahmi.ble

import android.content.Context
import tn.ahmi.MainActivityDebug
import tn.ahmi.ble.logging.CentralLog
import tn.ahmi.data.db.AppDatabase
import tn.ahmi.data.db.entities.User
import tn.ahmi.util.Coroutines
import java.lang.Exception

class InteractionsObserver() {
    private val TAG = "InteractionsObserver"
    //private var idmList = ArrayList<InteractionDataManager>()
    private var idmHashMap = HashMap<String, InteractionDataManager>()
    private val deleteKeys = ArrayList<String>()


    fun addInteractionData(
        address: String,
        rssi: Int,
        txPower: Int?,
        build_model: String?
    ) {
        if (!idmHashMap.containsKey(address)) {
            val idm = InteractionDataManager(address, txPower, build_model)
            idm.appendRssiData(rssi)
            idmHashMap.put(address, idm)
            //idmList.add(idm)
        } else {
            idmHashMap.get(address)!!.appendRssiData(rssi)
        }
    }

    fun saveFinishedInteractionsIntoSQLite(
        context: Context,
        isMaxWaitingTimeHystConsidered: Boolean,
        saveCallback: saveDataCallback?
    ) {

        try {
            idmHashMap.forEach {
                val idm = it.value
                val key = it.key
                val interactionData = idm.calcInteractionMetrics(
                    Constants.deviceMaxWaitingTimeHyst,
                    Constants.rssiSmthTimeWindow,
                    Constants.rssiMeanCalcStampsWindow,
                    isMaxWaitingTimeHystConsidered
                )
                if (interactionData != null) {
                    saveDao(context, interactionData)
                    if (Constants.sessionVer == Session.DEBUG) {
                        MainActivityDebug.interactionsList.add(
                            createDebugData(
                                interactionData,
                                idm.txPower,
                                idm.model
                            )
                        )
                        CentralLog.i(
                            TAG,
                            "InteractionsList for adapter extended size ${MainActivityDebug.interactionsList.size}"
                        )
                    }

                    deleteKeys.add(key)
                }
            }
            for (key in deleteKeys) {
                CentralLog.i(TAG, "Removing saved interactions in SQLite from HashMap")
                idmHashMap.remove(key)
            }
            deleteKeys.clear()
        } catch (e: Exception) {
            CentralLog.i(TAG, "Exception in saveFinishedInteractionsIntoSQLite method $e")
        }
        saveCallback?.done()
    }


    private fun saveDao(context: Context, interaction: InteractionData) {
        CentralLog.i(
            TAG,
            "Device ${interaction.getInteractionMac()}: Saving interaction data into SQLite db"
        )
        Coroutines.main {
            val db = AppDatabase.invoke(context)
            db.getUserDao().upsert(createSqliteDataInstance(interaction))
        }
    }

    private fun createSqliteDataInstance(interaction: InteractionData): User {
        val user = User()
        user.BleMac = interaction.getInteractionMac()
        user.startTimeStamp = interaction.getInteractionStartTimestamp()
        user.avgDist = interaction.getInteractionAvgDistance()
        user.minDist = interaction.getInteractionMinDistance()
        user.interractionTime = interaction.getInteractionTime()
        return user
    }

    private fun createDebugData(
        interaction: InteractionData,
        txPower: Int?,
        model: String?
    ): DebugData {
        var debug = DebugData()
        debug.BleMac = interaction.getInteractionMac()
        debug.startTimestamp = interaction.getInteractionStartTimestamp()
        debug.avgDist = interaction.getInteractionAvgDistance()
        debug.minDist = interaction.getInteractionMinDistance()
        debug.interactionTime = interaction.getInteractionTime()
        debug.txPower = txPower
        debug.buildModel = model
        return debug
    }

}