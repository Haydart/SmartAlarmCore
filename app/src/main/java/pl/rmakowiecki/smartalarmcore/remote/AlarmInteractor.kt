package pl.rmakowiecki.smartalarmcore.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Observable
import pl.rmakowiecki.smartalarmcore.AlarmTrigger
import pl.rmakowiecki.smartalarmcore.ArmingState
import pl.rmakowiecki.smartalarmcore.extensions.toArmingState

class AlarmInteractor : AlarmInteractorContract {

    private val databaseNode = FirebaseDatabase
            .getInstance()
            .reference
            .child("active")

    override fun observeAlarmArmingState(): Observable<ArmingState> = Observable.create { emitter ->
        val valueListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) = emitter.onNext(dataSnapshot.getArmingState())

            override fun onCancelled(databaseError: DatabaseError?) = emitter.onComplete()
        }

        databaseNode.addValueEventListener(valueListener)

        emitter.setCancellable { databaseNode.removeEventListener(valueListener) }
    }

    override fun updateAlarmState(alarmState: AlarmTrigger) {
        databaseNode.setValue(alarmState.value())
                .addOnCompleteListener { }
    }
}

private fun DataSnapshot.getArmingState() = (this.value as Boolean).toArmingState()