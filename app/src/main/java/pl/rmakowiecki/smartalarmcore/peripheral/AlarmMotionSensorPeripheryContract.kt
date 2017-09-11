
import io.reactivex.Single

interface AlarmMotionSensorPeripheryContract {
    fun registerForChanges(): Single<Boolean>
    fun readValue(): Boolean

    companion object {
        fun create() = AlarmMotionSensorPeriphery()
    }
}