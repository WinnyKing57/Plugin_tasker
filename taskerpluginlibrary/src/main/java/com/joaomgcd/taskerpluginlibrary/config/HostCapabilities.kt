package com.joaomgcd.taskerpluginlibrary.config

import android.content.Context
import android.content.pm.PackageManager

class HostInfo(val context: Context, val hostPackageName: String?) {
    val hostVersionCode: Int?
        get() {
            return hostPackageName?.let {
                try {
                    context.packageManager.getPackageInfo(it, 0).versionCode
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
            }
        }
    val hostVersionName: String?
        get() {
            return hostPackageName?.let {
                try {
                    context.packageManager.getPackageInfo(it, 0).versionName
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
            }
        }

    fun versionIsAtLeast(version: String): Boolean {
        val hostVersion = hostVersionName ?: return false
        val hostVersionParts = hostVersion.split(".")
        val versionParts = version.split(".")
        for (i in 0 until Math.max(hostVersionParts.size, versionParts.size)) {
            val hostPart = hostVersionParts.getOrNull(i)?.toIntOrNull() ?: 0
            val versionPart = versionParts.getOrNull(i)?.toIntOrNull() ?: 0
            if (hostPart > versionPart) return true
            if (hostPart < versionPart) return false
        }
        return true
    }
}

interface HostCapabilities {
    val hostInfo: HostInfo
    val supportsPassingThroughData get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsRelevantVariables get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsVariableNamesInArrays get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsVariableChecking get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsComplexVariables get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsConfigurableVariableNames get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsHtmlHelp get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsOutputOnlyVariables get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsPermissions get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsTimeout get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsSelfDestruction get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsReceiversForActionsAndConditions get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsForegroundExecution get() = hostInfo.versionIsAtLeast("5.9.3")
    val supportsEvents get() = hostInfo.versionIsAtLeast("5.9.3")
}
