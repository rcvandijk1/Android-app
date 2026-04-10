package com.homehub.app.ui.edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.homehub.app.R
import com.homehub.app.icons.DeviceIcons
import com.homehub.app.ui.common.RoomPickerSheet
import com.homehub.app.util.toImageBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeviceScreen(
    viewModel: EditDeviceViewModel,
    deviceId: Long,
    onDone: () -> Unit,
) {
    LaunchedEffect(deviceId) { viewModel.load(deviceId) }

    val state by viewModel.state.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    var showAppPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    var showRoomPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val canSave = state.name.isNotBlank() && state.selectedApp != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (state.isEditMode) R.string.edit_title_edit
                            else R.string.edit_title_new,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
                actions = {
                    if (state.isEditMode) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.edit_delete))
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // --- Name ---------------------------------------------------
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text(stringResource(R.string.edit_name_label)) },
                placeholder = { Text(stringResource(R.string.edit_name_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // --- Icon ---------------------------------------------------
            SectionLabel(stringResource(R.string.edit_section_icon))
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val iconDef = remember(state.iconKey) { DeviceIcons.get(state.iconKey) }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { showIconPicker = true },
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = iconDef.image,
                            contentDescription = iconDef.label,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                    Text(
                        text = iconDef.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // --- Linked app --------------------------------------------
            SectionLabel(stringResource(R.string.edit_section_app))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAppPicker = true }
                    .padding(vertical = 8.dp),
            ) {
                val app = state.selectedApp
                if (app != null) {
                    Image(
                        bitmap = app.icon.toImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                    )
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Apps, contentDescription = null)
                    }
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = stringResource(R.string.edit_no_app_selected),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
                TextButton(onClick = { showAppPicker = true }) {
                    Text(
                        stringResource(
                            if (app == null) R.string.edit_pick_app else R.string.edit_change_app,
                        ),
                    )
                }
            }

            // --- Room ---------------------------------------------------
            SectionLabel(stringResource(R.string.edit_section_room))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRoomPicker = true }
                    .padding(vertical = 8.dp),
            ) {
                val roomName = rooms.firstOrNull { it.id == state.roomId }?.name
                    ?: stringResource(R.string.home_unassigned_room)
                Text(
                    text = roomName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { showRoomPicker = true }) {
                    Text(stringResource(R.string.edit_change_app))
                }
            }

            Spacer(Modifier.size(8.dp))

            // --- Save ---------------------------------------------------
            Button(
                onClick = { viewModel.save(onDone) },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.edit_save))
            }
        }
    }

    // --- Pickers ---------------------------------------------------------
    if (showAppPicker) {
        AppPickerSheet(
            apps = state.installedApps,
            isLoading = state.installedAppsLoading,
            onDismiss = { showAppPicker = false },
            onAppSelected = { app ->
                viewModel.onAppSelected(app)
                showAppPicker = false
            },
            onEnsureLoaded = viewModel::ensureInstalledAppsLoaded,
        )
    }

    if (showIconPicker) {
        IconPickerSheet(
            selectedKey = state.iconKey,
            onDismiss = { showIconPicker = false },
            onIconSelected = { key ->
                viewModel.onIconSelected(key)
                showIconPicker = false
            },
        )
    }

    if (showRoomPicker) {
        RoomPickerSheet(
            rooms = rooms,
            currentRoomId = state.roomId,
            onDismiss = { showRoomPicker = false },
            onRoomSelected = { id ->
                viewModel.onRoomSelected(id)
                showRoomPicker = false
            },
            onCreateRoom = { name -> viewModel.createRoom(name) },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.edit_delete_confirm_title)) },
            text = { Text(stringResource(R.string.edit_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete(onDone)
                }) {
                    Text(stringResource(R.string.edit_delete_confirm_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.edit_cancel))
                }
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}
