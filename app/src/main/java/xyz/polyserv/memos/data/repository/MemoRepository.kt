package xyz.polyserv.memos.data.repository

import xyz.polyserv.memos.data.local.LocalDataSource
import xyz.polyserv.memos.data.model.Memo
import xyz.polyserv.memos.data.model.SyncOperation
import xyz.polyserv.memos.data.model.SyncQueueItem
import xyz.polyserv.memos.data.model.SyncStatus
import xyz.polyserv.memos.data.remote.RemoteDataSource
import xyz.polyserv.memos.sync.NetworkConnectivityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class MemoRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val connectivityManager: NetworkConnectivityManager
) {
    fun getAllMemos(): Flow<List<Memo>> =
        localDataSource.getAllMemosFlow()

    fun searchMemos(query: String): Flow<List<Memo>> =
        localDataSource.searchMemos(query)

    suspend fun getMemoById(id: String): Memo? =
        localDataSource.getMemoById(id)

    suspend fun createMemo(content: String): Memo {
        val localMemo = Memo(
            content = content,
            syncStatus = if (connectivityManager.isNetworkAvailable())
                SyncStatus.SYNCING else SyncStatus.PENDING,
            isLocalOnly = !connectivityManager.isNetworkAvailable()
        )

        try {
            localDataSource.saveMemo(localMemo)

            return if (connectivityManager.isNetworkAvailable()) {
                try {
                    val remoteMemo = remoteDataSource.createMemo(content)
                    val syncedMemo = localMemo.copy(
                        id = remoteMemo.id,
                        serverId = remoteMemo.serverId,
                        syncStatus = SyncStatus.SYNCED,
                        lastSyncTime = System.currentTimeMillis(),
                        isLocalOnly = false
                    )
                    localDataSource.saveMemo(syncedMemo)
                    syncedMemo
                } catch (e: Exception) {
                    Timber.e(e, "Failed to sync memo")
                    val failedMemo = localMemo.copy(syncStatus = SyncStatus.FAILED)
                    localDataSource.saveMemo(failedMemo)
                    // Добавляем в очередь для повторной попытки
                    addToSyncQueue(localMemo.id, SyncOperation.CREATE, localMemo)
                    failedMemo
                }
            } else {
                // Добавляем в очередь синхронизации
                addToSyncQueue(localMemo.id, SyncOperation.CREATE, localMemo)
                localMemo
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating memo")
            throw e
        }
    }

    suspend fun updateMemo(id: String, content: String): Memo? {
        val memo = localDataSource.getMemoById(id) ?: return null

        val updatedMemo = memo.copy(
            content = content,
            updatedTs = System.currentTimeMillis(),
            syncStatus = if (connectivityManager.isNetworkAvailable())
                SyncStatus.SYNCING else SyncStatus.PENDING
        )

        localDataSource.saveMemo(updatedMemo)

        return if (connectivityManager.isNetworkAvailable() && !memo.isLocalOnly) {
            try {
                val remoteMemo = remoteDataSource.updateMemo(memo.serverId, content)
                val syncedMemo = updatedMemo.copy(
                    syncStatus = SyncStatus.SYNCED,
                    lastSyncTime = System.currentTimeMillis()
                )
                localDataSource.saveMemo(syncedMemo)
                syncedMemo
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync update")
                val failedMemo = updatedMemo.copy(syncStatus = SyncStatus.FAILED)
                localDataSource.saveMemo(failedMemo)
                addToSyncQueue(id, SyncOperation.UPDATE, updatedMemo)
                failedMemo
            }
        } else {
            addToSyncQueue(id, SyncOperation.UPDATE, updatedMemo)
            updatedMemo
        }
    }

    suspend fun deleteMemo(id: String) {
        val memo = localDataSource.getMemoById(id) ?: return

        return if (connectivityManager.isNetworkAvailable() && !memo.isLocalOnly) {
            try {
                remoteDataSource.deleteMemo(memo.serverId)
                localDataSource.deleteMemo(id)
                localDataSource.clearMemoSyncQueue(id)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete memo on server")
                addToSyncQueue(id, SyncOperation.DELETE, memo)
            }
        } else {
            localDataSource.deleteMemo(id)
            if (!memo.isLocalOnly) {
                addToSyncQueue(id, SyncOperation.DELETE, memo)
            } else {}
        }
    }

    private suspend fun addToSyncQueue(
        memoId: String,
        operation: SyncOperation,
        memo: Memo
    ) {
        val payload = memo.content // В продакшене можно использовать JSON serialization
        val queueItem = SyncQueueItem(
            memoId = memoId,
            operation = operation,
            payload = payload
        )
        localDataSource.addToSyncQueue(queueItem)
    }

    suspend fun syncPendingChanges() {
        if (!connectivityManager.isNetworkAvailable()) {
            Timber.d("No network available for sync")
            return
        }

        Timber.d("Sync started")

        val queueItems = localDataSource.getSyncQueue()
        for (item in queueItems) {
            try {
                syncQueueItem(item)
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync queue item ${item.id}")
                val updatedItem = item.copy(retryCount = item.retryCount + 1)
                if (updatedItem.retryCount < updatedItem.maxRetries) {
                    // Retry later
                } else {
                    localDataSource.removeSyncQueueItem(item.id)
                }
            }
        }
    }

    private suspend fun syncQueueItem(item: SyncQueueItem) {
        val memo = localDataSource.getMemoById(item.memoId) ?: return

        when (item.operation) {
            SyncOperation.CREATE -> {
                val remoteMemo = remoteDataSource.createMemo(item.payload)
                localDataSource.saveMemo(memo.copy(
                    serverId = remoteMemo.serverId,
                    syncStatus = SyncStatus.SYNCED,
                    lastSyncTime = System.currentTimeMillis(),
                    isLocalOnly = false
                ))
            }
            SyncOperation.UPDATE -> {
                remoteDataSource.updateMemo(memo.serverId, item.payload)
                localDataSource.updateMemoSyncStatus(
                    memo.id,
                    SyncStatus.SYNCED,
                    System.currentTimeMillis()
                )
            }
            SyncOperation.DELETE -> {
                remoteDataSource.deleteMemo(memo.serverId)
                localDataSource.deleteMemo(memo.id)
            }
        }
        localDataSource.removeSyncQueueItem(item.id)
    }
}
