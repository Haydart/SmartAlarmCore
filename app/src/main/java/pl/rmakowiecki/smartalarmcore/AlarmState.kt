package pl.rmakowiecki.smartalarmcore

enum class AlarmTrigger {
    TRIGGERED,
    IDLE;

    fun toBoolean() = when (this) {
        TRIGGERED -> true
        IDLE -> false
    }
}

enum class ArmingArming {
    ARMED,
    DISARMED;

    fun toBoolean() = when (this) {
        ARMED -> true
        DISARMED -> false
    }
}

fun Boolean.toArmingState() = when (this) {
    true -> ArmingArming.ARMED
    false -> ArmingArming.DISARMED
}

fun Boolean.toTriggerState() = when (this) {
    true -> AlarmTrigger.TRIGGERED
    false -> AlarmTrigger.IDLE
}