package pl.rmakowiecki.smartalarmcore.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Observable
import pl.rmakowiecki.smartalarmcore.AlarmArmingState
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.toArmingState

class AlarmInteractor : AlarmInteractorContract {

    private val databaseNode = FirebaseDatabase
            .getInstance()
            .reference

    override fun observeAlarmArmingState(): Observable<AlarmArmingState> = Observable.create { emitter ->
        val valueListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) = emitter.onNext(dataSnapshot.getArmingState())

            override fun onCancelled(databaseError: DatabaseError?) = emitter.onComplete()
        }

        databaseNode.child("active")
                .addValueEventListener(valueListener)

        emitter.setCancellable { databaseNode.child("active").removeEventListener(valueListener) }
    }

    override fun updateAlarmState(alarmState: AlarmTriggerState) {
        logD("Updating trigger value on server")
        databaseNode.child("trigger")
                .setValue(alarmState.toBoolean())
                .addOnCompleteListener { }
    }
}

private fun DataSnapshot.getArmingState() = (this.value as Boolean).toArmingState()