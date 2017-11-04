package pl.rmakowiecki.smartalarmcore.remote.models

class RemoteAlarmStateModel(
        val active: Boolean,
        val triggered: Boolean,
        val connected: Boolean) {

    companion object {
        fun createDefault() = RemoteAlarmStateModel(true, false, true)
    }
}