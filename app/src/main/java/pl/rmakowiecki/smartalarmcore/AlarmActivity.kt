package pl.rmakowiecki.smartalarmcore

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import pl.rmakowiecki.smartalarmcore.peripheral.beam.BeamBreakDetectorPeriphery
import pl.rmakowiecki.smartalarmcore.peripheral.camera.CameraPeripheryContract
import pl.rmakowiecki.smartalarmcore.peripheral.motion.MotionSensorPeriphery
import pl.rmakowiecki.smartalarmcore.remote.AlarmBackendContract
import pl.rmakowiecki.smartalarmcore.setup.UsbSetupProviderContract

class AlarmActivity : AppCompatActivity() {

    private lateinit var alarmController: AlarmController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        printWifiNetworkStatus() test
        alarmController = initSystemController()
    }

    private fun initSystemController() = AlarmController(
            BeamBreakDetectorPeriphery(),
            MotionSensorPeriphery(),
            CameraPeripheryContract.create(this),
            AlarmBackendContract.create(this),
            UsbSetupProviderContract.create(this)
    )

    private fun printWifiNetworkStatus() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        if (wifi.isConnected) {
            Log.d(javaClass.simpleName, wifi.toString())
        }
    }

    override fun onDestroy() {
        alarmController.onAppDestroy()
        super.onDestroy()
    }
}
