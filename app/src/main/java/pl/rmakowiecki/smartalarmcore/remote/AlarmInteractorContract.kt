package pl.rmakowiecki.smartalarmcore.remote

import io.reactivex.Observable
import pl.rmakowiecki.smartalarmcore.AlarmArmingState
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState

interface AlarmInteractorContract {
    fun observeAlarmArmingState(): Observable<AlarmArmingState>
    fun updateAlarmState(alarmState: AlarmTriggerState)

    companion object {
        fun create() = AlarmInteractor()
    }
}

