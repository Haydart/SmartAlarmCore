package pl.rmakowiecki.smartalarmcore.remote

class SecurityIncident(
        val reason: AlarmTriggerReason,
        val timestamp: Long
)