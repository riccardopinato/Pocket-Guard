package com.riccardopinato.pocketguard.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.riccardopinato.pocketguard.MainActivity
import com.riccardopinato.pocketguard.R
import com.riccardopinato.pocketguard.model.GuardState

class PocketGuardNotificationManager(
    private val context: Context
) {
    private val manager =
        context.getSystemService(NotificationManager::class.java)

    fun createChannels() {
        manager.createNotificationChannel(
            NotificationChannel(
                SERVICE_CHANNEL,
                "Protezione Pockara",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                description = "Mostra lo stato della protezione attiva."
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                ALARM_CHANNEL,
                "Allarme Pockara",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    "Avvisa quando viene rilevata una possibile estrazione."
            }
        )
    }

    fun buildService(
        state: GuardState,
        confidence: Int = 0
    ): Notification =
        NotificationCompat.Builder(context, SERVICE_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle("Pockara")
            .setContentText(stateText(state, confidence))
            .setContentIntent(openApp())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

    fun update(state: GuardState, confidence: Int = 0) {
        manager.notify(
            SERVICE_NOTIFICATION_ID,
            buildService(state, confidence)
        )
    }

    fun showAlarm() {
        manager.notify(
            ALARM_NOTIFICATION_ID,
            NotificationCompat.Builder(context, ALARM_CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_shield)
                .setContentTitle("Pockara: allarme")
                .setContentText(
                    "Estrazione sospetta confermata. Apri l'app per autenticarti."
                )
                .setContentIntent(openApp())
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build()
        )
    }

    fun cancelAlarm() {
        manager.cancel(ALARM_NOTIFICATION_ID)
    }

    private fun openApp(): PendingIntent =
        PendingIntent.getActivity(
            context,
            201,
            Intent(context, MainActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

    private fun stateText(state: GuardState, confidence: Int): String =
        when (state) {
            GuardState.DISARMED -> "Protezione disattivata"
            GuardState.ARMING -> "Attivazione in corso"
            GuardState.CALIBRATING -> "Calibrazione sensori in corso"
            GuardState.WAITING_FOR_POCKET -> "In attesa di tasca/borsa"
            GuardState.POCKET_CONFIRMED -> "Protezione attiva"
            GuardState.REMOVAL_SUSPECTED -> "Estrazione sospetta: $confidence%"
            GuardState.OWNER_CHECK -> "Verifica proprietario"
            GuardState.ALARM -> "ALLARME ATTIVO"
        }

    companion object {
        const val SERVICE_NOTIFICATION_ID = 2001
        const val ALARM_NOTIFICATION_ID = 2002
        private const val SERVICE_CHANNEL = "pocket_guard_service_v2"
        private const val ALARM_CHANNEL = "pocket_guard_alarm_v2"
    }
}
