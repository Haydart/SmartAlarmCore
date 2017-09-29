package pl.rmakowiecki.smartalarmcore.remote

import android.provider.Settings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import pl.rmakowiecki.smartalarmcore.AlarmActivity
import pl.rmakowiecki.smartalarmcore.AlarmArmingState
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.extensions.printStackTrace
import pl.rmakowiecki.smartalarmcore.toArmingState

class AlarmBackendInteractor(val activity: AlarmActivity) : AlarmBackendContract {

    private val databaseNode = FirebaseDatabase
            .getInstance()
            .reference

    override fun signInToBackend(): Single<Boolean> = Single.create { emitter ->

        FirebaseAuth.getInstance().signOut()
        logD(Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID))

        getCurrentBackendUser()?.let {
            emitter.onSuccess(true)
        } ?: FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnSuccessListener { signUpAsAuthorizedUser(it.user, emitter) }
    }

    private fun signUpAsAuthorizedUser(firebaseUser: FirebaseUser, emitter: SingleEmitter<Boolean>) {
        logD(firebaseUser.uid, "UID USERA")
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                "${firebaseUser.uid}@smarthome.com",
                Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
        ).addOnSuccessListener {
            initializeCoreDeviceNoSqlModel(emitter)
        }.addOnFailureListener { printStackTrace(it) }
    }

    private fun initializeCoreDeviceNoSqlModel(emitter: SingleEmitter<Boolean>) = databaseNode
            .child(getCurrentBackendUser()?.uid)
            .setValue(RemoteAlarmStateModel(true, false))
            .addOnSuccessListener { emitter.onSuccess(true) }

    override fun isLoggedInToBackend(): Single<Boolean> =
            Single.just(getCurrentBackendUser() != null)

    private fun getCurrentBackendUser(): FirebaseUser? {
        logD(FirebaseAuth.getInstance().currentUser, "GET UID")
        return FirebaseAuth.getInstance().currentUser
    }

    override fun observeAlarmArmingState(): Observable<AlarmArmingState> = Observable.create { emitter ->
        val valueListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) = emitter.onNext(dataSnapshot.getArmingState())

            override fun onCancelled(databaseError: DatabaseError?) = emitter.onComplete()
        }

        val alarmArmingNode = databaseNode
                .child(getCurrentBackendUser()?.uid)
                .child(Nodes.ALARM_ARMING)

        alarmArmingNode.addValueEventListener(valueListener)

        emitter.setCancellable {
            alarmArmingNode.removeEventListener(valueListener)
        }
    }

    override fun updateAlarmState(alarmState: AlarmTriggerState) {
        databaseNode.child(getCurrentBackendUser()?.uid)
                .child(Nodes.ALARM_TRIGGER)
                .setValue(alarmState.toBoolean())
                .addOnCompleteListener { }
    }
}

private fun DataSnapshot.getArmingState() = (this.value as Boolean).toArmingState()

class RemoteAlarmStateModel(val active: Boolean, val triggered: Boolean)