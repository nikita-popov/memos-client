package xyz.polyserv.memos.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.polyserv.memos.data.model.Memo
import xyz.polyserv.memos.presentation.ui.components.SyncStatusIndicator
import xyz.polyserv.memos.presentation.viewmodel.MemoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoDetailScreen(
    memo: Memo,
    viewModel: MemoViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onEditClick: (Memo) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState.value
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить заметку?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMemo(memo.id)
                        showDeleteDialog = false
                        onBackClick()
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Заметка") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { onEditClick(memo) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SyncStatusIndicator(syncStatus = memo.syncStatus)

            Text(
                text = memo.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Metadata
            Text(
                text = "Создано: ${java.text.SimpleDateFormat("dd.MM.yyyy HH:mm",
                    java.util.Locale.getDefault()).format(memo.createdTs)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (memo.updatedTs != memo.createdTs) {
                Text(
                    text = "Обновлено: ${java.text.SimpleDateFormat("dd.MM.yyyy HH:mm",
                        java.util.Locale.getDefault()).format(memo.updatedTs)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
