package pl.rmakowiecki.smartalarmcore.peripheral.motion

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.extensions.logE
import pl.rmakowiecki.smartalarmcore.peripheral.AlarmTriggerPeripheralDevice
import pl.rmakowiecki.smartalarmcore.toTriggerState

private const val PIN_NAME = "BCM20"

class MotionSensorPeriphery : AlarmTriggerPeripheralDevice {

    private lateinit var alarmGpio: Gpio
    private val statePublisher: PublishSubject<AlarmTriggerState> by lazy {
        PublishSubject.create<AlarmTriggerState>()
    }

    private val gpioStateListener = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio): Boolean {
            logD("Motion sensor triggered? ${gpio.value}")
            statePublisher.onNext(gpio.value.toTriggerState())
            return true
        }

        override fun onGpioError(gpio: Gpio?, error: Int) {
            statePublisher.onError(Throwable())
            super.onGpioError(gpio, error)
        }
    }

    override fun registerForChanges(): Observable<AlarmTriggerState> {
        initAndRegisterGpioCallback()
        logD("REGISTERED TO MOTION SENSOR")

        return statePublisher
    }

    private fun initAndRegisterGpioCallback() {
        val service = PeripheralManagerService()
        logD("Complete GPIO list: " + service.gpioList)
        try {
            alarmGpio = service.openGpio(PIN_NAME)
            alarmGpio.apply {
                setDirection(Gpio.DIRECTION_IN)
                setActiveType(Gpio.ACTIVE_HIGH)
                setEdgeTriggerType(Gpio.EDGE_BOTH)
                registerGpioCallback(gpioStateListener)
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            logE("GPIO exception")
        }
    }

    override fun readValue(): AlarmTriggerState = if (alarmGpio.value) AlarmTriggerState.TRIGGERED else AlarmTriggerState.IDLE

    override fun unregisterFromChanges() {
        statePublisher.onComplete()
        alarmGpio.unregisterGpioCallback(gpioStateListener)
        alarmGpio.close()
    }
}