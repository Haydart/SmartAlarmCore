import io.reactivex.Observable
import pl.rmakowiecki.smartalarmcore.AlarmState

interface BeamBreakDetectorPeripheryContract {
    fun registerForChanges(): Observable<AlarmState>
    fun unregisterFromChanges()
    fun readValue(): AlarmState

    companion object {
        fun create() = BeamBreakDetectorPeriphery()
    }
}