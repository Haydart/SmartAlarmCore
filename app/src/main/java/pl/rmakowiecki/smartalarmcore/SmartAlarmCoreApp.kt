package pl.rmakowiecki.smartalarmcore

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class SmartAlarmCoreApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(false)
    }
}