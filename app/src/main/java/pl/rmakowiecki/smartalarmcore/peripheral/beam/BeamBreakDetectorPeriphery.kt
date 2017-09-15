package pl.rmakowiecki.smartalarmcore.peripheral.beam

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.extensions.logE
import java.util.concurrent.TimeUnit

private const val PIN_NAME = "BCM19"

class BeamBreakDetectorPeriphery : BeamBreakDetectorPeripheryContract {

    private lateinit var alarmGpio: Gpio
    private val statePublisher: PublishSubject<Int> by lazy {
        PublishSubject.create<Int>()
    }
    private var emittedItemsCount = 0

    private val gpioStateListener = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio): Boolean {
            logD(gpio.value, "BEFORE EMITTING ${++emittedItemsCount}")
            statePublisher.onNext(emittedItemsCount)
            return true
        }

        override fun onGpioError(gpio: Gpio?, error: Int) {
            statePublisher.onError(Throwable())
            super.onGpioError(gpio, error)
        }
    }

    override fun registerForChanges(): Observable<Int> {
        initAndRegisterGpioCallback()
        logD("REGISTERED TO BEAM DETECTOR")

        val throttledPublisher = statePublisher.throttleFirst(3, TimeUnit.SECONDS)

        return Observable.merge(
                throttledPublisher,
                Observable.interval(1500, TimeUnit.MILLISECONDS).map { -1 }
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