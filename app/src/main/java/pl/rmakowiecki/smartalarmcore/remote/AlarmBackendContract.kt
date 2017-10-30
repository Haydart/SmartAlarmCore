package pl.rmakowiecki.smartalarmcore.remote

import io.reactivex.Observable
import io.reactivex.Single
import pl.rmakowiecki.smartalarmcore.AlarmActivity
import pl.rmakowiecki.smartalarmcore.AlarmArmingState
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState

interface AlarmBackendContract {
    fun isLoggedInToBackend(): Single<Boolean>
    fun signInToBackend(): Single<Boolean>
    fun observeAlarmArmingState(): Observable<AlarmArmingState>
    fun updateAlarmState(alarmState: AlarmTriggerState)
    fun reportSecurityIncident(securityIncident: SecurityIncident): Single<Boolean>
    fun uploadIncidentPhoto(photoBytes: ByteArray, reportTimestamp: Long): Single<Boolean>

    companion object {
        fun create(activity: AlarmActivity) = AlarmBackendInteractor(activity)
    }

}