package com.example.rollingicon

import android.app.Application
import io.sentry.android.core.SentryAndroid

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Sentry
        SentryAndroid.init(this) { options ->
            options.dsn = "https://your-sentry-dsn-url"
            // You can configure other options here as needed
        }
    }
}