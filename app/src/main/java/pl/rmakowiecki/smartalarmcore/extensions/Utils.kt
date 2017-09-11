package pl.rmakowiecki.smartalarmcore.extensions

import pl.rmakowiecki.smartalarmcore.AlarmTrigger
import pl.rmakowiecki.smartalarmcore.ArmingState

fun Boolean.toArmingState() = when (this) {
    true -> ArmingState.ARMED
    false -> ArmingState.DISARMED
}

fun Boolean.toTriggerState() = when (this) {
    true -> AlarmTrigger.TRIGGERED
    false -> AlarmTrigger.IDLE
}