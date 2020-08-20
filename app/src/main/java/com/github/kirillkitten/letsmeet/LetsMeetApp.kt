package com.github.kirillkitten.letsmeet

import android.app.Application
import timber.log.Timber

class LetsMeetApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}