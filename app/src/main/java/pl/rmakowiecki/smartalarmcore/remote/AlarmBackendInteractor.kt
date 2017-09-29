package pl.rmakowiecki.smartalarmcore.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Observable
import io.reactivex.Single
import pl.rmakowiecki.smartalarmcore.AlarmArmingState
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.toArmingState

class AlarmBackendInteractor : AlarmBackendContract {

    private val databaseNode = FirebaseDatabase
            .getInstance()
            .reference

    override fun signInToBackend(): Single<Boolean> = Single.create { emitter ->
        getCurrentBackendUser()?.let {
            emitter.onSuccess(true)
        } ?: FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnCompleteListener { emitter.onSuccess(it.isSuccessful) }
    }

    override fun isLoggedInToBackend(): Single<Boolean> =
            Single.just(getCurrentBackendUser() != null)

    private fun getCurrentBackendUser() = FirebaseAuth.getInstance().currentUser

    override fun observeAlarmArmingState(): Observable<AlarmArmingState> = Observable.create { emitter ->
        val valueListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) = emitter.onNext(dataSnapshot.getArmingState())

            override fun onCancelled(databaseError: DatabaseError?) = emitter.onComplete()
        }

        databaseNode.child(Nodes.ALARM_ARMING).addValueEventListener(valueListener)

        emitter.setCancellable {
            databaseNode.child(Nodes.ALARM_ARMING).removeEventListener(valueListener)
        }
    }

    override fun updateAlarmState(alarmState: AlarmTriggerState) {
        logD("Updating trigger value on server")
        databaseNode.child(Nodes.ALARM_TRIGGER)
                .setValue(alarmState.toBoolean())
                .addOnCompleteListener { }
    }
}

private fun DataSnapshot.getArmingState() = (this.value as Boolean).toArmingState()