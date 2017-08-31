package pl.rmakowiecki.smartalarmcore

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun sendAlarmTriggerData(gpio: Gpio) {
        FirebaseDatabase
                .getInstance()
                .reference
                .child("trigger")
                .setValue(gpio.value)
                .addOnCompleteListener {
                    Log.d(javaClass.simpleName, if (it.isSuccessful) "Server trigger data push successful" else "Error sending data to server")
                }
    }
}
