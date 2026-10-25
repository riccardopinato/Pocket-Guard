package com.riccardopinato.pocketguard

import android.app.Application
import com.riccardopinato.pocketguard.data.SettingsRepository
import com.riccardopinato.pocketguard.history.HistoryRepository
import com.riccardopinato.pocketguard.security.PinSecurityManager
import com.riccardopinato.pocketguard.security.SecurityRepository

class PocketGuardApplication : Application() {
    val settingsRepository by lazy {
        SettingsRepository(applicationContext)
    }

    val historyRepository by lazy {
        HistoryRepository(applicationContext)
    }

    val securityRepository by lazy {
        SecurityRepository(applicationContext)
    }

    val pinSecurityManager by lazy {
        PinSecurityManager(securityRepository)
    }
}
