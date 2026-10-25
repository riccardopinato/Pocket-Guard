package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyScreen(
    contentPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroCard(
            title = "Privacy",
            subtitle = "Pockara è progettata per funzionare principalmente sul dispositivo.",
            badge = "Local"
        ) {}

        PrivacySection(
            "Dati e sensori",
            "La protezione usa sensori di prossimità, luce e movimento. I valori vengono elaborati sul dispositivo e non vengono caricati su server Pockara."
        )
        PrivacySection(
            "PIN e cronologia",
            "PIN, impostazioni, calibrazione e cronologia restano memorizzati localmente sul telefono. Il PIN non viene salvato in chiaro."
        )
        PrivacySection(
            "Fotocamera",
            "Il permesso fotocamera viene usato solo per controllare il flash se abiliti questa funzione. Pockara non scatta né salva foto o video."
        )
        PrivacySection(
            "Acquisti",
            "Gli acquisti Pro sono gestiti da Google Play. Pockara riceve solo lo stato necessario a sbloccare le funzioni acquistate e non riceve i dati completi della carta."
        )
        PrivacySection(
            "Pubblicità e tracking",
            "Pockara non include pubblicità di terze parti e non usa un advertising ID per profilazione pubblicitaria."
        )
        PrivacySection(
            "Backup",
            "Le normali impostazioni possono essere incluse nel backup Android. PIN, stato runtime e dati sensibili del disarmo non vengono inclusi nel backup configurato dall'app."
        )
        PrivacySection(
            "Limiti del servizio",
            "Pockara aiuta a rilevare estrazioni sospette, ma nessuna app può garantire la prevenzione di un furto o il recupero del dispositivo."
        )
    }
}

@Composable
private fun PrivacySection(
    title: String,
    text: String
) {
    SectionCard(title = title) {
        Text(
            text,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
