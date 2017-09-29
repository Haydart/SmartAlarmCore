package pl.rmakowiecki.smartalarmcore.setup

import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import pl.rmakowiecki.smartalarmcore.ActivityWrapper
import pl.rmakowiecki.smartalarmcore.background.UsbStateBroadcastReceiver

class UsbSetupProvider : UsbSetupProviderContract {

    private var activity: AppCompatActivity? = null
    private val usbReceiver = UsbStateBroadcastReceiver()

    fun attachBroadcastSource(activityWrapper: ActivityWrapper) {
        this.activity = activityWrapper.activity
    }

    override fun registerBroadcastListener() {
        activity?.registerReceiver(usbReceiver, IntentFilter().apply {
            addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED")
            addAction("android.hardware.usb.action.USB_DEVICE_DETACHED")
        }) ?: throw IllegalAccessException("Tried to register broadcast receiver but no activity has been attached")
    }

    override fun unregisterBroadcastListener() {
        activity?.unregisterReceiver(usbReceiver)
    }
}