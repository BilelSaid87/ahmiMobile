package tn.ahmi.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import tn.ahmi.ble.logging.CentralLog
import tn.ahmi.util.log
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class BleScanner(
    val context: Context,
    val serviceUUID: UUID,
    val scanDeviceCallback: ScanDeviceCallback
) {

    private val TAG = "BleScanner"
    private val maskUUID: UUID
    private var mBluetoothAdapter: BluetoothAdapter
    private var isScanning: Boolean
    private var mHandler: Handler
    private val scanningResetRate: Long
    private var count = 0
    private val reset = Constants.isScanningResetEnabled
    private val scanFilter = Constants.filterScanResultsByUUID

    init {
        this.maskUUID = Constants.interactionMaskUUID
        isScanning = false
        this.mHandler = Handler()
        scanningResetRate = Constants.scanningResetRate
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    private val scanCallback18 = object : BluetoothAdapter.LeScanCallback {
        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
            mHandler.post(object : Runnable {
                override fun run() {
                    scanDeviceCallback.done(device!!.name, device.address, rssi, null, null)
                }
            })
        }
    }
    private val scanCallback21 = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val device = result!!.device
            val rssi = result.rssi
            var txPower: Int? = null
            if (Build.VERSION.SDK_INT > 26) {
                txPower = result.txPower
            }
            val manuData: ByteArray =
                result.scanRecord?.getManufacturerSpecificData(1023) ?: "N.A".toByteArray()
            val build_model_str = String(manuData, Charsets.UTF_8)


            mHandler.post(object : Runnable {
                override fun run() {
                    val uuid_str: String? = result.scanRecord?.serviceUuids?.get(0)?.uuid.toString()
                    CentralLog.i(TAG, "current uuid detected in onScanResult callback is $uuid_str")
                    var mac_address = device!!.address
                    var considerCurrentDevice = false
                    if ((scanFilter == FilterEnum.UUID)) {
                        //device.uuids?
                        //result.scanRecord?.serviceUuids?.let {
                        //val uuid_str = it.toString()
                        if (Constants.injectDeviceMacInUUID && uuid_str != null && uuid_str.length >= 24) {
                            if (uuid_str.substring(0, 24).equals(
                                    serviceUUID.toString().substring(0, 24), true
                                )
                            ) {
                                considerCurrentDevice = true
                                // get device mac address injected in service UUID ( last 6 bytes)
                                val sub_str_mac = uuid_str.substring(24, uuid_str.length)
                                val str_builder = StringBuilder(sub_str_mac)
                                str_builder.insert(2, ":")
                                str_builder.insert(5, ":")
                                str_builder.insert(8, ":")
                                str_builder.insert(11, ":")
                                str_builder.insert(14, ":")
                                val injected_mac = str_builder.toString().toUpperCase(Locale.FRENCH)
                                mac_address = injected_mac
                                CentralLog.i(
                                    TAG,
                                    "Injected Mac address $mac_address decoded from service UUID"
                                )
                            }
                        } else if (uuid_str != null) {
                            if (uuid_str.equals(serviceUUID.toString(), true)) {
                                considerCurrentDevice = true
                            }
                        }
                        //}
                    } else {
                        considerCurrentDevice = true
                    }

                    if (considerCurrentDevice) {
                        scanDeviceCallback.done(
                            device.name,
                            mac_address,
                            rssi,
                            txPower,
                            build_model_str
                        )
                    }

                }
            })
            super.onScanResult(callbackType, result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }
    }

    fun checkScanning(): Boolean {
        return isScanning
    }

    fun startScanning() {
        if (!Utils.checkBluetooth(mBluetoothAdapter)) {
            CentralLog.e(TAG, "Bluetooth is off. Scanning can't start")
            stopScanning()
        } else {
            scanDevice()
        }
    }

    fun stopScanning() {
        if (!isScanning) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                mBluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback21)
                mHandler.removeCallbacksAndMessages(null)
                isScanning = false
                count--
                CentralLog.i(TAG, "Scan stopped. Current active scan count is $count")
            } catch (exception: Exception) {
                CentralLog.e(
                    TAG,
                    "Stop scan failed: $exception. Current active scanners count is $count"
                )
            }

        } else {
            try {
                mBluetoothAdapter.stopLeScan(scanCallback18)
                mHandler.removeCallbacksAndMessages(null)
                isScanning = false
                count--
                CentralLog.i(TAG, "Scan stopped. Current active scanners count is $count")
            } catch (exception: Exception) {
                CentralLog.e(
                    TAG,
                    "Stop scan failed: $exception. Current active scanners count is $count"
                )
            }
        }
    }

    private fun scanDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CentralLog.i(TAG, "Scan for DEVICE >= LOLIPOP")
            scanDevice21()
        } else {
            CentralLog.i(TAG, "Scan for DEVICE < LOLIPOP")
            scanDevice18()
        }
    }

    private fun scanDevice18() {
        CentralLog.i(TAG, "Starting BLE scan ...")
        if (!isScanning) {
            try {
                if (reset) {
                    //mHandler.removeCallbacksAndMessages(null)
                    mHandler.postDelayed(object : Runnable {
                        override fun run() {
                            CentralLog.i(
                                TAG,
                                "Reseting BLE scan after $scanningResetRate ms"
                            )
                            stopScanning()
                            startScanning()
                        }
                    }, scanningResetRate)
                }
                mBluetoothAdapter.startLeScan(scanCallback18)
                isScanning = true
                count++
                CentralLog.i(
                    TAG,
                    "BLE scan started successfully. Current active scanners count is $count"
                )
            } catch (exception: Exception) {
                CentralLog.e(
                    TAG,
                    "Start scan failed: $exception.Current active scanners count is $count"
                )
            }
        } else {
            return
        }
    }

    private fun scanDevice21() {
        CentralLog.i(TAG, "Starting BLE scan...")
        if (!isScanning) {
            try {
                val scanSettings = ScanSettings.Builder()
                    .setReportDelay(0)
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()

                var filters: ArrayList<ScanFilter>? = null
                if (scanFilter != FilterEnum.NONE) {
                    filters = ArrayList<ScanFilter>()
                    val builder = ScanFilter.Builder()
                    if (scanFilter == FilterEnum.UUID) {
                        if (Constants.injectDeviceMacInUUID) {
                            builder.setServiceUuid(ParcelUuid(serviceUUID), ParcelUuid(maskUUID))
                        } else {
                            builder.setServiceUuid(ParcelUuid(serviceUUID))
                        }
                    }
                    filters.add(builder.build())
                } else {
                    filters = null
                }

                mBluetoothAdapter.bluetoothLeScanner.startScan(
                    filters,
                    scanSettings,
                    scanCallback21
                )
                isScanning = true
                count++
                if (reset) {
                    //mHandler.removeCallbacksAndMessages(null)
                    mHandler.postDelayed(object : Runnable {
                        override fun run() {
                            CentralLog.i(
                                TAG,
                                "Reseting BLE scan after $scanningResetRate ms"
                            )
                            stopScanning()
                            startScanning()
                        }
                    }, scanningResetRate)
                }
                CentralLog.i(
                    TAG,
                    "BLE scan started successfully. Current active scanners count is $count"
                )
            } catch (e: Exception) {
                CentralLog.e(
                    TAG,
                    "Starting scan failed: exception $e. Current active scanners count is $count"
                )
            }
        } else {
            return
        }
    }
}