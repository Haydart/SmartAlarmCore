package pl.rmakowiecki.smartalarmcore.peripheral

import io.reactivex.Observable
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState

interface AlarmTriggerPeripheralDevice {
    fun registerForChanges(): Observable<AlarmTriggerState>
    fun readValue(): AlarmTriggerState
    fun unregisterFromChanges()
}