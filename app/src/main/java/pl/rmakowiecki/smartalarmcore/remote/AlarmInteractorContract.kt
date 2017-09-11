package pl.rmakowiecki.smartalarmcore.remote

import io.reactivex.Observable
import pl.rmakowiecki.smartalarmcore.AlarmTrigger
import pl.rmakowiecki.smartalarmcore.ArmingState

interface AlarmInteractorContract {
    fun observeAlarmArmingState(): Observable<ArmingState>
    fun updateAlarmState(alarmState: AlarmTrigger)
}

