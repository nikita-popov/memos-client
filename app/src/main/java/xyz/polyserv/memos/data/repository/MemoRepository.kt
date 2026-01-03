package xyz.polyserv.memos.data.repository

import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import xyz.polyserv.memos.data.local.LocalDataSource
import xyz.polyserv.memos.data.remote.RemoteDataSource
import xyz.polyserv.memos.data.model.Memo
import xyz.polyserv.memos.data.model.SyncQueueItem
import xyz.polyserv.memos.data.model.SyncAction
import xyz.polyserv.memos.data.model.SyncStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    fun getMemos(): Flow<List<Memo>> = localDataSource.getAllMemosFlow()

    suspend fun getMemoById(memoId: String): Memo? {
        return localDataSource.getMemoById(memoId)
    }

    suspend fun addMemo(memo: Memo) {
        localDataSource.saveMemo(memo)
        localDataSource.addToSyncQueue(
            SyncQueueItem(
                memoId = memo.id,
                action = SyncAction.CREATE,
                payload = memo.content,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateMemo(memo: Memo) {
        localDataSource.saveMemo(memo)
        localDataSource.addToSyncQueue(
            SyncQueueItem(
                memoId = memo.id,
                action = SyncAction.UPDATE,
                payload = memo.content,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteMemo(memoId: String) {
        localDataSource.deleteMemo(memoId)
        localDataSource.addToSyncQueue(
            SyncQueueItem(
                memoId = memoId,
                action = SyncAction.DELETE,
                payload = "",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun searchMemos(query: String): Flow<List<Memo>> =  localDataSource.searchMemos(query)

    suspend fun syncWithServer() {
        try {
            // Set SYNCING for all memos in sync queue
            val syncQueue = localDataSource.getSyncQueue()
            for (queueItem in syncQueue) {
                val memo = localDataSource.getMemoById(queueItem.memoId)
                if (memo != null && memo.syncStatus == SyncStatus.PENDING) {
                    val syncingMemo = memo.copy(syncStatus = SyncStatus.SYNCING)
                    localDataSource.saveMemo(syncingMemo)
                }
            }

            // First get memos from server
            val remoteMemos = remoteDataSource.getAllMemos()

            // Saving locally (update or create)
            for (remoteMemo in remoteMemos) {
                val saved = localDataSource.saveMemo(remoteMemo)
                if (saved) {
                    Timber.d("Memo saved: ${remoteMemo.id}")
                }
            }

            // Sync local changes
            for (queueItem in syncQueue) {
                try {
                    when (queueItem.action) {
                        SyncAction.CREATE -> {
                            val memo = localDataSource.getMemoById(queueItem.memoId)
                            if (memo != null) {
                                val response = remoteDataSource.createMemo(memo.content)
                                val updatedMemo = memo.copy(
                                    syncStatus = SyncStatus.SYNCED,
                                    lastSyncTime = System.currentTimeMillis(),
                                    isLocalOnly = false,
                                    serverId = response.id ?: memo.serverId
                                )
                                localDataSource.saveMemo(updatedMemo)
                                Timber.d("Memo synced successfully: ${memo.id}")
                            }
                        }
                        SyncAction.UPDATE -> {
                            val memo = localDataSource.getMemoById(queueItem.memoId)
                            if (memo != null) {
                                remoteDataSource.updateMemo(memo.serverId, memo.content)
                                val updatedMemo = memo.copy(
                                    syncStatus = SyncStatus.SYNCED,
                                    lastSyncTime = System.currentTimeMillis()
                                )
                                localDataSource.saveMemo(updatedMemo)
                                Timber.d("Memo updated and synced: ${memo.id}")
                            }
                        }
                        SyncAction.DELETE -> {
                            remoteDataSource.deleteMemo(queueItem.memoId)
                            Timber.d("Memo deleted on server: ${queueItem.memoId}")
                        }
                    }
                    localDataSource.removeSyncQueueItem(queueItem.id)
                } catch (e: Exception) {
                    val memo = localDataSource.getMemoById(queueItem.memoId)
                    if (memo != null) {
                        val failedMemo = memo.copy(syncStatus = SyncStatus.FAILED)
                        localDataSource.saveMemo(failedMemo)
                        Timber.e(e, "Failed to sync memo: ${memo.id}")
                    }
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Sync with server failed")
            e.printStackTrace()
        }
    }
}
