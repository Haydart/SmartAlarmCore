package pl.rmakowiecki.smartalarmcore.setup

import android.support.v7.app.AppCompatActivity

interface UsbSetupProviderContract {
    fun registerBroadcastListener(onAttach: () -> Unit, onDetach: () -> Unit)
    fun unregisterBroadcastListener()

    companion object {
        fun create(activity: AppCompatActivity) = UsbSetupProvider(activity)
    }
}