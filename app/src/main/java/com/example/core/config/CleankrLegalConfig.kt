package com.example.core.config

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Central configuration for Cleankr legal documentation and published policy URLs.
 * If the published URL needs to be updated, change it here without touching any UI code.
 */
object CleankrLegalConfig {
    /**
     * Existing published Privacy Policy URL for the Cleankr project.
     */
    const val PRIVACY_POLICY_URL: String =
        "https://github.com/cleankarservice-byte/Cleankr-Costumers-App/blob/main/PRIVACY_POLICY.md"

    /**
     * Optional published URL for Terms & Conditions.
     * Empty string by default so we do not invent unverified URLs.
     */
    const val TERMS_CONDITIONS_URL: String = ""

    /**
     * Optional published URL for Refund & Cancellation Policy.
     * Empty string by default so we do not invent unverified URLs.
     */
    const val REFUND_CANCELLATION_URL: String = ""

    /**
     * Opens a web URL securely using Android Custom Tabs with standard browser fallback.
     */
    fun openWebUrl(context: Context, url: String) {
        if (url.isBlank()) return
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                // Graceful handling if no browser application exists
            }
        }
    }
}
