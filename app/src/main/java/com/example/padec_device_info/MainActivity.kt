package com.example.padec_device_info

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.padec_device_info.databinding.ActivityMainBinding
import org.eclipse.paho.client.mqttv3.*

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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
}
