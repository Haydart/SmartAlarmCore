package pl.rmakowiecki.smartalarmcore

import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.subscribeBy
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState.TRIGGERED
import pl.rmakowiecki.smartalarmcore.extensions.applyIoSchedulers
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.peripheral.AlarmTriggerPeripheralDevice
import pl.rmakowiecki.smartalarmcore.peripheral.camera.CameraPeripheryContract
import pl.rmakowiecki.smartalarmcore.remote.AlarmBackendContract
import pl.rmakowiecki.smartalarmcore.remote.AlarmTriggerReason
import pl.rmakowiecki.smartalarmcore.remote.SecurityIncident
import pl.rmakowiecki.smartalarmcore.setup.UsbSetupProviderContract

class AlarmController(
        private val beamBreakDetector: AlarmTriggerPeripheralDevice,
        private val motionSensor: AlarmTriggerPeripheralDevice,
        private val camera: CameraPeripheryContract,
        private val backendInteractor: AlarmBackendContract,
        private val usbSetupProvider: UsbSetupProviderContract
) {

    private var beamBreakDetectorDisposable = Disposables.disposed()
    private var motionSensorDisposable = Disposables.disposed()
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
        alarmTriggerDisposable = backendInteractor
                .observeAlarmArmingState()
                .applyIoSchedulers()
                .subscribeBy(
                        onNext = this::observeTriggerStateIfArmed
                )
    }

    private fun observeTriggerStateIfArmed(armingState: AlarmArmingState) = when (armingState) {
        AlarmArmingState.ARMED -> {
            observeBeamBreakDetector()
            observeMotionSensor()
        }
        else -> {
            beamBreakDetectorDisposable.dispose()
            motionSensorDisposable.dispose()
        }
    }

    private fun observeBeamBreakDetector() {
        if (beamBreakDetectorDisposable.isDisposed) {
            beamBreakDetectorDisposable = beamBreakDetector
                    .registerForChanges()
                    .applyIoSchedulers()
                    .subscribeBy(
                            onNext = {
                                updateAlarmTriggerState(it)
                                if (it == TRIGGERED) {
                                    reportBeamBreakIncident()
                                }
                            }
                    )
        }
    }

    private fun observeMotionSensor() {
        if (motionSensorDisposable.isDisposed) {
            motionSensorDisposable = motionSensor
                    .registerForChanges()
                    .applyIoSchedulers()
                    .subscribeBy(
                            onNext = { /*logD(it)*/ }
                    )
        }
    }

    private fun updateAlarmTriggerState(alarmTriggerState: AlarmTriggerState)
            = backendInteractor.updateAlarmState(alarmTriggerState)

    private fun reportBeamBreakIncident() {
        val reportTimestamp = System.currentTimeMillis()

        backendInteractor
                .reportSecurityIncident(SecurityIncident(AlarmTriggerReason.BEAM_BREAK_DETECTOR, reportTimestamp))
                .flatMapObservable { incidentModel ->
                    camera.capturePhoto()
                            .flatMapSingle { photo ->
                                backendInteractor.uploadIncidentPhoto(photo, incidentModel.generatedId)
                            }
                }.applyIoSchedulers()
                .subscribeBy(
                        onNext = { logD("Security incident report successful? $it") }
                )
    }

    fun onAppDestroy() {
        beamBreakDetectorDisposable.dispose()
        motionSensorDisposable.dispose()
        alarmTriggerDisposable.dispose()
        backendConnectionDisposable.dispose()
        cameraPhotoSessionDisposable.dispose()
        usbSetupProvider.unregisterBroadcastListener()
    }
}