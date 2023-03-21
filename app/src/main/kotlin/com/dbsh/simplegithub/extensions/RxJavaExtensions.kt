package com.dbsh.simplegithub.extensions

import com.dbsh.simplegithub.extensions.rx.AutoClearedDisposable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

//operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
//    this.add(disposable)
//}

operator fun AutoClearedDisposable.plusAssign(disposable: Disposable) = this.add(disposable)

fun runOnIoScheduler(func: () -> Unit): Disposable = Completable
    .fromCallable(func)
    .subscribeOn(Schedulers.io())
    .subscribe()