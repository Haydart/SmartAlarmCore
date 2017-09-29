package pl.rmakowiecki.smartalarmcore

import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.subscribeBy
import pl.rmakowiecki.smartalarmcore.extensions.applyIoSchedulers
import pl.rmakowiecki.smartalarmcore.peripheral.beam.BeamBreakDetectorPeripheryContract
import pl.rmakowiecki.smartalarmcore.remote.AlarmBackendContract
import pl.rmakowiecki.smartalarmcore.setup.UsbSetupProviderContract

class AlarmController(
        val beamBreakDetector: BeamBreakDetectorPeripheryContract,
        val backendInteractor: AlarmBackendContract,
        val usbSetupProviderContract: UsbSetupProviderContract
) {

    private var alarmArmingDisposable = Disposables.disposed()
    private var alarmTriggerDisposable = Disposables.disposed()
    private var backendConnectionDisposable = Disposables.disposed()

    init {
        connectToBackend()
        usbSetupProviderContract.registerBroadcastListener({ }, { })
    }

    private fun connectToBackend() {
        backendConnectionDisposable = backendInteractor
                .signInToBackend()
                .applyIoSchedulers()
                .subscribeBy(
                        onSuccess = { observeAlarm() }
                )
    }

    private fun observeAlarm() {
        alarmArmingDisposable = backendInteractor
                .observeAlarmArmingState()
                .applyIoSchedulers()
                .subscribeBy(
                        onNext = this::observeTriggerStateIfArmed
                )
    }

    private fun observeTriggerStateIfArmed(armingState: AlarmArmingState) = when (armingState) {
        AlarmArmingState.ARMED -> observeBeamBreakDetector()
        else -> alarmTriggerDisposable.dispose()
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
            = backendInteractor.updateAlarmState(alarmTriggerState)

    fun onAppDestroy() {
        alarmArmingDisposable.dispose()
        alarmTriggerDisposable.dispose()
        backendConnectionDisposable.dispose()
        usbSetupProviderContract.unregisterBroadcastListener()
    }
}