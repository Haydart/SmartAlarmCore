package pl.rmakowiecki.smartalarmcore

import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.subscribeBy
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState.TRIGGERED
import pl.rmakowiecki.smartalarmcore.extensions.applyIoSchedulers
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.peripheral.beam.BeamBreakDetectorPeripheryContract
import pl.rmakowiecki.smartalarmcore.peripheral.camera.CameraPeripheryContract
import pl.rmakowiecki.smartalarmcore.remote.AlarmBackendContract
import pl.rmakowiecki.smartalarmcore.setup.UsbSetupProviderContract

class AlarmController(
        private val beamBreakDetector: BeamBreakDetectorPeripheryContract,
        private val camera: CameraPeripheryContract,
        private val backendInteractor: AlarmBackendContract,
        private val usbSetupProvider: UsbSetupProviderContract
) {

    private var alarmArmingDisposable = Disposables.disposed()
    private var alarmTriggerDisposable = Disposables.disposed()
    private var backendConnectionDisposable = Disposables.disposed()
    private var cameraPhotoSessionDisposable = Disposables.disposed()

    init {
        camera.openCamera()
        connectToBackend()
        usbSetupProvider.registerBroadcastListener({ }, { })
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
                            onNext = {
                                updateTriggerState(it)
                                if (it == TRIGGERED) {
                                    capturePhoto()
                                }
                            }
                    )
        }
    }

    private fun capturePhoto() {
        cameraPhotoSessionDisposable = camera.capturePhoto()
                .flatMapSingle(backendInteractor::uploadPhoto)
                .applyIoSchedulers()
                .subscribeBy(
                        onNext = { logD("Photo upload success? $it") }
                )
    }

    private fun updateTriggerState(alarmTriggerState: AlarmTriggerState)
            = backendInteractor.updateAlarmState(alarmTriggerState)

    fun onAppDestroy() {
        alarmArmingDisposable.dispose()
        alarmTriggerDisposable.dispose()
        backendConnectionDisposable.dispose()
        cameraPhotoSessionDisposable.dispose()
        usbSetupProvider.unregisterBroadcastListener()
    }
}