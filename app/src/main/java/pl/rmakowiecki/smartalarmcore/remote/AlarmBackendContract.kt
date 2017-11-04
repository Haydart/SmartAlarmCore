package pl.rmakowiecki.smartalarmcore.remote

import io.reactivex.Observable
import io.reactivex.Single
import pl.rmakowiecki.smartalarmcore.AlarmActivity
import pl.rmakowiecki.smartalarmcore.AlarmArmingState
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState
import pl.rmakowiecki.smartalarmcore.remote.models.SecurityIncident
import pl.rmakowiecki.smartalarmcore.remote.models.SecurityIncidentResponse

interface AlarmBackendContract {
    fun isLoggedInToBackend(): Single<Boolean>
    fun signInToBackend(): Single<Boolean>
    fun observeAlarmArmingState(): Observable<AlarmArmingState>
    fun updateAlarmState(alarmState: AlarmTriggerState): Single<Boolean>
    fun reportSecurityIncident(securityIncident: SecurityIncident): Single<SecurityIncidentResponse>
    fun uploadIncidentPhoto(photoBytes: ByteArray, uniqueIncidentId: String, photoNumber: Int): Single<Boolean>

    companion object {
        fun create(activity: AlarmActivity) = AlarmBackendInteractor(activity)
    }

}