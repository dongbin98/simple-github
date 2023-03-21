package com.dbsh.simplegithub.extensions.rx

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.rxjava3.disposables.Disposable

class AutoActivatedDisposable(
    private val lifecycleOwner: LifecycleOwner,
    private val func: () -> Disposable,
): LifecycleEventObserver {
    private var disposable: Disposable? = null

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event) {
            Lifecycle.Event.ON_START -> {
              disposable = func.invoke()
            }
            Lifecycle.Event.ON_STOP -> {
                disposable?.dispose()
            }
            Lifecycle.Event.ON_DESTROY -> {
                lifecycleOwner.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }
}