package com.example.offline

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build

class FlashlightManager(private val context: Context) {

    private var isTorchOn = false

    fun toggleFlashlight(turnOn: Boolean): String {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        if (cameraManager == null) {
            return "Sir, flashlight control is not supported on this device."
        }

        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull()
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, turnOn)
                isTorchOn = turnOn
                if (turnOn) "Sir, flashlight is now turned ON." else "Sir, flashlight is now turned OFF."
            } else {
                "Sir, camera hardware for flashlight was not found."
            }
        } catch (e: Exception) {
            "Sir, unable to access flashlight: ${e.message}"
        }
    }

    fun isFlashlightOn(): Boolean = isTorchOn
}
