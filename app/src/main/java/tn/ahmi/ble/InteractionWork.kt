package tn.ahmi.ble

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import tn.ahmi.ble.logging.CentralLog
import java.util.*


class InteractionWork(val context: Context, interactionUUID: UUID) {

    private val TAG = "InteractionWork"
    private var bleScanner: BleScanner
    private var bleAdvertiser: BleAdvertiser
    private var observer: InteractionsObserver
    private var iter = 0

    init {
        observer = InteractionsObserver()
        bleScanner = BleScanner(context, interactionUUID, object : ScanDeviceCallback {
            override fun done(
                name: String?,
                address: String,
                rssi: Int,
                txPower: Int?,
                build_model: String?
            ) {
                observer.addInteractionData(address, rssi, txPower, build_model)
                CentralLog.i(
                    TAG,
                    "Device detected while scanning ${name} with address ${address} " +
                            "## txPower = $txPower ##" +
                            "## rssi  = $rssi ##" +
                            "## model = $build_model ##"
                )
            }
        })
        bleAdvertiser = BleAdvertiser(context, interactionUUID)
    }

    fun startWork(isCallFromService: Boolean = false) {
        iter++
        CentralLog.i(TAG, "Starting Interaction Work iteration $iter")
        if (checkPermissions()) {
            if (!bleAdvertiser.checkAdvertising() || isCallFromService) {
                bleAdvertiser.startAdvertising()
            }
            if (!bleScanner.checkScanning() || isCallFromService) {
                bleScanner.startScanning()
            }

            //calculate interactions metrics and save finished interactions to SQLite after
            // numCyclesToSaveInSQLite cycles
            if (iter % Constants.numCyclesToSaveInSQLite == 0) {
                bleScanner.stopScanning()
                CentralLog.d(
                    TAG,
                    "Calculating finished interactions metrics and saving into local DB"
                )
                observer.saveFinishedInteractionsIntoSQLite(
                    context,
                    true,
                    object : saveDataCallback {
                        override fun done() {
                            bleScanner.startScanning()
                        }
                    })
            }

            /*
            // call this function in a loop for a predefined time cycle
            if (iter > 1) {
                GlobalScope.launch(Dispatchers.IO) {
                    while (getServiceState(context) == ServiceState.STARTED) {
                        launch(Dispatchers.IO) {
                            CentralLog.d(
                                TAG,
                                "Calculating finished interactions metrics and saving into local DB"
                            )
                            /*
                            if (bleScanner.checkScanning()) {
                                CentralLog.d(TAG, "Stop scanning to save finished Interactions")
                                bleScanner.stopScanning()
                            }

                             */
                            observer.saveFinishedInteractionsIntoSQLite(
                                context,
                                true,
                                object : saveDataCallback {
                                    override fun done() {
                                        /*
                                        if (!bleScanner.checkScanning()) {
                                            CentralLog.d(
                                                TAG,
                                                "Start scanning again after saving finished Interactions"
                                            )
                                            bleScanner.startScanning()
                                        }

                                         */
                                    }
                                })
                        }
                        delay(Constants.saveInteractionsToSQLiteTimeRate)
                    }
                    CentralLog.i(TAG, "End of the loop for the service")
                }
            }
            */
        } else {
            stopWork()
        }
    }

    fun stopWork() {
        iter = 0
        CentralLog.i(TAG, "Stopping Interaction Work...")
        if (bleAdvertiser.checkAdvertising()) {
            bleAdvertiser.stopAdvertising()
        }
        if (bleScanner.checkScanning()) {
            bleScanner.stopScanning()
        }
        observer.saveFinishedInteractionsIntoSQLite(context, false, null)
    }

    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return !((ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED))
        } else {
            return true
        }
    }
}