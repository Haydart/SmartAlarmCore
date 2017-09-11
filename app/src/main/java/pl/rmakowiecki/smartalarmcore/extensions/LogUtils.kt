package pl.rmakowiecki.smartalarmcore.extensions

import android.util.Log
import pl.rmakowiecki.smartalarmcore.BuildConfig

fun <T : Any> T.logD(): T = apply { logD(this) }
fun <T : Any> T.logW(): T = apply { logW(this) }
fun <T : Any> T.logE(): T = apply { logE(this) }

fun logD(any: Any?, tag: String? = null) = log(any, tag, Log.DEBUG)
fun logW(any: Any?, tag: String? = null) = log(any, tag, Log.WARN)
fun logE(any: Any?, tag: String? = null) = log(any, tag, Log.ERROR)

val <T : Any> T.simpleClassName: String get() = this::class.java.simpleName

fun printStackTrace(throwable: Throwable?) {
    if (BuildConfig.DEBUG) {
        throwable?.printStackTrace()
    }
}

fun log(any: Any?, tag: String?, priority: Int) {
    if (BuildConfig.DEBUG) {
        Log.println(priority, tag ?: "TAG", any.toString())
    }
}