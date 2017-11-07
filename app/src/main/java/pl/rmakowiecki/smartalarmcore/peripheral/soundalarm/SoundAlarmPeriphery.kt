package pl.rmakowiecki.smartalarmcore.peripheral.soundalarm

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import pl.rmakowiecki.smartalarmcore.extensions.logE

private const val PIN_NAME = "BCM21"

class SoundAlarmPeriphery {

    private lateinit var alarmSirenGpio: Gpio

    init {
        initAndRegisterGpioCallback()
    }

    private fun initAndRegisterGpioCallback() {
        val service = PeripheralManagerService()
        try {
            alarmSirenGpio = service.openGpio(PIN_NAME)
            alarmSirenGpio.apply {
                setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            logE("GPIO exception")
        }
    }

    fun startSiren() {
        alarmSirenGpio.value = true
    }

    fun stopSiren() {
        alarmSirenGpio.value = false
    }
}
