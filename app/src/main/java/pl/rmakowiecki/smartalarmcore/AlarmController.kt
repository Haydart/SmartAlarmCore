package pl.rmakowiecki.smartalarmcore

import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.subscribeBy
import pl.rmakowiecki.smartalarmcore.extensions.applyIoSchedulers
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.peripheral.beam.BeamBreakDetectorPeripheryContract
import pl.rmakowiecki.smartalarmcore.remote.AlarmInteractorContract

class AlarmController(
        val beamBreakDetector: BeamBreakDetectorPeripheryContract,
        val interactor: AlarmInteractorContract) {

    private var alarmArmingDisposable = Disposables.disposed()
    private var alarmTriggerDisposable = Disposables.disposed()

    fun observeAlarm() {
        alarmArmingDisposable = interactor
                .observeAlarmArmingState()
                .applyIoSchedulers()
                .subscribeBy(
                        onNext = this::observeTriggerStateIfArmed
                )
    }

    private fun observeTriggerStateIfArmed(armingState: AlarmArmingState) {
        logD("New alarm arming state $armingState")
        if (armingState == AlarmArmingState.ARMED) {
            observeBeamBreakDetector()
        } else alarmTriggerDisposable.dispose()
    }

    private fun observeBeamBreakDetector() {
        if (alarmTriggerDisposable.isDisposed) {
            alarmTriggerDisposable = beamBreakDetector
                    .registerForChanges()
                    .applyIoSchedulers()
                    .subscribeBy(
                            onNext = this::updateTriggerState
                    )
        }
    }

    private fun updateTriggerState(alarmTriggerState: AlarmTriggerState)
            = interactor.updateAlarmState(alarmTriggerState)
}