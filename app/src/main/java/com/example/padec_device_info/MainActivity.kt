package com.example.padec_device_info

import android.Manifest
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.padec_device_info.databinding.ActivityMainBinding
import org.eclipse.paho.client.mqttv3.*

import android.content.Intent
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.os.*
import androidx.activity.result.contract.ActivityResultContracts
import java.util.*
import kotlin.random.Random.Default.nextInt

class MainActivity : AppCompatActivity() {
    private val mPADECService: PADECService = PADECService()

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
        lateinit var button_req: Button
        // device info
        lateinit var btn_device_info: Button
        lateinit var text_device_info: TextView

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        input_uri = findViewById(R.id.input_uri)
        //input_client_id = findViewById(R.id.input_client_id)
        //input_user_name = findViewById(R.id.input_user_name)
        //input_password = findViewById(R.id.input_password)
        btn_connect = findViewById(R.id.button_connect)

        input_sub_topic = findViewById(R.id.input_sub_topic)
        //input_pub_topic = findViewById(R.id.input_pub_topic)
        //input_message = findViewById(R.id.input_message)
        btn_sub = findViewById(R.id.button_sub)
        btn_unsub = findViewById(R.id.button_unsub)
        //btn_pub = findViewById(R.id.button_pub)
        button_req = findViewById(R.id.button_req)

        btn_device_info = findViewById(R.id.button_device_info)
        text_device_info = findViewById(R.id.text_device_info)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }

        btn_connect.setOnClickListener{
            mqttClient= MQTTClient(this@MainActivity, "" +input_uri.text,
                ""
            )
            mqttClient.connect(
                "",
                "",
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

                        val returnPrivateTopic="PADEC"+message

                        // Handle receive message
                        if (topic=="PadecRequest"){
                            // send device info back
                            val returnMessage=getDeviceInfo()
                            mqttClient.publish(
                                returnPrivateTopic,
                                returnMessage,
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
                        } else{
                            // show receviced device info
                            text_device_info.setText(message.toString())
                        }
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.d(this.javaClass.name, "Connection lost ${cause.toString()}")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        Log.d(this.javaClass.name, "Delivery complete")
                    }
                })
        }

//        btn_pub.setOnClickListener{
//            var topic = ""+input_sub_topic.text
//            var message = ""+input_message.text
//            mqttClient.publish(
//                topic,
//                message,
//                1,
//                false,
//                object : IMqttActionListener {
//                    override fun onSuccess(asyncActionToken: IMqttToken?) {
//                        val msg ="Publish message: $message to topic: $topic"
//                        Log.d(this.javaClass.name, msg)
//
//                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
//                    }
//
//                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
//                        Log.d(this.javaClass.name, "Failed to publish message to topic")
//                    }
//                })
//        }

        // Request device info through the topic "PadecRequest"
        button_req.setOnClickListener{
            val topic = "PadecRequest"
            val message = nextInt(Math.pow(10.0, 10.0).toInt()).toString() //create a number as id to get private topic
            val privateTopic = "PADEC"+message
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
            // sub private topic
            mqttClient.subscribe(
                privateTopic,
                1,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        val msg = "Subscribed to privateTopic: $privateTopic"
                        Log.d(this.javaClass.name, msg)

                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(this.javaClass.name, "Failed to subscribe: $privateTopic")
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

        val intent = Intent(this@MainActivity, PADECService::class.java)
        startService(intent)

        btn_device_info.setOnClickListener{
            text_device_info.text = getDeviceInfo()
        }
    }

    fun getDeviceInfo(): String {
        val mBatteryManager:BatteryManager = this.getSystemService(BATTERY_SERVICE) as BatteryManager
        val activityManager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        var info = "Device Name:\n" + mPADECService.getDeviceName(this) + "\n\n"
        info += "Model Info:\n" + mPADECService.getModelInfo() + "\n\n"
        info+="Power Info:\n" + mPADECService.getPowerInfo(this.intent,mBatteryManager) + "\n\n"
        info+="Storage Info:\n" + mPADECService.getStorageInfo() + "\n\n"
        info+="RAM Info:\n" + mPADECService.getRAMInfo(activityManager) + "\n\n"
        info+="CPU Info:\n" + mPADECService.getCPUInfo() + "\n\n"
        return info
    }

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
        }else{
            //deny
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }
}