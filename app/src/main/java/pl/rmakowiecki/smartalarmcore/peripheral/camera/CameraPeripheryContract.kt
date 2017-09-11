package pl.rmakowiecki.smartalarmcore.peripheral.camera

import android.content.Context

interface CameraPeripheryContract {
    fun openCamera()
    fun closeCamera()
    fun captureFrame()

    companion object {
        fun create(context: Context) = CameraPeriphery(context)
    }
}