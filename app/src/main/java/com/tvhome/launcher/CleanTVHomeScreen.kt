package com.tvhome.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CleanBlack = Color(0xFF050506)
private val CleanSurface = Color(0xFF151518)
private val CleanWhite = Color(0xFFF7F7F8)
private val CleanMuted = Color(0xFF92929B)
private val CleanGlow = Color(0xFFB9C7FF)

@Composable
fun CleanTVHomeScreen(
    onOpenApp: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWifi: () -> Unit,
    onAppInfo: (String) -> Unit,
    onUninstall: (String) -> Unit,
    loadApps: () -> List<LaunchApp>
) {
    val apps = remember { mutableStateListOf<LaunchApp>() }
    val selected = remember { mutableStateOf<LaunchApp?>(null) }
    val showLibrary = remember { mutableStateOf(false) }
    val clock = remember { mutableStateOf(SimpleDateFormat("h:mm", Locale.getDefault()).format(Date())) }
    val date = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())

    LaunchedEffect(Unit) {
        apps.addAll(loadApps())
        selected.value = apps.firstOrNull()
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(CleanBlack)) {
        val compact = maxWidth < 600.dp
        Column(
            Modifier.fillMaxSize().padding(horizontal = if (compact) 22.dp else 58.dp, vertical = if (compact) 18.dp else 30.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 18.dp else 24.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("TVHomeAI", color = CleanWhite, fontSize = if (compact) 20.sp else 25.sp, fontWeight = FontWeight.Bold)
                    Text("YOUR TV. YOUR SPACE.", color = CleanMuted, fontSize = 9.sp, letterSpacing = 2.sp)
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(clock.value, color = CleanWhite, fontSize = if (compact) 24.sp else 32.sp, fontWeight = FontWeight.Light)
                    Text(date, color = CleanMuted, fontSize = if (compact) 10.sp else 12.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                CleanAction("Inputs", Icons.Default.Tv, compact) { onOpenSettings() }
                CleanAction("Wi-Fi", Icons.Default.Wifi, compact) { onOpenWifi() }
                CleanAction("Settings", Icons.Default.Settings, compact) { onOpenSettings() }
                Spacer(Modifier.weight(1f))
                CleanAction("Library", Icons.Default.Apps, compact) { showLibrary.value = !showLibrary.value }
            }

            Box(
                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(if (compact) 22.dp else 30.dp))
                    .background(androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(Color(0xFF20212B), Color(0xFF0D0D11), CleanBlack),
                        radius = if (compact) 650f else 1100f
                    ))
                    .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(if (compact) 22.dp else 30.dp))
                    .padding(if (compact) 22.dp else 42.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)) {
                    Text("NOW READY TO PLAY", color = CleanGlow, fontSize = 10.sp, letterSpacing = 2.5.sp, fontWeight = FontWeight.Bold)
                    Text(selected.value?.label ?: "Welcome home", color = CleanWhite,
                        fontSize = if (compact) 30.sp else 48.sp, lineHeight = if (compact) 35.sp else 54.sp,
                        fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(if (selected.value == null) "Your installed apps will appear below." else "Press OK to launch " + (selected.value?.label ?: "the selected app"),
                        color = CleanMuted, fontSize = if (compact) 12.sp else 15.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(CleanGlow))
                        Text("${apps.size} APPS AVAILABLE", color = CleanMuted, fontSize = 10.sp, letterSpacing = 1.5.sp)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (showLibrary.value) "All applications" else "Your apps", color = CleanWhite, fontSize = if (compact) 16.sp else 19.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("◀  ▶  MOVE     •     OK  OPEN", color = CleanMuted, fontSize = 9.sp, letterSpacing = 1.sp)
            }
            if (apps.isEmpty()) {
                Text("No launchable apps found.", color = CleanMuted, fontSize = 13.sp)
            } else {
                LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(if (showLibrary.value) apps else apps.take(8), key = { it.packageName }) { app ->
                        CleanAppTile(app, compact, selected.value?.packageName == app.packageName,
                            onFocus = { selected.value = app }, onClick = { onOpenApp(app.packageName) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CleanAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, compact: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(if (focused) CleanWhite else CleanSurface)
            .border(1.dp, if (focused) CleanWhite else Color.White.copy(alpha = .10f), RoundedCornerShape(50))
            .onFocusChanged { focused = it.isFocused }.clickable(onClick = onClick).focusable()
            .padding(horizontal = if (compact) 12.dp else 17.dp, vertical = if (compact) 9.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (focused) CleanBlack else CleanWhite, modifier = Modifier.size(14.dp))
        Text(label, color = if (focused) CleanBlack else CleanWhite, fontSize = if (compact) 10.sp else 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CleanAppTile(app: LaunchApp, compact: Boolean, selected: Boolean, onFocus: () -> Unit, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Column(
        Modifier.width(if (compact) 94.dp else 124.dp).clip(RoundedCornerShape(18.dp))
            .background(if (focused || selected) Color(0xFF25252B) else CleanSurface)
            .border(if (focused || selected) 2.dp else 1.dp, if (focused || selected) CleanGlow else Color.White.copy(alpha = .07f), RoundedCornerShape(18.dp))
            .onFocusChanged { focused = it.isFocused; if (it.isFocused) onFocus() }
            .clickable(onClick = onClick).focusable().padding(horizontal = 10.dp, vertical = if (compact) 12.dp else 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.size(if (compact) 45.dp else 58.dp).clip(RoundedCornerShape(14.dp)).background(Color.White), contentAlignment = Alignment.Center) {
            val bitmap = remember(app.icon) { app.icon.toBitmap(width = 192, height = 192).asImageBitmap() }
            androidx.compose.foundation.Image(bitmap, contentDescription = app.label, modifier = Modifier.size(if (compact) 39.dp else 50.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
        }
        Text(app.label, color = CleanWhite, fontSize = if (compact) 10.sp else 12.sp,
            fontWeight = if (focused || selected) FontWeight.SemiBold else FontWeight.Normal, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
