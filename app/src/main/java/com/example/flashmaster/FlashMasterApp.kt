package com.example.flashmaster

import android.app.Application
import com.example.flashmaster.Setting.NotificationHelper
import com.example.flashmaster.Setting.OkHttpHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class FlashMasterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FlashMasterApp)
            modules(appModule)
        }
    }
}

val appModule = module {
    single { NotificationHelper(get()) }
    single { OkHttpHelper }
} 