import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.subscribeBy
import pl.rmakowiecki.smartalarmcore.AlarmArmingState
import pl.rmakowiecki.smartalarmcore.extensions.applyIoSchedulers
import pl.rmakowiecki.smartalarmcore.peripheral.beam.BeamBreakDetectorPeripheryContract
import pl.rmakowiecki.smartalarmcore.remote.AlarmInteractorContract

class AlarmController(
        val beamBreakDetector: BeamBreakDetectorPeripheryContract,
        val interactor: AlarmInteractorContract
) {

    private var alarmArmingDisposable = Disposables.disposed()
    private var alarmTriggerDisposable = Disposables.disposed()

    fun observeAlarm() {
        alarmArmingDisposable = interactor
                .observeAlarmArmingState()
                .subscribeBy(
                        onNext = this::observeTriggerStateIfArmed,
                        onComplete = {},
                        onError = {}
                )
    }

    private fun observeTriggerStateIfArmed(armingState: AlarmArmingState) =
            if (armingState == AlarmArmingState.ARMED) {
                observeBeamBreakDetector()
            } else Unit

    fun observeBeamBreakDetector() = if (!alarmTriggerDisposable.isDisposed) {
        alarmTriggerDisposable = beamBreakDetector
                .registerForChanges()
                .applyIoSchedulers()
                .subscribe {
                    interactor.updateAlarmState(it)
                }
    } else Unit
}