package com.example.softtest.app

import android.app.Application
import android.content.ContextWrapper
import com.dsofttech.dprefs.utils.DPrefs
import com.pixplicity.easyprefs.library.Prefs

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DPrefs.initializeDPrefs(this)
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
//        if (BuildConfig.DEBUG) {
//            Timber.plant(Timber.DebugTree())
//        }
    }
}