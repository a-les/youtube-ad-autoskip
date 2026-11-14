package com.autoskip.yt

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Locale

class YouTubeAutoSkipService : AccessibilityService() {

    companion object {
        private const val TAG = "YTAutoSkipService"
        var isServiceRunning = false
            private set

        // Official YouTube package names
        private const val PKG_YOUTUBE = "com.google.android.youtube"
        private const val PKG_YT_MUSIC = "com.google.android.apps.youtube.music"

        // Known View IDs for YouTube skip button
        private val SKIP_BUTTON_IDS = listOf(
            "com.google.android.youtube:id/skip_ad_button",
            "com.google.android.youtube:id/modern_skip_ad_button",
            "com.google.android.youtube:id/skip_ad_button_container",
            "com.google.android.youtube:id/ad_skip_button",
            "com.google.android.apps.youtube.music:id/skip_ad_button"
        )

        // Multilingual keywords for skip button
        private val SKIP_TEXT_KEYWORDS = listOf(
            "Пропустить",
            "Пропустить рекламу",
            "Skip",
            "Skip ad",
            "Skip ads",
            "Skip advertisement",
            "Omitir anuncio",
            "Pular anúncio",
            "Überspringen",
            "Passer l'annonce",
            "Passer",
            "Lewati",
            "Ignorar",
            "Przeskocz"
        )
    }

    private lateinit var prefs: PreferencesManager
    private var lastClickTimestamp: Long = 0L

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager(this)
        if (prefs.serviceStartTimestamp <= 0L) {
            prefs.serviceStartTimestamp = System.currentTimeMillis()
        }
        isServiceRunning = true
        Log.i(TAG, "YouTubeAutoSkipService created")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        Log.i(TAG, "YouTubeAutoSkipService destroyed")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName != PKG_YOUTUBE && packageName != PKG_YT_MUSIC) {
            return
        }

        if (packageName == PKG_YT_MUSIC && !prefs.isYtMusicEnabled) {
            return
        }

        val rootNode = rootInActiveWindow ?: return

        try {
            if (prefs.isAutoSkipEnabled) {
                val now = SystemClock.uptimeMillis()
                if (now - lastClickTimestamp > 500) {
                    val skipped = tryClickSkipButton(rootNode)
                    if (skipped) {
                        lastClickTimestamp = now
                        prefs.incrementAdsSkipped()
                        Log.d(TAG, "Ad skipped successfully! Total count: ${prefs.adsSkippedCount}")
                    }
                }
            }
        } finally {
            rootNode.recycle()
        }
    }

    private fun tryClickSkipButton(rootNode: AccessibilityNodeInfo): Boolean {
        // 1. Search by resource ID
        for (id in SKIP_BUTTON_IDS) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(id)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    // Do NOT click if node is displaying a countdown
                    if (isCountdownOrPreskip(node)) {
                        continue
                    }
                    if (clickNodeOrParent(node)) {
                        recycleNodes(nodes)
                        return true
                    }
                }
                recycleNodes(nodes)
            }
        }

        // 2. Search by localized text
        for (keyword in SKIP_TEXT_KEYWORDS) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(keyword)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    // Do NOT click if node is displaying a countdown
                    if (isCountdownOrPreskip(node)) {
                        continue
                    }
                    if (clickNodeOrParent(node)) {
                        recycleNodes(nodes)
                        return true
                    }
                }
                recycleNodes(nodes)
            }
        }

        return false
    }

    private fun isCountdownOrPreskip(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""
        val combined = "$text $desc".lowercase(Locale.getDefault())

        // Check if node contains countdown phrases like "через", "skip in", "seconds"
        if (combined.contains("через") || 
            combined.contains("skip in") || 
            combined.contains("после") || 
            combined.contains("before") ||
            combined.contains("seconds") ||
            combined.contains("sec")) {
            return true
        }

        // Check if node contains countdown digits
        if (combined.any { it.isDigit() }) {
            return true
        }

        // Check children recursively
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val isChildCountdown = isCountdownOrPreskip(child)
            child.recycle()
            if (isChildCountdown) return true
        }

        return false
    }

    private fun clickNodeOrParent(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        var depth = 0
        while (current != null && depth < 4) {
            if (current.isClickable) {
                val clicked = current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (clicked) {
                    return true
                }
            }
            current = current.parent
            depth++
        }
        return false
    }

    private fun recycleNodes(nodes: List<AccessibilityNodeInfo>) {
        for (node in nodes) {
            try {
                node.recycle()
            } catch (e: Exception) {
                // Ignore recycle errors
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "YouTubeAutoSkipService interrupted")
    }
}
