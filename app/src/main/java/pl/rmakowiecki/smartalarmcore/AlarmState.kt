package pl.rmakowiecki.smartalarmcore

enum class AlarmTriggerState {
    TRIGGERED,
    IDLE;

    fun toBoolean() = when (this) {
        TRIGGERED -> true
        IDLE -> false
    }
}

enum class AlarmArmingState {
    ARMED,
    DISARMED;

    fun toBoolean() = when (this) {
        ARMED -> true
        DISARMED -> false
    }
}

fun Boolean.toArmingState() = when (this) {
    true -> AlarmArmingState.ARMED
    false -> AlarmArmingState.DISARMED
}

fun Boolean.toTriggerState() = when (this) {
    true -> AlarmTriggerState.TRIGGERED
    false -> AlarmTriggerState.IDLE
}