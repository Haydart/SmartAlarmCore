package pl.rmakowiecki.smartalarmcore.peripheral.camera

import android.content.Context
import io.reactivex.Observable

interface CameraPeripheryContract {
    fun openCamera()
    fun closeCamera()
    fun capturePhoto(): Observable<Pair<ByteArray, Int>>

    companion object {
        fun create(context: Context) = CameraPeriphery(context)
    }
}