package xyz.polyserv.memos.data.local

import xyz.polyserv.memos.data.local.database.MemoDao
import xyz.polyserv.memos.data.local.database.SyncQueueDao
import xyz.polyserv.memos.data.model.Memo
import xyz.polyserv.memos.data.model.SyncQueueItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val memoDao: MemoDao,
    private val syncQueueDao: SyncQueueDao
) {
    fun getAllMemosFlow(): Flow<List<Memo>> = memoDao.getAllMemosFlow()

    fun searchMemos(query: String): Flow<List<Memo>> = memoDao.searchMemos(query)

    suspend fun getMemoById(id: String): Memo? = memoDao.getMemoById(id)

    suspend fun saveMemo(memo: Memo) {
        val existing = memoDao.getMemoById(memo.id)
        if (existing != null) {
            memoDao.updateMemo(memo)
        } else {
            memoDao.insertMemo(memo)
        }
    }

    suspend fun deleteMemo(id: String) = memoDao.deleteMemoById(id)

    suspend fun addToSyncQueue(item: SyncQueueItem) =
        syncQueueDao.insertSyncQueueItem(item)

    suspend fun getSyncQueue(): List<SyncQueueItem> =
        syncQueueDao.getAllQueueItems()

    suspend fun removeSyncQueueItem(id: Int) =
        syncQueueDao.deleteQueueItem(id)

    suspend fun clearMemoSyncQueue(memoId: String) =
        syncQueueDao.deleteQueueItemsByMemoId(memoId)

    suspend fun updateMemoSyncStatus(id: String, status: xyz.polyserv.memos.data.model.SyncStatus, time: Long) {
        memoDao.updateMemoSyncStatus(id, status, time)
    }
}
