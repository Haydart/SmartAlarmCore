package pl.rmakowiecki.smartalarmcore.peripheral.beam

import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import io.reactivex.subjects.PublishSubject
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState

private const val PIN_NAME = "BCM19"

class BeamBreakDetectorPeriphery : BeamBreakDetectorPeripheryContract {

    private lateinit var alarmGpio: Gpio
    private val statePublisher: PublishSubject<AlarmTriggerState> by lazy {
        PublishSubject.create<AlarmTriggerState>().apply {
            distinctUntilChanged()
        }
    }

    private val gpioStateListener = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio): Boolean {
            statePublisher.onNext(if (gpio.value) AlarmTriggerState.TRIGGERED else AlarmTriggerState.IDLE)
            return true
        }

        override fun onGpioError(gpio: Gpio?, error: Int) {
            statePublisher.onError(Throwable())
            super.onGpioError(gpio, error)
        }
    }

    override fun registerForChanges() = statePublisher.apply {
        initAndRegisterGpioCallback()
    }

    private fun initAndRegisterGpioCallback() {
        val service = PeripheralManagerService()
        Log.d(javaClass.simpleName, "Complete GPIO list: " + service.gpioList)
        try {
            alarmGpio = service.openGpio(PIN_NAME)
            alarmGpio.apply {
                setDirection(Gpio.DIRECTION_IN)
                setActiveType(Gpio.ACTIVE_LOW)
                setEdgeTriggerType(Gpio.EDGE_BOTH)
                registerGpioCallback(gpioStateListener)
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.d(javaClass.simpleName, "Some exception")
        }
    }

    override fun readValue(): AlarmTriggerState = if (alarmGpio.value) AlarmTriggerState.TRIGGERED else AlarmTriggerState.IDLE

    override fun unregisterFromChanges() {
        statePublisher.onComplete()
        alarmGpio.unregisterGpioCallback(gpioStateListener)
        alarmGpio.close()
    }
}