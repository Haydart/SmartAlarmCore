package pl.rmakowiecki.smartalarmcore.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import pl.rmakowiecki.smartalarmcore.extensions.logD

private const val USB_STATE_CHANGE_ACTION = "android.hardware.usb.action.USB_STATE"
private const val USB_DEVICE_ATTACHED_ACTION = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
private const val USB_DEVICE_DETACHED_ACTION = "android.hardware.usb.action.USB_DEVICE_DETACHED"

class UsbStateBroadcastReceiver(val onAttach: () -> Unit, val onDetach: () -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) = when (intent.action) {
            USB_STATE_CHANGE_ACTION -> logD("USB state changed")
        USB_DEVICE_ATTACHED_ACTION -> onAttach()
        USB_DEVICE_DETACHED_ACTION -> onDetach()
        else -> Unit
    }

    private fun getDeviceData(context: Context) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        val deviceList = usbManager.deviceList
        val deviceIterator = deviceList.values.iterator()
        while (deviceIterator.hasNext()) {
            val device = deviceIterator.next()

            val model = device.deviceName
            val id = device.deviceId
            val vendor = device.vendorId
            val product = device.productId
            val productClass = device.deviceClass
            val productSubclass = device.deviceSubclass
        }
    }
}