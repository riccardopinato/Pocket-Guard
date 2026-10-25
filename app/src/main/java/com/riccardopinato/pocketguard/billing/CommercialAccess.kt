package com.riccardopinato.pocketguard.billing

import android.content.Context
import com.riccardopinato.pocketguard.BuildConfig

object CommercialAccess {
    private const val PREFS_NAME = "pocksecure_entitlement"
    private const val KEY_PRO_OWNED = "pro_owned"

    fun hasPro(context: Context): Boolean =
        BuildConfig.PRELAUNCH_PRO_PREVIEW ||
            context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_PRO_OWNED, false)
}
