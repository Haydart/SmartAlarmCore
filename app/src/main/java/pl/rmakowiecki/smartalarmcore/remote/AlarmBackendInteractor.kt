package pl.rmakowiecki.smartalarmcore.remote

import android.provider.Settings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import pl.rmakowiecki.smartalarmcore.AlarmActivity
import pl.rmakowiecki.smartalarmcore.AlarmArmingState
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.extensions.printStackTrace
import pl.rmakowiecki.smartalarmcore.remote.Nodes.ALARM_ARMING
import pl.rmakowiecki.smartalarmcore.remote.Nodes.ALARM_STATE
import pl.rmakowiecki.smartalarmcore.remote.Nodes.ALARM_TRIGGER
import pl.rmakowiecki.smartalarmcore.remote.Nodes.CORE_DEVICE_DIRECTORY
import pl.rmakowiecki.smartalarmcore.remote.Nodes.IMAGES_DIRECTORY
import pl.rmakowiecki.smartalarmcore.toArmingState

class AlarmBackendInteractor(private val activity: AlarmActivity) : AlarmBackendContract {

    private val databaseNode = FirebaseDatabase
            .getInstance()
            .reference

    private val storageNode = FirebaseStorage
            .getInstance()
            .getReferenceFromUrl("gs://smartalarmcore.appspot.com")

    override fun signInToBackend(): Single<Boolean> = Single.create { emitter ->

        logD(Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID))

        getCurrentBackendUser()?.let {
            emitter.onSuccess(true)
        } ?: FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnSuccessListener { signUpAsAuthorizedUser(it.user, emitter) }
    }

    private fun signUpAsAuthorizedUser(firebaseUser: FirebaseUser, emitter: SingleEmitter<Boolean>) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                "${firebaseUser.uid}@smarthome.com",
                Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
        ).addOnSuccessListener {
            initializeCoreDeviceNoSqlModel(emitter)
        }.addOnFailureListener(::printStackTrace)
    }

    private fun initializeCoreDeviceNoSqlModel(emitter: SingleEmitter<Boolean>) = databaseNode
            .child(getCurrentBackendUser()?.uid)
            .child(ALARM_STATE)
            .setValue(RemoteAlarmStateModel(true, false))
            .addOnSuccessListener { emitter.onSuccess(true) }

    override fun isLoggedInToBackend(): Single<Boolean> =
            Single.just(getCurrentBackendUser() != null)

    private fun getCurrentBackendUser() = FirebaseAuth.getInstance().currentUser

    override fun observeAlarmArmingState(): Observable<AlarmArmingState> = Observable.create { emitter ->
        val valueListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) = emitter.onNext(dataSnapshot.getArmingState())

            override fun onCancelled(databaseError: DatabaseError?) = emitter.onComplete()
        }

        val alarmArmingNode = databaseNode
                .child(getCurrentBackendUser()?.uid)
                .child(ALARM_STATE)
                .child(ALARM_ARMING)

        alarmArmingNode.addValueEventListener(valueListener)

        emitter.setCancellable {
            alarmArmingNode.removeEventListener(valueListener)
        }
    }

    override fun updateAlarmState(alarmState: AlarmTriggerState) {
        databaseNode.child(getCurrentBackendUser()?.uid)
                .child(ALARM_STATE)
                .child(ALARM_TRIGGER)
                .setValue(alarmState.toBoolean())
                .addOnCompleteListener { }
    }

    override fun reportSecurityIncident(securityIncident: SecurityIncident): Single<Boolean> = Single.create { emitter ->
        databaseNode.child(getCurrentBackendUser()?.uid)
                .child(Nodes.INCIDENTS)
                .push()
                .setValue(RemoteSecurityIncident.from(securityIncident))
                .addOnCompleteListener { emitter.onSuccess(it.isSuccessful) }
    }

    override fun uploadIncidentPhoto(photoBytes: ByteArray, reportTimestamp: Long): Single<Boolean> = Single.create { emitter ->
        storageNode.child(CORE_DEVICE_DIRECTORY)
                .child(IMAGES_DIRECTORY)
                .child(getCurrentBackendUser()?.uid ?: "non_assignable_incidents")
                .child("$reportTimestamp.jpg")
                .putBytes(photoBytes)
                .addOnCompleteListener { emitter.onSuccess(it.isSuccessful) }
    }
}

private fun DataSnapshot.getArmingState() = (this.value as Boolean).toArmingState()

class RemoteAlarmStateModel(val active: Boolean, val triggered: Boolean)