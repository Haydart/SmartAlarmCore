package pl.rmakowiecki.smartalarmcore.peripheral.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Handler
import android.os.HandlerThread
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.extensions.logE
import pl.rmakowiecki.smartalarmcore.extensions.logW
import pl.rmakowiecki.smartalarmcore.extensions.printStackTrace
import java.util.*

class CameraPeriphery(private var context: Context?) : CameraPeripheryContract {

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var imageReadProcessor: ImageReader? = null
    private var cameraId: String = ""

    private val photoPublishSubject: PublishSubject<ByteArray> = PublishSubject.create()

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            logD(javaClass.simpleName, "onCameraOpened")
            cameraDevice = camera
        }

        override fun onDisconnected(camera: CameraDevice) {
            logW("onCameraDisconnected")
            closeCamera()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            logE("onCameraError")
            closeCamera()
        }
    }

    private var cameraSessionCallback = object : CameraCaptureSession.StateCallback() {

        override fun onConfigured(captureSession: CameraCaptureSession) {
            if (cameraDevice == null) {
                logE("The camera is already closed")
                return
            }

            cameraCaptureSession = captureSession
            triggerImageCapture()
        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) = logW("Failed to configure camera")
    }

    private val cameraCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            super.onCaptureCompleted(session, request, result)
            if (session != null) {
                session.close()
                cameraCaptureSession = null
                logD("CaptureSession closed")
            }
        }
    }

    private val imageAvailabilityListener = OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage()
        val imageByteBuffer = image.planes[0].buffer
        val imageBytes = ByteArray(imageByteBuffer.remaining())
        imageByteBuffer.get(imageBytes)
        image.close()

        onPictureTaken(imageBytes)
    }

    private fun onPictureTaken(imageBytes: ByteArray?) {
        if (imageBytes != null) {
            photoPublishSubject.onNext(imageBytes)
        }
    }

    private fun triggerImageCapture() {
        try {
            imageReadProcessor = ImageReader.newInstance(1280, 768,
                    ImageFormat.JPEG, 10)
            imageReadProcessor?.setOnImageAvailableListener(imageAvailabilityListener, backgroundHandler)

            val captureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder?.addTarget(imageReadProcessor?.surface)
            captureBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            logD("Session initialized.")
            cameraCaptureSession?.capture(captureBuilder?.build(), cameraCaptureCallback, null)
        } catch (cameraAccessException: CameraAccessException) {
            printStackTrace(cameraAccessException)
        }

    }

    override fun openCamera() {
        val manager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        logD("opening camera")
        try {
            cameraId = manager.cameraIdList[0]
            val cameraCharacteristics = manager.getCameraCharacteristics(cameraId)
            val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            manager.openCamera(cameraId, cameraStateCallback, null)
        } catch (ex: CameraAccessException) {
            ex.printStackTrace()
        }
    }

    override fun capturePhoto(): Observable<ByteArray> {
        takePicture()
        return photoPublishSubject
    }

    private fun takePicture() {
        if (cameraDevice == null) {
            logE("Cannot capture image. Camera not initialized.")
            return
        }

        try {
            cameraDevice?.createCaptureSession(Collections.singletonList(imageReadProcessor?.surface), cameraSessionCallback, null)
        } catch (cameraAccessException: CameraAccessException) {
            printStackTrace(cameraAccessException)
        }

    }

    override fun closeCamera() {
        cameraDevice?.close()
    }
}
