package com.riccardopinato.pocketguard.alarm

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.core.content.ContextCompat

class FlashController(
    private val context: Context
) {
    private val cameraManager =
        context.getSystemService(CameraManager::class.java)

    private val cameraId: String? by lazy {
        runCatching {
            cameraManager.cameraIdList.firstOrNull { id ->
                val c = cameraManager.getCameraCharacteristics(id)
                val flash =
                    c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing =
                    c.get(CameraCharacteristics.LENS_FACING)

                flash &&
                    facing == CameraCharacteristics.LENS_FACING_BACK
            } ?: cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()
    }

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    fun setEnabled(enabled: Boolean) {
        if (!hasPermission()) return
        val id = cameraId ?: return

        runCatching {
            cameraManager.setTorchMode(id, enabled)
        }
    }

    fun forceOff() {
        setEnabled(false)
    }
}
