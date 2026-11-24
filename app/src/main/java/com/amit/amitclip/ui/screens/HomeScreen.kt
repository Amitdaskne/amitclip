package com.amit.amitclip.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amit.amitclip.data.Clip
import com.amit.amitclip.service.ClipAccessibilityService
import com.amit.amitclip.ui.components.GlassCard
import com.amit.amitclip.ui.theme.*
import com.amit.amitclip.viewmodel.ClipViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(vm: ClipViewModel) {
    val ctx = LocalContext.current
    val clips by vm.clips.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        GradientBackground()

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            TopBar(onSettingsClick = { showSettings = !showSettings })

            Spacer(Modifier.height(16.dp))
            SearchBar(query) { vm.setQuery(it) }

            if (showSettings) {
                Spacer(Modifier.height(12.dp))
                PermissionCard(ctx)
            }

            Spacer(Modifier.height(16.dp))

            if (clips.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(clips, key = { it.id }) { clip ->
                        ClipCard(
                            clip = clip,
                            onCopy = { copyToClipboard(ctx, clip.text) },
                            onPin = { vm.togglePin(clip) },
                            onDelete = { vm.delete(clip) },
                            onShare = { shareText(ctx, clip.text) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                val text = readClipboard(ctx)
                if (!text.isNullOrBlank()) vm.addClip(text)
            },
            containerColor = Color.White.copy(alpha = 0.15f),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add from clipboard")
        }
    }
}

@Composable
private fun GradientBackground() {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(BgDeep, BgMid, BgAccent),
                    start = androidx.compose.ui.geometry.Offset.Zero,
                    end = androidx.compose.ui.geometry.Offset.Infinite
                )
            )
    ) {
        Blob(BlobPurple, 300.dp, Alignment.TopStart, (-60).dp, 80.dp)
        Blob(BlobTeal, 260.dp, Alignment.CenterEnd, 100.dp, (-40).dp)
        Blob(BlobOrange, 220.dp, Alignment.BottomStart, (-40).dp, (-60).dp)
    }
}

@Composable
private fun BoxScope.Blob(
    color: Color,
    size: androidx.compose.ui.unit.Dp,
    align: Alignment,
    offsetX: androidx.compose.ui.unit.Dp,
    offsetY: androidx.compose.ui.unit.Dp
) {
    Box(
        Modifier
            .align(align)
            .offset(x = offsetX, y = offsetY)
            .size(size)
            .blur(100.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
            .background(color.copy(alpha = 0.35f), CircleShape)
    )
}

@Composable
private fun TopBar(onSettingsClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "AmitClip",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Outlined.Settings, null, tint = TextPrimary)
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18,
        contentPadding = 4
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = TextSecondary)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text("Search clips...", color = TextSecondary, fontSize = 16.sp)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextPrimary,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(ctx: Context) {
    val enabled = isAccessibilityEnabled(ctx)
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (enabled) Icons.Default.CheckCircle else Icons.Default.Info,
                    null,
                    tint = if (enabled) BlobTeal else BlobOrange
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (enabled) "Auto-capture is ON" else "Auto-capture is OFF",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                if (enabled) "Copied text will be saved automatically."
                else "Enable accessibility to auto-capture copied text.",
                color = TextSecondary,
                fontSize = 13.sp
            )
            if (!enabled) {
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }) {
                    Text("Open Settings", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        Modifier.fillMaxWidth().padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.ContentPaste,
                null,
                tint = TextSecondary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("No clips yet", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("Copy something or tap +", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ClipCard(
    clip: Clip,
    onCopy: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val typeIcon = when (clip.type) {
        "url" -> Icons.Default.Link
        "email" -> Icons.Default.Email
        "phone" -> Icons.Default.Phone
        else -> Icons.Default.Notes
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(typeIcon, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    formatTime(clip.timestamp),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.weight(1f))
                if (clip.isPinned) {
                    Icon(Icons.Default.PushPin, null, tint = BlobOrange, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                clip.text,
                color = TextPrimary,
                fontSize = 15.sp,
                maxLines = 4,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ActionIcon(Icons.Default.ContentCopy, onCopy)
                ActionIcon(Icons.Default.Share, onShare)
                ActionIcon(
                    if (clip.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                    onPin
                )
                Spacer(Modifier.weight(1f))
                ActionIcon(Icons.Default.Delete, onDelete, tint = BlobOrange)
            }
        }
    }
}

@Composable
private fun ActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tint: Color = TextSecondary
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.06f))
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

private fun formatTime(ts: Long): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return sdf.format(Date(ts))
}

private fun readClipboard(ctx: Context): String? {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return if (cm.hasPrimaryClip()) cm.primaryClip?.getItemAt(0)?.text?.toString() else null
}

private fun copyToClipboard(ctx: Context, text: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("AmitClip", text))
}

private fun shareText(ctx: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(Intent.createChooser(intent, "Share"))
}

private fun isAccessibilityEnabled(ctx: Context): Boolean {
    val serviceName = "${ctx.packageName}/${ClipAccessibilityService::class.java.name}"
    val enabled = Settings.Secure.getString(
        ctx.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabled.split(":").any { it.equals(serviceName, ignoreCase = true) }
}
