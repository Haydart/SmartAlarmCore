package pl.rmakowiecki.smartalarmcore.remote

class RemoteSecurityIncident private constructor(
        val reason: AlarmTriggerReason,
        val timestamp: Long,
        val thumbnailUrl: String = "",
        val archived: Boolean = false) {

    companion object {
        fun from(securityIncident: SecurityIncident) = RemoteSecurityIncident(securityIncident.reason, securityIncident.timestamp)
    }
}