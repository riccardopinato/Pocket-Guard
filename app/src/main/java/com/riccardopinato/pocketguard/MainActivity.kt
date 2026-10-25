package com.riccardopinato.pocketguard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riccardopinato.pocketguard.security.BiometricDisarmHelper
import com.riccardopinato.pocketguard.ui.PocketGuardApp
import com.riccardopinato.pocketguard.ui.PocketGuardViewModel
import com.riccardopinato.pocketguard.ui.theme.PocketGuardTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as PocketGuardApplication

        setContent {
            PocketGuardTheme {
                val vm: PocketGuardViewModel =
                    viewModel(
                        factory = PocketGuardViewModel.Factory(
                            appContext = applicationContext,
                            settingsRepository = app.settingsRepository,
                            billingRepository = app.billingRepository,
                            historyRepository = app.historyRepository,
                            securityRepository = app.securityRepository,
                            pinManager = app.pinSecurityManager
                        )
                    )

                Root(viewModel = vm)
            }
        }
    }

    @Composable
    private fun Root(
        viewModel: PocketGuardViewModel
    ) {
        val biometric = remember {
            BiometricDisarmHelper(this)
        }

        val notificationLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                viewModel.startGuard()
            }

        val cameraLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                viewModel.setFlash(granted)
            }

        PocketGuardApp(
            viewModel = viewModel,
            biometricHelper = biometric,
            onStartRequested = {
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                } else {
                    viewModel.startGuard()
                }
            },
            onBuyProRequested = {
                viewModel.purchasePro(this)
            },
            onFlashEnableRequested = {
                if (
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    viewModel.setFlash(true)
                } else {
                    cameraLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        )
    }
}
