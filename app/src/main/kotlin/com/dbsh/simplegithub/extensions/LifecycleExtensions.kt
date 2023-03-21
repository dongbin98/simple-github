package com.dbsh.simplegithub.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

operator fun Lifecycle.plusAssign(observer: LifecycleEventObserver) = this.addObserver(observer)