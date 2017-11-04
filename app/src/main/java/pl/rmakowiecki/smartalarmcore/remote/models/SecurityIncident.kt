package pl.rmakowiecki.smartalarmcore.remote.models

class SecurityIncident(
        val reason: AlarmTriggerReason,
        val timestamp: Long
)