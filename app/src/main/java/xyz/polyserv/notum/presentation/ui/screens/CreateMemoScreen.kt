package xyz.polyserv.notum.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.polyserv.notum.presentation.viewmodel.MemoViewModel
import xyz.polyserv.notum.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMemoScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MemoViewModel = hiltViewModel(),
    memoId: String? = null
) {
    val uiState = viewModel.uiState.value
    var content by remember { mutableStateOf("") }
    var isLoaded by remember { mutableStateOf(false) }

    // Load memo if ID passed
    LaunchedEffect(memoId) {
        if (memoId != null) {
            viewModel.loadMemoById(memoId)
        } else {
            // Clean if create new
            viewModel.selectMemo(null)
        }
    }

    // Fill from loaded content
    LaunchedEffect(uiState.selectedMemo) {
        if (memoId != null && uiState.selectedMemo != null && !isLoaded) {
            if (uiState.selectedMemo.id == memoId) {
                content = uiState.selectedMemo.content
                isLoaded = true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(if (memoId == null) stringResource(id = R.string.new_note) else stringResource(id = R.string.edit))
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        if (content.isNotBlank()) {
                            if (memoId != null) {
                                // Edit
                                viewModel.updateMemo(memoId, content)
                            } else {
                                // Create
                                viewModel.createMemo(content)
                            }
                            onBackClick()
                        }
                    },
                    enabled = content.isNotBlank() && !uiState.isLoading
                ) {
                    Icon(Icons.Default.Check, contentDescription = stringResource(id = R.string.save))
                }
            }
        )

        // Text Editor
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            placeholder = { Text(stringResource(id = R.string.start_writing)) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        }
    }
}
