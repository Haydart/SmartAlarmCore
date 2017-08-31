package pl.rmakowiecki.smartalarmcore

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log

class CameraPeriphery(private var context: Context?) : CameraPeripheryContract {
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private lateinit var cameraDevice: CameraDevice
    private lateinit var cameraId: String

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.e(javaClass.simpleName, "onCameraOpened")
            cameraDevice = camera
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(javaClass.simpleName, "onCameraDisconnected")
            closeCamera()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.d(javaClass.simpleName, "onCameraError")
            closeCamera()
        }
    }

    override fun openCamera() {
        val manager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.e(javaClass.simpleName, "opening camera")
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

    override fun captureFrame() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
