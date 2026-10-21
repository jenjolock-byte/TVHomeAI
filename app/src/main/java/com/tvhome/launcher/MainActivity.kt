package com.tvhome.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Background = Color(0xFF08090E)
private val Panel = Color(0xFF171720)
private val PanelLight = Color(0xFF29232D)
private val Accent = Color(0xFFFF6B65)
private val MainText = Color(0xFFF8F7FA)
private val MutedText = Color(0xFFAAA6B2)
private val HeroGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF651D2A), Color(0xFF24131F), Color(0xFF101018))
)

data class LaunchApp(val label: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 9, 14)
        window.navigationBarColor = android.graphics.Color.rgb(8, 9, 14)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Background) {
                    TVHomeScreen(
                        onOpenApp = { packageName ->
                            val intent = packageManager.getLaunchIntentForPackage(packageName)
                            if (intent != null) startActivity(intent)
                            else Toast.makeText(this, "Unable to open app", Toast.LENGTH_SHORT).show()
                        },
                        onOpenSettings = { startActivity(Intent(android.provider.Settings.ACTION_SETTINGS)) },
                        onOpenWifi = { startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)) },
                        onAppInfo = { packageName ->
                            startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
                        },
                        onUninstall = { packageName ->
                            startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")))
                        },
                        loadApps = {
                            val pm = packageManager
                            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                            pm.queryIntentActivities(intent, 0)
                                .mapNotNull { info ->
                                    val appPackage = info.activityInfo.packageName
                                    if (appPackage == packageName) null
                                    else LaunchApp(info.loadLabel(pm).toString(), appPackage, info.loadIcon(pm))
                                }
                                .distinctBy { it.packageName }
                                .sortedBy { it.label.lowercase() }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TVHomeScreen(
    onOpenApp: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWifi: () -> Unit,
    onAppInfo: (String) -> Unit,
    onUninstall: (String) -> Unit,
    loadApps: () -> List<LaunchApp>
) {
    val apps = remember { mutableStateListOf<LaunchApp>() }
    val favorites = remember { mutableStateListOf<String>() }
    val selectedApp = remember { mutableStateOf<LaunchApp?>(null) }
    val showAllApps = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        apps.addAll(loadApps())
        selectedApp.value = apps.firstOrNull()
    }

    if (showAllApps.value) {
        AlertDialog(
            onDismissRequest = { showAllApps.value = false },
            containerColor = Panel,
            titleContentColor = MainText,
            textContentColor = MutedText,
            title = { Text("All Apps", fontWeight = FontWeight.Bold) },
            text = {
                if (apps.isEmpty()) {
                    Text("No apps available to display.", color = MutedText)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(apps, key = { it.packageName }) { app ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PanelLight.copy(alpha = 0.45f))
                                    .clickable {
                                        showAllApps.value = false
                                        onOpenApp(app.packageName)
                                    }
                                    .focusable()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AppIcon(app, 36.dp)
                                Text(
                                    app.label,
                                    color = MainText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllApps.value = false }) {
                    Text("Close", color = Accent)
                }
            }
        )
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        val compact = maxWidth < 600.dp
        Column(
            modifier = Modifier.fillMaxSize().padding(
                horizontal = if (compact) 18.dp else 48.dp,
                vertical = if (compact) 18.dp else 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (compact) 18.dp else 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(if (compact) 40.dp else 48.dp)
                        .clip(RoundedCornerShape(14.dp)).background(Accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Tv, contentDescription = "TVHomeAI", tint = Background,
                        modifier = Modifier.size(if (compact) 23.dp else 29.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text("TVHomeAI", color = MainText, fontSize = if (compact) 21.sp else 27.sp,
                        fontWeight = FontWeight.ExtraBold)
                    Text("Your entertainment, one screen", color = MutedText, fontSize = if (compact) 11.sp else 13.sp)
                }
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FocusAction("Wi-Fi", onOpenWifi, compact, iconType = "wifi")
                    FocusAction("Settings", onOpenSettings, compact)
                    FocusAction("Apps", { showAllApps.value = true }, compact, iconType = "apps")
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(if (compact) 22.dp else 26.dp))
                    .background(HeroGradient)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(if (compact) 22.dp else 26.dp))
                    .padding(if (compact) 20.dp else 30.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 13.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(Accent))
                    Text(
                        if (selectedApp.value == null) "WELCOME" else "FEATURED FOR YOU",
                        color = Color(0xFFFFAAA4), fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp
                    )
                }
                Text(
                    selectedApp.value?.label ?: "What would you like to watch?",
                    color = MainText, fontSize = if (compact) 25.sp else 34.sp,
                    lineHeight = if (compact) 30.sp else 40.sp, fontWeight = FontWeight.Bold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Text(
                    when {
                        selectedApp.value == null -> if (apps.isEmpty()) "Your installed apps will appear here." else "Focus an app to update this panel."
                        else -> "Selected app • Press OK to launch"
                    },
                    color = Color(0xFFD0C5CE), fontSize = if (compact) 12.sp else 15.sp,
                    lineHeight = if (compact) 17.sp else 21.sp
                )
                selectedApp.value?.let { app ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppIcon(app, if (compact) 34.dp else 42.dp)
                        Column {
                            Text(app.label, color = MainText, fontSize = if (compact) 13.sp else 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Installed on this device", color = Color(0xFFC7B8C4), fontSize = 10.sp)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Your Apps", color = MainText, fontSize = if (compact) 17.sp else 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${apps.size} apps", color = MutedText, fontSize = 11.sp)
                }
                if (apps.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                            .background(Panel).padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No apps available to display.", color = MutedText, fontSize = 14.sp)
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(apps, key = { it.packageName }) { app ->
                            AppCard(
                                app = app,
                                compact = compact,
                                selected = selectedApp.value?.packageName == app.packageName,
                                onFocus = { selectedApp.value = app },
                                onClick = { onOpenApp(app.packageName) },
                                isFavorite = favorites.contains(app.packageName),
                                onToggleFavorite = {
                                    if (favorites.contains(app.packageName)) favorites.remove(app.packageName)
                                    else favorites.add(app.packageName)
                                },
                                onMove = { direction ->
                                    val from = apps.indexOfFirst { it.packageName == app.packageName }
                                    val to = (from + direction).coerceIn(0, apps.lastIndex)
                                    if (from >= 0 && to != from) {
                                        val item = apps.removeAt(from)
                                        apps.add(to, item)
                                    }
                                },
                                onAppInfo = { onAppInfo(app.packageName) },
                                onUninstall = { onUninstall(app.packageName) }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f, fill = false))
            Text(
                if (compact) "Select an app • Press OK to launch" else "◀ ▶ Navigate     •     OK Select     •     Settings",
                color = MutedText, fontSize = if (compact) 10.sp else 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun AppIcon(app: LaunchApp, size: androidx.compose.ui.unit.Dp) {
    AndroidView(
        factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
        update = { view -> view.setImageDrawable(app.icon) },
        modifier = Modifier.size(size)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCard(
    app: LaunchApp,
    compact: Boolean,
    selected: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onMove: (Int) -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit
) {
    val focused = remember { mutableStateOf(false) }
    val showMenu = remember { mutableStateOf(false) }
    if (showMenu.value) {
        AlertDialog(
            onDismissRequest = { showMenu.value = false },
            containerColor = Panel,
            titleContentColor = MainText,
            textContentColor = MutedText,
            title = { Text(app.label, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    MenuRow("App Info", "Application details") {
                        showMenu.value = false
                        onAppInfo()
                    }
                    MenuRow("Move Left", "Change app position") {
                        showMenu.value = false
                        onMove(-1)
                    }
                    MenuRow("Move Right", "Ubah kedudukan ikon") {
                        showMenu.value = false
                        onMove(1)
                    }
                    MenuRow(if (isFavorite) "Remove from Favorites" else "Add to Favorites", "TVHomeAI favorites") {
                        onToggleFavorite()
                        showMenu.value = false
                    }
                    MenuRow("Uninstall", "Buka pengesahan Android") {
                        showMenu.value = false
                        onUninstall()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMenu.value = false }) { Text("Close", color = Accent) }
            }
        )
    }
    Column(
        modifier = Modifier
            .size(width = if (compact) 104.dp else 136.dp, height = if (compact) 124.dp else 160.dp)
            .graphicsLayer {
                val scale = if (focused.value) 1.045f else 1f
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(if (compact) 17.dp else 20.dp))
            .background(if (focused.value || selected) PanelLight else Panel)
            .border(
                width = if (focused.value || selected) 2.dp else 1.dp,
                color = if (focused.value || selected) Accent else Color(0xFF302A35),
                shape = RoundedCornerShape(if (compact) 17.dp else 20.dp)
            )
            .onFocusChanged {
                focused.value = it.isFocused
                if (it.isFocused) onFocus()
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu.value = true }
            )
            .focusable()
            .padding(if (compact) 12.dp else 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(if (compact) 54.dp else 68.dp)
                .clip(RoundedCornerShape(if (compact) 14.dp else 18.dp))
                .background(Color.White.copy(alpha = 0.96f)),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(app, if (compact) 43.dp else 55.dp)
        }
        Spacer(Modifier.height(if (compact) 9.dp else 12.dp))
        Text(
            app.label,
            color = MainText,
            fontSize = if (compact) 10.sp else 13.sp,
            fontWeight = if (focused.value || selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MenuRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = MainText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = MutedText, fontSize = 11.sp)
        }
    }
}

@Composable
private fun FocusAction(label: String, onClick: () -> Unit, compact: Boolean, iconType: String = "settings") {
    val focused = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(if (focused.value) Accent else Panel)
            .border(1.dp, if (focused.value) Accent else Color(0xFF39313C), RoundedCornerShape(13.dp))
            .onFocusChanged { focused.value = it.isFocused }
            .clickable(onClick = onClick)
            .focusable()
            .padding(horizontal = if (compact) 12.dp else 17.dp, vertical = if (compact) 10.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(when (iconType) { "wifi" -> Icons.Default.Wifi; "apps" -> Icons.Default.Apps; else -> Icons.Default.Settings }, contentDescription = label,
            tint = if (focused.value) Background else Accent, modifier = Modifier.size(16.dp))
        Text(label, color = if (focused.value) Background else MainText,
            fontSize = if (compact) 11.sp else 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
