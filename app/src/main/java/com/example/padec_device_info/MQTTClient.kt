package com.example.padec_device_info

import android.content.Context
import android.util.Log
import info.mqtt.android.service.Ack
//import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.MqttClient
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener

class MQTTClient(context: Context,
                 serverURI: String,
                 clientID: String = "") {
    var clientId = MqttClient.generateClientId()
    private var mqttClient = MqttAndroidClient(context, serverURI, clientId, Ack.AUTO_ACK)
//    private var mqttClient = MqttAndroidClient(context, "tcp://192.168.0.141:8888", clientId, Ack.AUTO_ACK)

    fun connect(username:   String               = "",
                password:   String               = "",
                cbConnect:  IMqttActionListener,
                cbClient:   MqttCallback) {

        if(mqttClient!=null){
            if(!mqttClient.isConnected) {
                try {
                    mqttClient.setCallback(cbClient)
                    val options = MqttConnectOptions()
//                options.userName = username
//                options.password = password.toCharArray()
                    options.setAutomaticReconnect(true)
                    MqttAsyncClient.generateClientId()
                    mqttClient.connect(options, null, cbConnect)
                } catch (e: MqttException) {
                    Log.d("MqttException", e.toString())
                    e.printStackTrace()
                }
            }
        }
    }

    fun subscribe(topic:        String,
                  qos:          Int                 = 1,
                  cbSubscribe:  IMqttActionListener) {
        try {
            mqttClient.subscribe(topic, qos, null, cbSubscribe)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(topic:          String,
                    cbUnsubscribe:  IMqttActionListener) {
        try {
            mqttClient.unsubscribe(topic, null, cbUnsubscribe)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic:      String,
                msg:        String,
                qos:        Int                 = 1,
                retained:   Boolean             = false,
                cbPublish:  IMqttActionListener) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, cbPublish)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect(cbDisconnect: IMqttActionListener) {
        try {
            mqttClient.disconnect(null, cbDisconnect)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}
