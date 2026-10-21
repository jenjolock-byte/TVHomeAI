package com.tvhome.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF0B1018)
private val Panel = Color(0xFF151F2C)
private val PanelLight = Color(0xFF202D3D)
private val Accent = Color(0xFF69F0C0)
private val MainText = Color(0xFFF4F7FB)
private val MutedText = Color(0xFF9AA9BA)

data class LaunchApp(val label: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(11, 16, 24)
        window.navigationBarColor = android.graphics.Color.rgb(11, 16, 24)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Background) {
                    TVHomeScreen(
                        onOpenApp = { packageName ->
                            val intent = packageManager.getLaunchIntentForPackage(packageName)
                            if (intent != null) startActivity(intent)
                            else Toast.makeText(this, "Aplikasi tidak boleh dibuka", Toast.LENGTH_SHORT).show()
                        },
                        onOpenSettings = {
                            startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
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
    loadApps: () -> List<LaunchApp>
) {
    val apps = remember { mutableStateListOf<LaunchApp>() }
    LaunchedEffect(Unit) { apps.addAll(loadApps()) }

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(horizontal = 48.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(Accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tv, contentDescription = "TVHomeAI", tint = Background, modifier = Modifier.size(29.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("TVHomeAI", color = MainText, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                Text("Semua hiburan, satu skrin", color = MutedText, fontSize = 13.sp)
            }
            Spacer(Modifier.weight(1f))
            FocusAction("Tetapan", Icons.Default.Settings, onOpenSettings)
        }

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(0.92f).clip(RoundedCornerShape(26.dp))
                    .background(Panel).padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Accent))
                    Text("LAMAN UTAMA", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
                Text("Apa yang anda mahu tonton?", color = MainText, fontSize = 27.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (apps.isEmpty()) "Aplikasi anda akan dipaparkan di sini apabila tersedia."
                    else "${apps.size} aplikasi sedia untuk dibuka.",
                    color = MutedText, fontSize = 15.sp, lineHeight = 22.sp
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Icon(Icons.Default.Apps, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                    Text("Pilih kad aplikasi menggunakan remote", color = MainText, fontSize = 12.sp)
                }
            }

            Column(
                modifier = Modifier.weight(1.08f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Aplikasi anda", color = MainText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${apps.size} aplikasi", color = MutedText, fontSize = 12.sp)
                }
                if (apps.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(20.dp))
                            .background(Panel).padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada aplikasi untuk dipaparkan.", color = MutedText, fontSize = 15.sp)
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(apps, key = { it.packageName }) { app ->
                            AppCard(app = app, onClick = { onOpenApp(app.packageName) })
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel.copy(alpha = 0.72f))
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TVHOMEAI", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
            Spacer(Modifier.weight(1f))
            Text("◀ ▶ Navigasi   •   OK Pilih   •   Tetapan untuk sistem", color = MutedText, fontSize = 11.sp)
        }
    }
}

@Composable
private fun AppCard(app: LaunchApp, onClick: () -> Unit) {
    val focused = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .size(width = 136.dp, height = 176.dp)
            .graphicsLayer {
                val scale = if (focused.value) 1.045f else 1f
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(if (focused.value) PanelLight else Panel)
            .border(
                width = if (focused.value) 2.dp else 1.dp,
                color = if (focused.value) Accent else Color(0xFF263342),
                shape = RoundedCornerShape(20.dp)
            )
            .onFocusChanged { focused.value = it.isFocused }
            .clickable(onClick = onClick)
            .focusable()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(68.dp).clip(RoundedCornerShape(20.dp))
                .background(if (focused.value) Accent.copy(alpha = 0.16f) else Color(0xFF263747)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                app.label.trim().take(1).uppercase(),
                color = Accent,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            app.label,
            color = MainText,
            fontSize = 13.sp,
            fontWeight = if (focused.value) FontWeight.Bold else FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FocusAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    val focused = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (focused.value) Accent else Panel)
            .border(1.dp, if (focused.value) Accent else Color(0xFF293747), RoundedCornerShape(14.dp))
            .onFocusChanged { focused.value = it.isFocused }
            .clickable(onClick = onClick)
            .focusable()
            .padding(horizontal = 17.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(icon, contentDescription = null, tint = if (focused.value) Background else Accent, modifier = Modifier.size(18.dp))
        Text(label, color = if (focused.value) Background else MainText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
