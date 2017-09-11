package pl.rmakowiecki.smartalarmcore.extensions

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign

infix fun CompositeDisposable.handle(disposable: Disposable) {
    this += disposable
}