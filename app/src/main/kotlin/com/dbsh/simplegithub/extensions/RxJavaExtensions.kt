package com.dbsh.simplegithub.extensions

import com.dbsh.simplegithub.extensions.rx.AutoClearDisposable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

//operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
//    this.add(disposable)
//}

operator fun AutoClearDisposable.plusAssign(disposable: Disposable) = this.add(disposable)
