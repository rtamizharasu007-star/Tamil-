package com.example

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navigation.JarvisNavigation
import com.example.ui.MainViewModel
import com.example.ui.theme.ArcBlueDark
import com.example.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {

    companion object {
        var activeInstance: MainActivity? = null

        fun unlockDeviceKeyguard() {
            activeInstance?.unlockKeyguard()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeInstance = this
        
        setupLockScreenFlags()
        enableEdgeToEdge()

        setContent {
            JarvisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ArcBlueDark
                ) {
                    val mainViewModel: MainViewModel = viewModel()
                    JarvisNavigation(viewModel = mainViewModel)
                }
            }
        }
    }

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }

    fun unlockKeyguard() {
        runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissSucceeded() {
                        super.onDismissSucceeded()
                    }
                })
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeInstance == this) {
            activeInstance = null
        }
    }
}
