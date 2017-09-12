package pl.rmakowiecki.smartalarmcore.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class UsbBroadcastReceiver : BroadcastReceiver() {
    var usbStateChangeAction = "android.hardware.usb.action.USB_STATE"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(this::class.java.simpleName, "Received Broadcast: " + action)
        if (action.equals(usbStateChangeAction, ignoreCase = true)) { //Check if change in USB state
            if (intent.extras.getBoolean("connected")) {
                // USB was connected
            } else {
                // USB was disconnected
            }
        }
    }
}