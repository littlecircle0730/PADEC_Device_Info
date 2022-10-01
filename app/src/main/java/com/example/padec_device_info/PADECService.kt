package com.example.padec_device_info

import android.app.ActivityManager
import android.app.Service
import android.content.Intent
import android.os.*
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context

class PADECService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // Device Info ---
    fun getModelInfo(): String? {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.lowercase(Locale.getDefault()).startsWith(manufacturer.lowercase(Locale.getDefault()))) {
            capitalize(model)
        } else {
            capitalize(manufacturer).toString() + " " + model
        }
    }

    fun getDeviceName(context:Context): String? {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var myDevice: BluetoothAdapter = bluetoothManager.getAdapter()
        val deviceName: String = myDevice.getName()
        return deviceName
    }

    private fun capitalize(s: String?): String? {
        if (s == null || s.length == 0) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first).toString() + s.substring(1)
        }
    }

    fun getPowerInfo(batteryStatus: Intent, mBatteryManager: BatteryManager): String? {
        val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // How are we charging?
        val chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
        val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC

        // //Determine the current battery capacity
        var batteryCapacity=0
        //BATTERY_PROPERTY_CHARGE_COUNTER: Battery capacity in microampere-hours, as an integer.
        // BATTERY_PROPERTY_CAPACITY: the remaining battery capacity as an integer percentage.
        val chargeCounter: Int = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val capacity = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        if(chargeCounter == Integer.MIN_VALUE || capacity == Integer.MIN_VALUE) batteryCapacity=0;
        else batteryCapacity=(chargeCounter/1000/capacity*100)
        return String.format(
            " Is Charging: %s\n Charging by: %s\n Current Battery: %s%%\n Battery Capacity: %s mAh",
            isCharging,
            if (isCharging) if (usbCharge) "USB" else "AC Power" else "NOT Charging",
            capacity,
            batteryCapacity
        )
    }

    fun getStorageInfo(): String? {
        var availableSpace = -1L
        val stat = StatFs(Environment.getExternalStorageDirectory().getPath())
        availableSpace =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) stat.blockSizeLong * stat.availableBlocksLong else stat.availableBlocks
                .toLong() * stat.blockSize.toLong()
        if (availableSpace <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(availableSpace.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(
            "Available Storage Space %s\n",
            DecimalFormat("#,##0.#").format(
                availableSpace / Math.pow(
                    1024.0,
                    digitGroups.toDouble()
                )
            ).toString() + " " + units[digitGroups]
        )
    }

    fun getRAMInfo(actManager: ActivityManager): String? {
        // Declaring MemoryInfo object
        val memoryInfo = ActivityManager.MemoryInfo()

        // Fetching the data from the ActivityManager
        actManager.getMemoryInfo(memoryInfo)

        // Fetching the available and total memory and converting into Giga Bytes
        val availMemory =
            java.lang.Double.valueOf(memoryInfo.availMem.toDouble()) / (1024 * 1024 * 1024)
        val totalMemory =
            java.lang.Double.valueOf(memoryInfo.totalMem.toDouble()) / (1024 * 1024 * 1024)
        return String.format("Available RAM: %s\nTotal RAM: %s\n", availMemory, totalMemory)
    }

    fun getCPUInfo(): String? {
        val sb = StringBuffer()
        sb.append("abi: ").append(Build.CPU_ABI).append("\n")
        if (File("/proc/cpuinfo").exists()) {
            try {
                val file = File("/proc/cpuinfo")
                file.bufferedReader().forEachLine {
                    var aLine: String
                    sb.append(
                        """
                    $it

                    """.trimIndent()
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }
}