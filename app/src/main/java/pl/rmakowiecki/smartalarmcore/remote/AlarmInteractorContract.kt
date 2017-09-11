package pl.rmakowiecki.smartalarmcore.remote

import io.reactivex.Observable
import pl.rmakowiecki.smartalarmcore.AlarmTrigger
import pl.rmakowiecki.smartalarmcore.ArmingArming

interface AlarmInteractorContract {
    fun observeAlarmArmingState(): Observable<ArmingArming>
    fun updateAlarmState(alarmState: AlarmTrigger)

    companion object {
        fun create() = AlarmInteractor()
    }
}

