package pl.rmakowiecki.smartalarmcore

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import pl.rmakowiecki.smartalarmcore.background.UsbStateBroadcastReceiver
import pl.rmakowiecki.smartalarmcore.peripheral.beam.BeamBreakDetectorPeripheryContract
import pl.rmakowiecki.smartalarmcore.remote.AlarmInteractorContract

class AlarmActivity : AppCompatActivity() {

    private lateinit var alarmController: AlarmController
    val usbReceiver = UsbStateBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerReceiver(usbReceiver, IntentFilter().apply {
            addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED")
            addAction("android.hardware.usb.action.USB_DEVICE_DETACHED")
        })

        printWifiNetworkStatus()
        alarmController = initSystemController()
        alarmController.observeAlarm()
    }

    private fun initSystemController() = AlarmController(
            BeamBreakDetectorPeripheryContract.create(),
            AlarmInteractorContract.create()
    )

    private fun printWifiNetworkStatus() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        if (wifi.isConnected) {
            Log.d(javaClass.simpleName, wifi.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }
}
