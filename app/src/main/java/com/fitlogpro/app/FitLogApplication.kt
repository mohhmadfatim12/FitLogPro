package com.fitlogpro.app

import android.app.Application
import com.fitlogpro.app.di.AppContainer
import com.fitlogpro.app.di.DefaultAppContainer

class FitLogApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
