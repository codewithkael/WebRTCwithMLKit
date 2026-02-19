package com.codewithkael.webrtcwithmlkit.utils

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import java.util.UUID

@HiltAndroidApp
class MyApplication : Application() {
    companion object {
        val TAG: String = "MyApplication"
        var UserID: String = UUID.randomUUID().toString().substring(0, 2)
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}