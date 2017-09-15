package pl.rmakowiecki.smartalarmcore.peripheral.beam

import io.reactivex.Observable
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState

interface BeamBreakDetectorPeripheryContract {
    fun registerForChanges(): Observable<Int>
    fun unregisterFromChanges()
    fun readValue(): AlarmTriggerState

    companion object {
        fun create() = BeamBreakDetectorPeriphery()
    }
}