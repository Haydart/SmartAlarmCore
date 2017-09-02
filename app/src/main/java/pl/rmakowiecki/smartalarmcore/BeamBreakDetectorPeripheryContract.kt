package pl.rmakowiecki.homepiecore.peripherals.beam_break_detector

import io.reactivex.Single

interface BeamBreakDetectorPeripheryContract {
    fun registerForChanges(): Single<Boolean>
    fun unregisterFromChanges()
    fun readValue(): Boolean

    companion object {
        fun create() = BeamBreakDetectorPeriphery()
    }
}