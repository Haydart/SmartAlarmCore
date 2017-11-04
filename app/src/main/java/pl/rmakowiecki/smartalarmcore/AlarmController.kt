package pl.rmakowiecki.smartalarmcore

import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.subscribeBy
import pl.rmakowiecki.smartalarmcore.AlarmTriggerState.TRIGGERED
import pl.rmakowiecki.smartalarmcore.extensions.applyIoSchedulers
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.peripheral.AlarmTriggerPeripheralDevice
import pl.rmakowiecki.smartalarmcore.peripheral.camera.CameraPeripheryContract
import pl.rmakowiecki.smartalarmcore.remote.AlarmBackendContract
import pl.rmakowiecki.smartalarmcore.remote.models.AlarmTriggerReason
import pl.rmakowiecki.smartalarmcore.remote.models.SecurityIncident
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
        AlarmArmingState.DISARMED -> {
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
                                    reportAlarmIncident(AlarmTriggerReason.BEAM_BREAK_DETECTOR)
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
                            onNext = {
                                updateAlarmTriggerState(it)
                                if (it == TRIGGERED) {
                                    reportAlarmIncident(AlarmTriggerReason.MOTION_SENSOR)
                                }
                            }
                    )
        }
    }

    private fun updateAlarmTriggerState(alarmTriggerState: AlarmTriggerState) = backendInteractor
            .updateAlarmState(alarmTriggerState)
            .applyIoSchedulers()
            .subscribeBy(
                    onSuccess = { logD("Updating trigger value on server successful") }
            )

    private fun reportAlarmIncident(reason: AlarmTriggerReason) {
        val reportTimestamp = System.currentTimeMillis()

        val cameraPhotoObservable = camera.capturePhoto()
                .applyIoSchedulers()

        cameraPhotoObservable.subscribeBy(onNext = { logD("camera photo taken") })

        backendInteractor
                .reportSecurityIncident(SecurityIncident(reason, reportTimestamp))
                .flatMapObservable { incidentModel ->
                    cameraPhotoObservable
                            .flatMapSingle { (photo, photoNumber) ->
                                backendInteractor.uploadIncidentPhoto(photo, incidentModel.generatedId, photoNumber)
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