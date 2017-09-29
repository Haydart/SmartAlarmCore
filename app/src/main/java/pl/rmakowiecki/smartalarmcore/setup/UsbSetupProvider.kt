package pl.rmakowiecki.smartalarmcore.setup

import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import pl.rmakowiecki.smartalarmcore.background.UsbStateBroadcastReceiver

class UsbSetupProvider(private val activity: AppCompatActivity) : UsbSetupProviderContract {

    private val usbReceiver = UsbStateBroadcastReceiver()

    override fun registerBroadcastListener() {
        activity.registerReceiver(usbReceiver, IntentFilter().apply {
            addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED")
            addAction("android.hardware.usb.action.USB_DEVICE_DETACHED")
        })
    }

    override fun unregisterBroadcastListener() = activity.unregisterReceiver(usbReceiver)
}