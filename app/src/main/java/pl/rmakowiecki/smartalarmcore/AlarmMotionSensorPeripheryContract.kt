package pl.rmakowiecki.homepiecore.peripherals.motion_sensor

import io.reactivex.Single

interface AlarmMotionSensorPeripheryContract {
    fun registerForChanges(): Single<Boolean>
    fun readValue(): Boolean

    companion object {
        fun create() = AlarmMotionSensorPeriphery()
    }
}