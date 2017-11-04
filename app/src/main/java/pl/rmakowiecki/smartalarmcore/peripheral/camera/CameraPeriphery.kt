package pl.rmakowiecki.smartalarmcore.peripheral.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.extensions.logE
import pl.rmakowiecki.smartalarmcore.extensions.logW
import pl.rmakowiecki.smartalarmcore.extensions.printStackTrace
import java.io.ByteArrayOutputStream
import java.util.*

private const val PHOTO_WIDTH = 1920
private const val PHOTO_HEIGHT = 1080
private const val PHOTO_MAX_COUNT = 10
private const val PHOHOS_IN_SEQUENCE = 10
private const val PHOTO_SEQUENCE_INTERVAL = 250

class CameraPeriphery(private var context: Context?) : CameraPeripheryContract {

    private var backgroundHandler: Handler? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var imageReadProcessor: ImageReader? = null
    private var cameraId: String = ""
    private var photosEmittedInSequence = 0

    private val photoPublishSubject: PublishSubject<Pair<ByteArray, Int>> = PublishSubject.create()

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

            session?.close()
            logD("CaptureSession closed")
        }
    }

    private fun triggerImageCapture() {
        try {
            val captureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG)
            captureBuilder?.addTarget(imageReadProcessor?.surface)
            captureBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)

            logD("Session initialized.")

            val cameraRequests = mutableListOf<CaptureRequest>()
            (1..10).map {
                val request = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                request?.addTarget(imageReadProcessor?.surface)
                request?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                request?.build()

            }.forEach { cameraRequests.add(it!!) }

//            cameraCaptureSession?.captureBurst(cameraRequests, cameraCaptureCallback, null) CANNOT BE INTERLEAVED
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
            manager.openCamera(cameraId, cameraStateCallback, null)
        } catch (ex: CameraAccessException) {
            ex.printStackTrace()
        }
    }

    override fun capturePhoto(): Observable<Pair<ByteArray, Int>> {
        takePicture()
        photosEmittedInSequence = 0
        return photoPublishSubject
    }

    private fun takePicture() {
        if (cameraDevice == null) {
            logE("Cannot capture image. Camera not initialized.")
            return
        }

        try {
            imageReadProcessor = ImageReader.newInstance(PHOTO_WIDTH, PHOTO_HEIGHT, ImageFormat.JPEG, PHOTO_MAX_COUNT)
            imageReadProcessor?.setOnImageAvailableListener(
                    { reader -> processTakenImage(reader) },
                    backgroundHandler)
            cameraDevice?.createCaptureSession(Collections.singletonList(imageReadProcessor?.surface), cameraSessionCallback, null)
        } catch (cameraAccessException: CameraAccessException) {
            printStackTrace(cameraAccessException)
        }

    }

    private fun processTakenImage(reader: ImageReader) {
        val image = reader.acquireLatestImage()
        val imageByteBuffer = image.planes[0].buffer
        val imageBytes = ByteArray(imageByteBuffer.remaining())
        imageByteBuffer.get(imageBytes)
        image.close()

        onPictureTaken(imageBytes)
    }

    private fun onPictureTaken(imageBytes: ByteArray?) {
        if (imageBytes != null) {
            photoPublishSubject.onNext(
                    Pair(compressTakenImage(imageBytes), ++photosEmittedInSequence)
            )
        }
    }

    private fun compressTakenImage(imageBytes: ByteArray): ByteArray {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        return outputStream.toByteArray()
    }

    override fun closeCamera() {
        cameraDevice?.close()
    }
}
