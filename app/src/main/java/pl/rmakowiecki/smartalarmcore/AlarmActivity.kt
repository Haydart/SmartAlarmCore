package pl.rmakowiecki.smartalarmcore

import AlarmController
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import pl.rmakowiecki.smartalarmcore.peripheral.BeamBreakDetectorPeripheryContract

class AlarmActivity : AppCompatActivity() {

    private lateinit var alarmController: AlarmController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        printWifiNetworkStatus()
        alarmController = initSystemController()
    }

    private fun initSystemController() = AlarmController(BeamBreakDetectorPeripheryContract.create())

    private fun printWifiNetworkStatus() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        if (wifi.isConnected) {
            Log.d(javaClass.simpleName, wifi.toString())
        }
    }
}
