package pl.rmakowiecki.smartalarmcore.peripheral.camera

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import pl.rmakowiecki.smartalarmcore.extensions.logD
import pl.rmakowiecki.smartalarmcore.extensions.logE
import pl.rmakowiecki.smartalarmcore.extensions.logW

class CameraPeriphery(private var context: Context?) : CameraPeripheryContract {
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private lateinit var cameraDevice: CameraDevice
    private lateinit var cameraId: String

    private val stateCallback = object : CameraDevice.StateCallback() {
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

    override fun openCamera() {
        val manager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        logD("opening camera")
        try {
            cameraId = manager.cameraIdList[0]
            val cameraCharacteristics = manager.getCameraCharacteristics(cameraId)
            val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            manager.openCamera(cameraId, stateCallback, null)
        } catch (ex: CameraAccessException) {
            ex.printStackTrace()
        }
    }

    override fun closeCamera() {
        cameraDevice.close()
        context = null
    }

    override fun captureFrame() = Unit
}
