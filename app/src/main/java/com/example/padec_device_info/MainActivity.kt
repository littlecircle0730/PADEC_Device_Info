package com.example.padec_device_info

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.padec_device_info.databinding.ActivityMainBinding
import org.eclipse.paho.client.mqttv3.*
import android.os.StatFs
import java.text.DecimalFormat
import android.os.BatteryManager

import android.content.Intent
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ConfigurationInfo
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mqttClient: MQTTClient

    override fun onCreate(savedInstanceState: Bundle?) {
        // connection
        lateinit var input_uri: TextView
        lateinit var input_client_id: TextView
        lateinit var input_user_name: TextView
        lateinit var input_password: TextView
        lateinit var btn_connect: Button
        // subscription,publish
        lateinit var input_sub_topic: TextView
        lateinit var input_pub_topic: TextView
        lateinit var input_message: TextView
        lateinit var btn_sub: Button
        lateinit var btn_unsub: Button
        lateinit var btn_pub: Button
        // device info
        lateinit var btn_device_info: Button
        lateinit var text_device_info: TextView

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        input_uri = findViewById(R.id.input_uri)
        input_client_id = findViewById(R.id.input_client_id)
        input_user_name = findViewById(R.id.input_user_name)
        input_password = findViewById(R.id.input_password)
        btn_connect = findViewById(R.id.button_connect)

        input_sub_topic = findViewById(R.id.input_sub_topic)
        input_pub_topic = findViewById(R.id.input_pub_topic)
        input_message = findViewById(R.id.input_message)
        btn_sub = findViewById(R.id.button_sub)
        btn_unsub = findViewById(R.id.button_unsub)
        btn_pub = findViewById(R.id.button_pub)

        btn_device_info = findViewById(R.id.button_device_info)
        text_device_info = findViewById(R.id.text_device_info)

        btn_connect.setOnClickListener{
            mqttClient= MQTTClient(this@MainActivity, "" +input_uri.text,
                "" +input_client_id.text
            )
            mqttClient.connect(
                "" +input_user_name.text,
                "" +input_password.text,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(this.javaClass.name, "Connection success")

                        Toast.makeText(this@MainActivity, "MQTT Connection success", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(this.javaClass.name, "Connection failure: ${exception.toString()}")

                        Toast.makeText(this@MainActivity, "MQTT Connection fails: ${exception.toString()}",
                            Toast.LENGTH_SHORT).show()
                    }
                },
                object : MqttCallback {
                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        val msg = "Receive message: ${message.toString()} from topic: $topic"
                        Log.d(this.javaClass.name, msg)

                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.d(this.javaClass.name, "Connection lost ${cause.toString()}")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        Log.d(this.javaClass.name, "Delivery complete")
                    }
                })
        }

        btn_pub.setOnClickListener{
            var topic = ""+input_sub_topic.text
            var message = ""+input_message.text
            mqttClient.publish(
                topic,
                message,
                1,
                false,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        val msg ="Publish message: $message to topic: $topic"
                        Log.d(this.javaClass.name, msg)

                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(this.javaClass.name, "Failed to publish message to topic")
                    }
                })
        }

        btn_sub.setOnClickListener{
            var topic = ""+input_sub_topic.text
            mqttClient.subscribe(
                topic,
                1,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        val msg = "Subscribed to: $topic"
                        Log.d(this.javaClass.name, msg)

                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(this.javaClass.name, "Failed to subscribe: $topic")
                    }
                })
        }

        btn_unsub.setOnClickListener{
            var topic = ""+input_pub_topic.text
            mqttClient.unsubscribe(
                topic,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        val msg = "Unsubscribed to: $topic"
                        Log.d(this.javaClass.name, msg)

                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(this.javaClass.name, "Failed to unsubscribe: $topic")
                    }
                })
        }

        btn_device_info.setOnClickListener{
            val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            var info = "Model Info:\n" + getModelInfo() + "\n\n"
            info+="Power Info:\n" + getPowerInfo(this.intent) + "\n\n"
            info+="Storage Info:\n" + getStorageInfo() + "\n\n"
            info+="RAM Info:\n" + getRAMInfo(activityManager) + "\n\n"
            info+="CPU Info:\n" + getCPUInfo() + "\n\n"
            text_device_info.setText(info)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Device Info ---
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

    fun getPowerInfo(batteryStatus: Intent): String? {
        val mBatteryManager:BatteryManager = this.getSystemService(BATTERY_SERVICE) as BatteryManager

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

    fun getModelInfo(): String? {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.lowercase(Locale.getDefault()).startsWith(manufacturer.lowercase(Locale.getDefault()))) {
            capitalize(model)
        } else {
            capitalize(manufacturer).toString() + " " + model
        }
    }
    // Device Info ---

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
}
