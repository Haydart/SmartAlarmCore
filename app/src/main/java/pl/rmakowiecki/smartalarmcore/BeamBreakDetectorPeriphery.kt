import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import io.reactivex.Single
import pl.rmakowiecki.homepiecore.peripherals.beam_break_detector.BeamBreakDetectorPeripheryContract

class BeamBreakDetectorPeriphery : BeamBreakDetectorPeripheryContract {

    private lateinit var alarmGpio: Gpio

    private val gpioStateListener = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio): Boolean {
            System.out.println("Alarm GPIO state: ${gpio.value}")
            return true
        }

        override fun onGpioError(gpio: Gpio?, error: Int) {
            Log.d(javaClass.simpleName, "GPIO error occurred")
            super.onGpioError(gpio, error)
        }
    }

    override fun registerForChanges(): Single<Boolean> {
        initAndRegisterGpioCallback()
        return Single.just(false)
    }

    private fun initAndRegisterGpioCallback() {
        val service = PeripheralManagerService()
        Log.d(javaClass.simpleName, "Complete GPIO list: " + service.gpioList)
        try {
            val pinName = "BCM26"
            alarmGpio = service.openGpio(pinName)
            alarmGpio.setDirection(Gpio.DIRECTION_IN)
            alarmGpio.setActiveType(Gpio.ACTIVE_LOW)
            alarmGpio.setEdgeTriggerType(Gpio.EDGE_BOTH)
            alarmGpio.registerGpioCallback(gpioStateListener)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.d(javaClass.simpleName, "Some exception")
        }
    }

    override fun readValue(): Boolean = alarmGpio.value

    override fun unregisterFromChanges() {
        alarmGpio.unregisterGpioCallback(gpioStateListener)
        alarmGpio.close()
    }
}