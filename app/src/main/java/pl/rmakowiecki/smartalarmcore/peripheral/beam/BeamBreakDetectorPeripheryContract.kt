package pl.rmakowiecki.smartalarmcore.peripheral.beam

import io.reactivex.Observable
import pl.rmakowiecki.smartalarmcore.AlarmTrigger

interface BeamBreakDetectorPeripheryContract {
    fun registerForChanges(): Observable<AlarmTrigger>
    fun unregisterFromChanges()
    fun readValue(): AlarmTrigger

    companion object {
        fun create() = BeamBreakDetectorPeriphery()
    }
}