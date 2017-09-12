package pl.rmakowiecki.smartalarmcore.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import pl.rmakowiecki.smartalarmcore.extensions.logD

private const val USB_STATE_CHANGE_ACTION = "android.hardware.usb.action.USB_STATE"
private const val USB_DEVICE_ATTACHED_ACTION = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
private const val USB_DEVICE_DETACHED_ACTION = "android.hardware.usb.action.USB_DEVICE_DETACHED"

class UsbStateBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            USB_STATE_CHANGE_ACTION -> logD("USB state changed")
            USB_DEVICE_ATTACHED_ACTION -> logD("USB device attached")
            USB_DEVICE_DETACHED_ACTION -> logD("USB device detached")
        }
    }
}