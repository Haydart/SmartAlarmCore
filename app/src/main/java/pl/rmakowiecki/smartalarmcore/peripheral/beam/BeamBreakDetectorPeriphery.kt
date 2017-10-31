package pl.rmakowiecki.smartalarmcore.peripheral.beam

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
import java.util.concurrent.TimeUnit

private const val PIN_NAME = "BCM19"
private const val THROTTLED_PERIPHERY_INTERVAL_MILLIS = 5000L
private const val ALARM_TURN_OFF_SIGNAL_INTERVAL_MILLIS = 2500L

class BeamBreakDetectorPeriphery : AlarmTriggerPeripheralDevice {

    private lateinit var alarmGpio: Gpio
    private val statePublisher: PublishSubject<AlarmTriggerState> by lazy {
        PublishSubject.create<AlarmTriggerState>()
    }

    private val gpioStateListener = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio): Boolean {
            statePublisher.onNext(gpio.value.toTriggerState())
//            logD("GPIO 19 state changed to: ${gpio.value}")
            return true
        }

        override fun onGpioError(gpio: Gpio?, error: Int) {
            statePublisher.onError(Throwable())
            super.onGpioError(gpio, error)
        }
    }

    override fun registerForChanges(): Observable<AlarmTriggerState> {
        initAndRegisterGpioCallback()
        logD("REGISTERED TO BEAM DETECTOR")

        return Observable.merge(
                statePublisher.throttleFirst(THROTTLED_PERIPHERY_INTERVAL_MILLIS, TimeUnit.MILLISECONDS),
                Observable.interval(ALARM_TURN_OFF_SIGNAL_INTERVAL_MILLIS, TimeUnit.MILLISECONDS).map { AlarmTriggerState.IDLE }
        ).distinctUntilChanged()
    }

    private fun initAndRegisterGpioCallback() {
        val service = PeripheralManagerService()
        logD("Complete GPIO list: " + service.gpioList)
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