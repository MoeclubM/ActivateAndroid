package moe.telecom.activateandroid

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private var isFloatingWindowActive by mutableStateOf(false)

    // 创建一个ActivityResultLauncher，用来请求悬浮窗权限
    private val requestOverlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Settings.canDrawOverlays(this)) {
                // 权限授予，启动悬浮窗服务
                startService(Intent(this, FloatWindowService::class.java))
            } else {
                // 权限未授予，提示用户
                // 你可以给出提示让用户知道需要此权限
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkOverlayPermission()

        setContent {
            FloatingWindowApp(
                isFloatingWindowActive = isFloatingWindowActive,
                onSwitchChanged = { toggleFloatingWindow(it) }
            )
        }
    }

    private fun checkOverlayPermission() {
        // 如果是 Android 8 (API 26) 或更高版本，检查是否有悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!Settings.canDrawOverlays(this)) {
                // 如果没有权限，跳转到权限页面
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
                requestOverlayPermissionLauncher.launch(intent)
            }
        }
    }

    private fun toggleFloatingWindow(isActive: Boolean) {
        isFloatingWindowActive = isActive
        if (isActive) {
            startService(Intent(this, FloatWindowService::class.java))
        } else {
            stopService(Intent(this, FloatWindowService::class.java))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingWindowApp(isFloatingWindowActive: Boolean, onSwitchChanged: (Boolean) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Activate Android") })
        }
    ) { paddingValues ->
        // 使用paddingValues来避免顶部被遮挡
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Text("Display \"Activate Android\" on your Android Devices", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Start:", fontSize = 16.sp)
            Switch(
                checked = isFloatingWindowActive,
                onCheckedChange = onSwitchChanged
            )
        }
    }
}
