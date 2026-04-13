package com.homehub.app.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.homehub.app.R
import com.homehub.app.data.Device
import com.homehub.app.ui.common.RoomPickerSheet
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddDevice: () -> Unit,
    onEditDevice: (Long) -> Unit,
    onManageRooms: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val groups by viewModel.groups.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val appMetadata by viewModel.appMetadata.collectAsState()

    var menuOpen by remember { mutableStateOf(false) }
    var deviceBeingMoved by remember { mutableStateOf<Device?>(null) }

    // One-shot launch-failure events: toast + navigate to edit for re-linking.
    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is HomeEvent.LaunchFailed -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.launch_error_not_installed),
                        Toast.LENGTH_LONG,
                    ).show()
                    onEditDevice(event.deviceId)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.common_more))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_menu_manage_rooms)) },
                            onClick = {
                                menuOpen = false
                                onManageRooms()
                            },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDevice) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.home_add_device))
            }
        },
    ) { padding ->
        if (groups.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        // Resolve composable-only resources up-front so the LazyVerticalGrid's
        // (non-composable) item DSL can reuse them safely.
        val unassignedTitle = stringResource(R.string.home_unassigned_room)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            groups.forEach { group ->
                val title = group.room?.name ?: unassignedTitle

                // Section header spans all columns.
                item(
                    key = "header-${group.room?.id ?: -1L}",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    RoomSectionHeader(title = title, deviceCount = group.devices.size)
                }

                items(
                    items = group.devices,
                    key = { device -> device.id },
                ) { device ->
                    LaunchedEffect(device.packageName) {
                        viewModel.ensureAppMetadata(device.packageName)
                    }
                    DeviceTile(
                        device = device,
                        appMetadata = appMetadata[device.packageName],
                        onClick = { viewModel.onDeviceTapped(context, device) },
                        onEdit = { onEditDevice(device.id) },
                        onMove = { deviceBeingMoved = device },
                        onDelete = { viewModel.deleteDevice(device) },
                    )
                }
            }
        }
    }

    // "Move to room…" sheet reuses the shared picker.
    deviceBeingMoved?.let { device ->
        RoomPickerSheet(
            rooms = rooms,
            currentRoomId = device.roomId,
            onDismiss = { deviceBeingMoved = null },
            onRoomSelected = { newRoomId ->
                viewModel.moveDeviceToRoom(device, newRoomId)
                deviceBeingMoved = null
            },
            onCreateRoom = { name ->
                viewModel.createRoom(name)
            },
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp),
            )
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.home_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

