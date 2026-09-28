package com.example

import android.app.Application
import com.example.core.di.AppContainer

class CleankrApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
