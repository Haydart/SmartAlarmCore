package pl.rmakowiecki.smartalarmcore.remote.models

class CameraSequenceSettingsModel(
        val sessionPhotoCount: Int,
        val photoSequenceIntervalMillis: Int) {

    companion object {
        fun createDefault() = CameraSequenceSettingsModel(20, 250)
    }
}