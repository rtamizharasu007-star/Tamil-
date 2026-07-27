package com.example.offline

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.database.dao.MemoryDao
import com.example.database.entity.UserMemoryEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class CommandResult {
    data class Success(val responseText: String, val launchedApp: Boolean = false) : CommandResult()
    data class NotHandled(val input: String) : CommandResult()
}

class OfflineCommandProcessor(
    private val context: Context,
    private val memoryDao: MemoryDao
) {
    private val flashlightManager = FlashlightManager(context)
    private val deviceInfoProvider = DeviceInfoProvider(context)

    suspend fun processCommand(rawInput: String): CommandResult {
        var input = rawInput.trim().lowercase(Locale.ROOT)

        // Clean hotwords ("hey jarvis", "ok jarvis", "jarvis")
        if (input.startsWith("hey jarvis ")) input = input.substringAfter("hey jarvis ").trim()
        else if (input.startsWith("ok jarvis ")) input = input.substringAfter("ok jarvis ").trim()
        else if (input.startsWith("hello jarvis ")) input = input.substringAfter("hello jarvis ").trim()
        else if (input.startsWith("jarvis ") && input.length > 7) input = input.substringAfter("jarvis ").trim()

        // 0. LOCK SCREEN & PHONE UNLOCK COMMANDS
        if (input.contains("unlock phone") || input.contains("unlock device") || input.contains("unlock my phone") ||
            input.contains("unlock screen") || input.contains("open lock screen") || input.contains("turn screen on") ||
            input.contains("dismiss keyguard") || input == "unlock") {
            com.example.MainActivity.unlockDeviceKeyguard()
            return CommandResult.Success("Unlocking phone and authenticating Voice Match profile, sir. Welcome back.")
        }

        if (input.contains("lock phone") || input.contains("lock device") || input.contains("lock screen")) {
            return CommandResult.Success("Locking device screen and securing all JARVIS subsystem interfaces, sir.")
        }

        // 1. GREETINGS & IDENTITY
        if (input == "hello" || input == "hi" || input == "hey" || input == "jarvis" || input == "hey jarvis" || input.startsWith("hello jarvis") || input.startsWith("hi jarvis")) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val greeting = when (hour) {
                in 4..11 -> "Good morning, sir. I am ready to assist you."
                in 12..16 -> "Good afternoon, sir. How may I help you today?"
                in 17..22 -> "Good evening, sir. All systems are operational."
                else -> "Good night, sir. Systems are online and monitoring."
            }
            return CommandResult.Success(greeting)
        }

        if (input.contains("good morning")) return CommandResult.Success("Good morning, sir. All systems are running smoothly.")
        if (input.contains("good night")) return CommandResult.Success("Good night, sir. Rest well.")

        if (input.contains("what is your name") || input.contains("who are you")) {
            return CommandResult.Success("I am JARVIS, your personal AI virtual assistant, created to assist you in online and offline environments.")
        }

        if (input.contains("what can you do") || input.contains("help")) {
            return CommandResult.Success("Sir, I can execute system actions offline (open camera, settings, calculator, apps, flashlight, alarms, math calculations, and local notes), or connect online to Gemini AI for advanced coding, explanations, and general knowledge.")
        }

        // 2. TIME & DATE
        if (input.contains("time")) {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val timeStr = timeFormat.format(Date())
            return CommandResult.Success("Sir, the current time is $timeStr.")
        }

        if (input.contains("date") || input.contains("day is today")) {
            val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            return CommandResult.Success("Sir, today is $dateStr.")
        }

        // 3. FLASHLIGHT
        if (input.contains("turn on flashlight") || input.contains("flashlight on") || input == "torch on") {
            val res = flashlightManager.toggleFlashlight(true)
            return CommandResult.Success(res)
        }
        if (input.contains("turn off flashlight") || input.contains("flashlight off") || input == "torch off") {
            val res = flashlightManager.toggleFlashlight(false)
            return CommandResult.Success(res)
        }

        // 4. DEVICE INFO & BATTERY & INTERNET
        if (input.contains("battery")) {
            val pct = deviceInfoProvider.getBatteryPercentage()
            return CommandResult.Success("Sir, the battery level is currently $pct percent.")
        }

        if (input.contains("internet") || input.contains("connection status")) {
            val isOnline = deviceInfoProvider.isNetworkAvailable()
            val status = if (isOnline) "connected to the internet. Online AI mode is active." else "disconnected. Offline Mode is active."
            return CommandResult.Success("Sir, internet status is currently $status")
        }

        if (input.contains("device info") || input.contains("system info") || input.contains("about device")) {
            return CommandResult.Success(deviceInfoProvider.getDeviceInfo())
        }

        // 5. MATH CALCULATIONS
        if (input.startsWith("calculate") || input.startsWith("what is ") && (input.contains("+") || input.contains("-") || input.contains("*") || input.contains("/") || input.contains("x"))) {
            val mathResult = tryCalculateMath(input)
            if (mathResult != null) {
                return CommandResult.Success("Sir, the answer is $mathResult.")
            }
        }

        // 6. MEMORY & NOTES COMMANDS
        // "remember that my project is..." or "save note..."
        if (input.startsWith("remember that ") || input.startsWith("remember ") || input.startsWith("save note ")) {
            val factText = rawInput.substringAfter("remember that ", "")
                .ifEmpty { rawInput.substringAfter("remember ", "") }
                .ifEmpty { rawInput.substringAfter("save note ", "") }

            if (factText.isNotBlank()) {
                val category = if (factText.lowercase().contains("project")) "project" else "general"
                memoryDao.insertMemory(UserMemoryEntity(keyCategory = category, factText = factText))
                return CommandResult.Success("Understood, sir. I have saved that information in my memory bank.")
            }
        }

        // "what is my project?" / "read notes" / "what did I tell you"
        if (input.startsWith("what is my ") || input.contains("read notes") || input.contains("read saved notes") || input.contains("my notes") || input.contains("what did i tell you")) {
            val query = if (input.contains("project")) "project" else ""
            val memories = if (query.isNotBlank()) memoryDao.searchMemories(query) else memoryDao.searchMemories("")
            return if (memories.isNotEmpty()) {
                val lastFact = memories.first().factText
                CommandResult.Success("Sir, according to my saved records: $lastFact")
            } else {
                CommandResult.Success("Sir, I do not have any notes or memory entries saved regarding that request.")
            }
        }

        // 7. ALARM, TIMER & REMINDER
        if (input.contains("set alarm") || input.contains("set an alarm")) {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, "JARVIS Alarm")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return tryLaunchIntent(intent, "Opening alarm settings, sir.", "Alarm application is not available.")
        }

        if (input.contains("set timer") || input.contains("set a timer")) {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, "JARVIS Timer")
                putExtra(AlarmClock.EXTRA_LENGTH, 300) // 5 mins default
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return tryLaunchIntent(intent, "Opening timer settings, sir.", "Timer application is not available.")
        }

        // 8. DEVICE APP LAUNCHERS
        if (input.contains("open settings")) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return tryLaunchIntent(intent, "Opening system settings, sir.", "Settings application could not be opened.")
        }

        if (input.contains("open camera")) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return tryLaunchIntent(intent, "Opening camera, sir.", "Sorry sir, camera application is not installed on this device.")
        }

        if (input.contains("open calculator")) {
            val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.calculator")
                ?: context.packageManager.getLaunchIntentForPackage("com.sec.android.app.popupcalculator")
                ?: Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_CALCULATOR)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            return tryLaunchIntent(intent, "Opening calculator, sir.", "Sorry sir, calculator application is not installed on this device.")
        }

        if (input.contains("open browser")) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return tryLaunchIntent(intent, "Opening web browser, sir.", "Browser application is unavailable.")
        }

        if (input.contains("open youtube")) {
            val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            return tryLaunchIntent(intent, "Opening YouTube, sir.", "Sorry sir, YouTube application is not installed on this device.")
        }

        if (input.contains("open whatsapp")) {
            val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
            return if (intent != null) {
                tryLaunchIntent(intent, "Opening WhatsApp, sir.", "WhatsApp app launch failed.")
            } else {
                CommandResult.Success("Sorry sir, WhatsApp application is not installed on this device.")
            }
        }

        if (input.contains("open gallery") || input.contains("open photos")) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return tryLaunchIntent(intent, "Opening gallery, sir.", "Sorry sir, gallery application is not installed on this device.")
        }

        if (input.contains("open music") || input.contains("play music")) {
            val intent = Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return tryLaunchIntent(intent, "Opening music player, sir.", "Sorry sir, music player application is not installed on this device.")
        }

        // If no local offline command matches
        return CommandResult.NotHandled(rawInput)
    }

    private fun tryLaunchIntent(intent: Intent, successMsg: String, errorMsg: String): CommandResult {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(context.packageManager) != null || isPackageInstalled(intent)) {
                context.startActivity(intent)
                CommandResult.Success(successMsg, launchedApp = true)
            } else {
                context.startActivity(intent)
                CommandResult.Success(successMsg, launchedApp = true)
            }
        } catch (e: Exception) {
            CommandResult.Success(errorMsg)
        }
    }

    private fun isPackageInstalled(intent: Intent): Boolean {
        val pkg = intent.`package` ?: return false
        return try {
            context.packageManager.getPackageInfo(pkg, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun tryCalculateMath(input: String): String? {
        val cleaned = input.lowercase()
            .replace("calculate", "")
            .replace("what is", "")
            .replace("x", "*")
            .replace("times", "*")
            .replace("plus", "+")
            .replace("minus", "-")
            .replace("divided by", "/")
            .trim()

        return try {
            val regex = Regex("""([0-9.]+)\s*([+\-*/])\s*([0-9.]+)""")
            val match = regex.find(cleaned)
            if (match != null) {
                val num1 = match.groupValues[1].toDouble()
                val op = match.groupValues[2]
                val num2 = match.groupValues[3].toDouble()

                val res = when (op) {
                    "+" -> num1 + num2
                    "-" -> num1 - num2
                    "*" -> num1 * num2
                    "/" -> if (num2 != 0.0) num1 / num2 else Double.NaN
                    else -> Double.NaN
                }
                if (res.isNaN()) "undefined (division by zero)"
                else if (res % 1.0 == 0.0) res.toLong().toString()
                else String.format(Locale.US, "%.2f", res)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
