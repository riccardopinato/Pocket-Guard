package com.riccardopinato.pocketguard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riccardopinato.pocketguard.billing.BillingUiState

@Composable
fun PaywallScreen(
    contentPadding: PaddingValues,
    billing: BillingUiState,
    onBuy: () -> Unit,
    onRestore: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroCard(
            title = "PockSecure Pro",
            subtitle = "Più controllo durante viaggi, folla e situazioni a rischio.",
            badge = if (billing.isPro) "PRO" else "Lifetime",
            accent = listOf(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary
            )
        ) {}

        SectionCard(
            title = if (billing.isPro) "Pro attivo" else "Un solo acquisto",
            subtitle =
                if (billing.isPreview) {
                    "Durante la beta tutte le funzioni Pro sono sbloccate per i test."
                } else {
                    "Nessun abbonamento: paghi una volta e mantieni l'accesso."
                }
        ) {
            ProBenefit("Modalità Borsa")
            ProBenefit("Journey Guard avanzato")
            ProBenefit("Sensibilità Alta")
            ProBenefit("Biometria e flash")
            ProBenefit("Affidabilità avanzata")
            ProBenefit("Cronologia completa")
        }

        if (!billing.isPro) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = billing.isReady && billing.priceText != null,
                onClick = onBuy
            ) {
                Text(
                    billing.priceText?.let {
                        "Sblocca Pro Lifetime • $it"
                    } ?: "Caricamento prezzo…",
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            SectionCard(
                title = "Licenza attiva",
                subtitle =
                    if (billing.ownedByPlay) {
                        "Acquisto verificato tramite Google Play."
                    } else {
                        "Pro Preview attiva nella build di test pre-lancio."
                    }
            ) {}
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onRestore
        ) {
            Text("Ripristina acquisto")
        }

        billing.message?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            "Il core anti-pickpocket, la calibrazione, il PIN, la sirena e la vibrazione restano disponibili anche nella versione Free.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProBenefit(text: String) {
    Text(
        "✓ $text",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium
    )
}
