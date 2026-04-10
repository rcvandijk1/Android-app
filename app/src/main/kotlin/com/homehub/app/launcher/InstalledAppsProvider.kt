package com.homehub.app.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lists apps installed on the device that expose a main launcher activity.
 * Uses [PackageManager.queryIntentActivities] with ACTION_MAIN + CATEGORY_LAUNCHER,
 * which is allowed under Android 11+ package visibility rules as long as a
 * matching `<queries>` block exists in the manifest — no QUERY_ALL_PACKAGES
 * permission needed.
 */
class InstalledAppsProvider(private val context: Context) {

    suspend fun list(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, 0)
        resolved
            .asSequence()
            .map { info ->
                InstalledApp(
                    label = info.loadLabel(pm).toString(),
                    packageName = info.activityInfo.packageName,
                    icon = info.loadIcon(pm),
                )
            }
            // Filter out Home Hub itself — picking it would create an infinite loop.
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    /** Resolves the label + icon for a package that's already been linked. */
    suspend fun resolve(packageName: String): InstalledApp? = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        try {
            @Suppress("DEPRECATION")
            val appInfo = pm.getApplicationInfo(packageName, 0)
            InstalledApp(
                label = pm.getApplicationLabel(appInfo).toString(),
                packageName = packageName,
                icon = pm.getApplicationIcon(appInfo),
            )
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}
