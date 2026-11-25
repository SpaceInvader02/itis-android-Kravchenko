package com.example.homework3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val MAIN = "main"
    const val LOCATION = "location"
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannels(this)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Homework3App()
                }
            }
        }
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val data = intent ?: return
        val id = data.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, -1)
        if (id == -1) return
        ActiveNotificationStore.remove(id)
        val title = data.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TITLE).orEmpty()
        val text = data.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TEXT).orEmpty()
        val message = if (AppVisibility.inForeground) text else title
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun Homework3App() {
    val navController = rememberNavController()

    DisposableEffect(Unit) {
        AppVisibility.inForeground = true
        onDispose { AppVisibility.inForeground = false }
    }

    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainScreen(onOpenSecondScreen = { navController.navigate(Routes.LOCATION) })
        }
        composable(Routes.LOCATION) {
            LocationScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(onOpenSecondScreen: () -> Unit) {
    val context = LocalContext.current
    val notificationsPermissionRequired = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    var permissionGranted by remember {
        mutableStateOf(
            !notificationsPermissionRequired ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }

    LaunchedEffect(Unit) {
        if (notificationsPermissionRequired && !permissionGranted) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (!permissionGranted) {
        PermissionRequiredContent(
            onOpenSettings = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            }
        )
        return
    }

    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var ongoing by remember { mutableStateOf(false) }
    var importance by remember { mutableStateOf(NotificationImportance.DEFAULT) }
    var importanceExpanded by remember { mutableStateOf(false) }
    var notificationId by remember { mutableIntStateOf(1) }
    var editId by remember { mutableStateOf("") }
    var editIdError by remember { mutableStateOf(false) }

    var hasActiveNotifications by remember { mutableStateOf(ActiveNotificationStore.isNotEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.hint_notification_title)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.hint_notification_text)) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(checked = ongoing, onCheckedChange = { ongoing = it })
            Text(stringResource(R.string.checkbox_ongoing))
        }
        ExposedDropdownMenuBox(
            expanded = importanceExpanded,
            onExpandedChange = { importanceExpanded = it }
        ) {
            OutlinedTextField(
                value = importanceLabel(importance),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.label_importance)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = importanceExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            DropdownMenu(
                expanded = importanceExpanded,
                onDismissRequest = { importanceExpanded = false }
            ) {
                NotificationImportance.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(importanceLabel(option)) },
                        onClick = {
                            importance = option
                            importanceExpanded = false
                        }
                    )
                }
            }
        }
        Button(
            onClick = {
                NotificationHelper.show(context, notificationId, title, text, ongoing, importance)
                hasActiveNotifications = true
                notificationId++
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.send_notification))
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = editId,
            onValueChange = { editId = it.filter(Char::isDigit); editIdError = false },
            label = { Text(stringResource(R.string.hint_notification_id)) },
            singleLine = true,
            isError = editIdError,
            enabled = hasActiveNotifications,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    val id = editId.toIntOrNull()
                    if (id == null || ActiveNotificationStore.channelIdOf(id) == null) {
                        editIdError = true
                    } else {
                        NotificationHelper.update(context, id, title, text)
                    }
                },
                enabled = hasActiveNotifications,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.edit_notification))
            }
            OutlinedButton(
                onClick = {
                    NotificationHelper.cancelAll(context)
                    hasActiveNotifications = false
                },
                enabled = hasActiveNotifications,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.delete_all_notifications))
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onOpenSecondScreen,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.open_second_screen))
        }
    }
}

@Composable
private fun importanceLabel(importance: NotificationImportance): String = when (importance) {
    NotificationImportance.MIN -> stringResource(R.string.importance_min)
    NotificationImportance.DEFAULT -> stringResource(R.string.importance_default)
    NotificationImportance.HIGH -> stringResource(R.string.importance_high)
}

@Composable
private fun PermissionRequiredContent(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.permission_required_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onOpenSettings) {
            Text(stringResource(R.string.open_settings))
        }
    }
}
