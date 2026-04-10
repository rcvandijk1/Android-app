package com.homehub.app.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.homehub.app.R
import com.homehub.app.data.Room
import kotlinx.coroutines.launch

/**
 * Bottom sheet for picking an existing room or creating a new one inline.
 * A null [currentRoomId] represents the "Unassigned" bucket.
 * [onCreateRoom] is suspended so the caller can persist the new room and
 * return its id before selection proceeds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomPickerSheet(
    rooms: List<Room>,
    currentRoomId: Long?,
    onDismiss: () -> Unit,
    onRoomSelected: (roomId: Long?) -> Unit,
    onCreateRoom: suspend (name: String) -> Long,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var creating by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.picker_room_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                item(key = "unassigned") {
                    ListItem(
                        headlineContent = {
                            Text(stringResource(R.string.home_unassigned_room))
                        },
                        leadingContent = {
                            Icon(Icons.Outlined.MeetingRoom, contentDescription = null)
                        },
                        trailingContent = {
                            if (currentRoomId == null) {
                                Icon(Icons.Outlined.Check, contentDescription = null)
                            }
                        },
                        modifier = Modifier.clickable { onRoomSelected(null) },
                    )
                    HorizontalDivider()
                }
                items(rooms, key = { it.id }) { room ->
                    ListItem(
                        headlineContent = { Text(room.name) },
                        leadingContent = {
                            Icon(Icons.Outlined.MeetingRoom, contentDescription = null)
                        },
                        trailingContent = {
                            if (currentRoomId == room.id) {
                                Icon(Icons.Outlined.Check, contentDescription = null)
                            }
                        },
                        modifier = Modifier.clickable { onRoomSelected(room.id) },
                    )
                }
            }

            HorizontalDivider()

            if (creating) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.picker_room_name_label)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        enabled = newName.isNotBlank(),
                        onClick = {
                            scope.launch {
                                val id = onCreateRoom(newName.trim())
                                onRoomSelected(id)
                            }
                        },
                    ) { Text(stringResource(R.string.picker_room_create)) }
                }
            } else {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.picker_room_new)) },
                    leadingContent = {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                    },
                    modifier = Modifier.clickable { creating = true },
                )
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
            ) { Text(stringResource(R.string.edit_cancel)) }
        }
    }
}
