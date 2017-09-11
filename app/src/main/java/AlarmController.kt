import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.disposables.CompositeDisposable
import pl.rmakowiecki.smartalarmcore.extensions.handle
import pl.rmakowiecki.smartalarmcore.peripheral.BeamBreakDetectorPeripheryContract

class AlarmController(val beamBreakDetector: BeamBreakDetectorPeripheryContract) {

    private val disposables = CompositeDisposable()

    fun observeBeamBreakDetector() =
            disposables handle beamBreakDetector
                    .registerForChanges()
                    .subscribe {
                        Log.d(this.javaClass.simpleName, "$it")
                    }


    private fun sendAlarmTriggerData(gpio: Gpio) {
        FirebaseDatabase
                .getInstance()
                .reference
                .child("trigger")
                .setValue(gpio.value)
                .addOnCompleteListener {
                    Log.d(javaClass.simpleName, if (it.isSuccessful) "Server trigger data push successful" else "Error sending data to server")
                }
    }
}