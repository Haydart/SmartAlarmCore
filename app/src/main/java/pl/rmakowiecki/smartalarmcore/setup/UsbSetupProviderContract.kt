package pl.rmakowiecki.smartalarmcore.setup

interface UsbSetupProviderContract {
    fun registerBroadcastListener()
    fun unregisterBroadcastListener()

    companion object {
        fun create() = UsbSetupProvider()
    }
}