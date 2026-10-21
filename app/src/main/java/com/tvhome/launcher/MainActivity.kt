package com.tvhome.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF101820)
private val Panel = Color(0xFF1A2732)
private val Accent = Color(0xFF65E6B7)
private val MainText = Color(0xFFF3F7FA)
private val MutedText = Color(0xFF9BAAB5)

data class LaunchApp(val label: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                                    val packageName = info.activityInfo.packageName
                                    if (packageName == packageNameOfSelf()) null
                                    else LaunchApp(
                                        info.loadLabel(pm).toString(),
                                        packageName,
                                        info.loadIcon(pm)
                                    )
                                }
                                .distinctBy { it.packageName }
                                .sortedBy { it.label.lowercase() }
                        }
                    )
                }
            }
        }
    }

    private fun packageNameOfSelf() = packageName
}

@Composable
private fun TVHomeScreen(
    onOpenApp: (String) -> Unit,
    onOpenSettings: () -> Unit,
    loadApps: () -> List<LaunchApp>
) {
    val context = LocalContext.current
    val apps = remember { mutableStateListOf<LaunchApp>() }
    LaunchedEffect(Unit) { apps.addAll(loadApps()) }

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(horizontal = 42.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconTile(Icons.Default.Tv, "TVHomeAI", Modifier.size(48.dp), selected = false)
            Column {
                Text("TVHomeAI", color = MainText, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Ruang hiburan anda", color = MutedText, fontSize = 14.sp)
            }
            Spacer(Modifier.weight(1f))
            FocusButton("Tetapan", Icons.Default.Settings, onOpenSettings)
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Aplikasi anda", color = MainText, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(if (apps.isEmpty()) "Tiada aplikasi pelancar ditemui" else "${apps.size} aplikasi tersedia",
                color = MutedText, fontSize = 13.sp)
        }

        if (apps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(18.dp)).background(Panel),
                contentAlignment = Alignment.Center
            ) {
                Text("Aplikasi yang dipasang akan dipaparkan di sini.", color = MutedText, fontSize = 16.sp)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Direka untuk Android TV", color = MutedText, fontSize = 12.sp)
            Text("Pilih aplikasi menggunakan alat kawalan jauh", color = MutedText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AppCard(app: LaunchApp, onClick: () -> Unit) {
    varFocusedCard()
    androidx.compose.runtime.key(app.packageName) {
        var focused = remember { androidx.compose.runtime.mutableStateOf(false) }
        Column(
            modifier = Modifier
                .size(width = 148.dp, height = 190.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (focused.value) Accent else Panel)
                .onFocusChanged { focused.value = it.isFocused }
                .clickable(onClick = onClick)
                .focusable()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF263744)),
                contentAlignment = Alignment.Center
            ) {
                Text(app.label.take(1).uppercase(), color = Accent, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                app.label,
                color = if (focused.value) Background else MainText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
        }
    }
}

private fun varFocusedCard() = Unit

@Composable
private fun IconTile(icon: ImageVector, label: String, modifier: Modifier = Modifier, selected: Boolean = false) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(if (selected) Accent else Panel),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Icon(icon, contentDescription = label, tint = if (selected) Background else Accent)
    }
}

@Composable
private fun FocusButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    var focused = remember { androidx.compose.runtime.mutableStateOf(false) }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (focused.value) Accent else Panel)
            .onFocusChanged { focused.value = it.isFocused }
            .clickable(onClick = onClick)
            .focusable()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        androidx.compose.material3.Icon(icon, contentDescription = null, tint = if (focused.value) Background else Accent)
        Text(label, color = if (focused.value) Background else MainText, fontSize = 14.sp)
    }
}
