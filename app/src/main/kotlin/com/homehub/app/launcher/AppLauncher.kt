package com.homehub.app.launcher

import android.content.Context
import android.content.Intent

sealed interface LaunchResult {
    data object Success : LaunchResult
    data object NotFound : LaunchResult
}

object AppLauncher {
    fun launch(context: Context, packageName: String): LaunchResult {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return LaunchResult.NotFound
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            LaunchResult.Success
        } catch (_: Throwable) {
            LaunchResult.NotFound
        }
    }
}
