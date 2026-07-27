package com.example.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build

class DeviceInfoProvider(private val context: Context) {

    fun getBatteryPercentage(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getDeviceInfo(): String {
        val battery = getBatteryPercentage()
        val onlineStatus = if (isNetworkAvailable()) "Connected (Online)" else "Disconnected (Offline)"
        return "Sir, device details: ${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}, running Android version ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}). Battery level: $battery%. Network status: $onlineStatus."
    }
}
