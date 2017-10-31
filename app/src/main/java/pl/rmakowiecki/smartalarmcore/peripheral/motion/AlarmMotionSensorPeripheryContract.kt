import io.reactivex.Observable
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState

interface AlarmMotionSensorPeripheryContract {
    fun registerForChanges(): Observable<AlarmTriggerState>
    fun readValue(): AlarmTriggerState

    companion object {
        fun create() = AlarmMotionSensorPeriphery()
    }

    fun unregisterFromChanges()
}