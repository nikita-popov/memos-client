package xyz.polyserv.memos.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import xyz.polyserv.memos.data.model.Memo
import xyz.polyserv.memos.data.model.SyncStatus
import xyz.polyserv.memos.data.repository.MemoRepository
import xyz.polyserv.memos.sync.NetworkConnectivityManager
import xyz.polyserv.memos.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class MemoUiState(
    val memos: List<Memo> = emptyList(),
    val selectedMemo: Memo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOnline: Boolean = true,
    val searchQuery: String = "",
    val filteredMemos: List<Memo> = emptyList(),
    val syncInProgress: Boolean = false,
    val pendingSyncCount: Int = 0
)

@HiltViewModel
class MemoViewModel @Inject constructor(
    private val memoRepository: MemoRepository,
    private val connectivityManager: NetworkConnectivityManager,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _uiState = mutableStateOf(MemoUiState())
    val uiState: State<MemoUiState> = _uiState

    val memos: StateFlow<List<Memo>> = memoRepository.getAllMemos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        setupNetworkListener()
        setupSyncScheduler()
        observeNetworkChanges()
    }

    private fun setupNetworkListener() {
        viewModelScope.launch {
            connectivityManager.isConnected.collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)
                if (isOnline) {
                    // Автоматическая синхронизация при восстановлении сети
                    syncPendingChanges()
                }
            }
        }
    }

    private fun setupSyncScheduler() {
        syncScheduler.scheduleSyncWork()
    }

    private fun observeNetworkChanges() {
        viewModelScope.launch {
            connectivityManager.isConnected.collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)
            }
        }
    }

    fun createMemo(content: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val memo = memoRepository.createMemo(content)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedMemo = null,
                    error = if (connectivityManager.isNetworkAvailable()) null
                    else "Offline mode: мемо будет синхронизировано при подключении"
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to create memo")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка при создании: ${e.message}"
                )
            }
        }
    }

    fun updateMemo(id: String, content: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                memoRepository.updateMemo(id, content)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update memo")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка при обновлении: ${e.message}"
                )
            }
        }
    }

    fun deleteMemo(id: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                memoRepository.deleteMemo(id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedMemo = null
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete memo")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка при удалении: ${e.message}"
                )
            }
        }
    }

    fun selectMemo(memo: Memo?) {
        _uiState.value = _uiState.value.copy(selectedMemo = memo)
    }

    fun searchMemos(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(searchQuery = query)
            if (query.isEmpty()) {
                _uiState.value = _uiState.value.copy(filteredMemos = emptyList())
            } else {
                memoRepository.searchMemos(query).collect { results ->
                    _uiState.value = _uiState.value.copy(filteredMemos = results)
                }
            }
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            filteredMemos = emptyList()
        )
    }

    fun syncPendingChanges() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(syncInProgress = true)
                memoRepository.syncPendingChanges()
                _uiState.value = _uiState.value.copy(syncInProgress = false)
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync")
                _uiState.value = _uiState.value.copy(
                    syncInProgress = false,
                    error = "Ошибка синхронизации: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
