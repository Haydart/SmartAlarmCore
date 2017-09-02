
import io.reactivex.Single

interface BeamBreakDetectorPeripheryContract {
    fun registerForChanges(): Single<Boolean>
    fun unregisterFromChanges()
    fun readValue(): Boolean

    companion object {
        fun create() = BeamBreakDetectorPeriphery()
    }
}