package com.amit.amitclip.service

import android.accessibilityservice.AccessibilityService
import android.content.ClipboardManager
import android.view.accessibility.AccessibilityEvent
import com.amit.amitclip.AmitClipApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ClipAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastClip: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        lastClip = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) {
            captureClipboard()
        }
    }

    private fun captureClipboard() {
        try {
            val cm = getSystemService(ClipboardManager::class.java) ?: return
            if (!cm.hasPrimaryClip()) return
            val clip = cm.primaryClip ?: return
            if (clip.itemCount == 0) return
            val text = clip.getItemAt(0).text?.toString() ?: return
            if (text.isBlank() || text == lastClip) return
            lastClip = text
            val app = application as AmitClipApp
            scope.launch { app.repository.addClip(text) }
        } catch (_: Exception) { }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
