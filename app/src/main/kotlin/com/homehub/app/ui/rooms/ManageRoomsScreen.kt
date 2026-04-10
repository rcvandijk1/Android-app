package com.homehub.app.ui.rooms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.homehub.app.R
import com.homehub.app.data.Room

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRoomsScreen(
    viewModel: ManageRoomsViewModel,
    onBack: () -> Unit,
) {
    val rooms by viewModel.rooms.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<Room?>(null) }
    var confirmingDelete by remember { mutableStateOf<Room?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rooms_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.rooms_add))
            }
        },
    ) { padding ->
        if (rooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.rooms_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            items(rooms, key = { it.id }) { room ->
                val index = rooms.indexOfFirst { it.id == room.id }
                ListItem(
                    headlineContent = { Text(room.name) },
                    trailingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(
                                onClick = { viewModel.move(room, -1) },
                                enabled = index > 0,
                            ) {
                                Icon(
                                    Icons.Outlined.ArrowUpward,
                                    contentDescription = stringResource(R.string.rooms_move_up),
                                )
                            }
                            IconButton(
                                onClick = { viewModel.move(room, +1) },
                                enabled = index < rooms.lastIndex,
                            ) {
                                Icon(
                                    Icons.Outlined.ArrowDownward,
                                    contentDescription = stringResource(R.string.rooms_move_down),
                                )
                            }
                            IconButton(onClick = { renaming = room }) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = stringResource(R.string.rooms_rename),
                                )
                            }
                            IconButton(onClick = { confirmingDelete = room }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.tile_menu_delete),
                                )
                            }
                        }
                    },
                )
            }
        }
    }

    if (showAddDialog) {
        RoomNameDialog(
            title = stringResource(R.string.rooms_add),
            initialValue = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.createRoom(name)
                showAddDialog = false
            },
        )
    }

    renaming?.let { room ->
        RoomNameDialog(
            title = stringResource(R.string.rooms_rename),
            initialValue = room.name,
            onDismiss = { renaming = null },
            onConfirm = { name ->
                viewModel.renameRoom(room, name)
                renaming = null
            },
        )
    }

    confirmingDelete?.let { room ->
        AlertDialog(
            onDismissRequest = { confirmingDelete = null },
            title = { Text(stringResource(R.string.rooms_delete_title)) },
            text = { Text(stringResource(R.string.rooms_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRoom(room)
                    confirmingDelete = null
                }) {
                    Text(stringResource(R.string.rooms_delete_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = null }) {
                    Text(stringResource(R.string.edit_cancel))
                }
            },
        )
    }
}

@Composable
private fun RoomNameDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(stringResource(R.string.picker_room_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = value.isNotBlank(),
                onClick = { onConfirm(value) },
            ) { Text(stringResource(R.string.picker_room_create)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.edit_cancel))
            }
        },
    )
}
