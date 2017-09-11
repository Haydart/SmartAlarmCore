package pl.rmakowiecki.smartalarmcore

enum class AlarmTrigger {
    TRIGGERED,
    IDLE;

    fun value() = when (this) {
        TRIGGERED -> true
        IDLE -> false
    }
}