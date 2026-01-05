package xyz.polyserv.notum.data.local

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import xyz.polyserv.notum.data.local.database.MemoDao
import xyz.polyserv.notum.data.local.database.SyncQueueDao
import xyz.polyserv.notum.data.model.Memo
import xyz.polyserv.notum.data.model.SyncQueueItem

class LocalDataSource @Inject constructor(
    private val memoDao: MemoDao,
    private val syncQueueDao: SyncQueueDao
) {
    fun getAllMemosFlow(): Flow<List<Memo>> = memoDao.getAllMemosFlow()

    fun searchMemos(query: String): Flow<List<Memo>> = memoDao.searchMemos(query)

    suspend fun getMemoById(id: String): Memo? = memoDao.getMemoById(id)

    suspend fun getMemoByName(name: String): Memo? {
        return memoDao.getMemoByName(name)
    }

    suspend fun saveMemo(memo: Memo): Boolean {
        val existing = memoDao.getMemoById(memo.id)
        if (existing != null) {
            // Update existing memo
            val existingTs = existing.getUpdateTimestamp()
            val newTs = memo.getUpdateTimestamp()

            Timber.d("saveMemo - Comparing timestamps:")
            Timber.d("  Existing: $existingTs (${existing.updateTime})")
            Timber.d("  New:      $newTs (${memo.updateTime})")

            if (existingTs <= newTs) {
                memoDao.updateMemo(memo)
                Timber.d("Memo updated: ${memo.id}")
                return true
            } else {
                Timber.d("Memo is older than existing, skipping: ${memo.id}")
                return false
            }
        } else {
            // Insert new memo
            memoDao.insertMemo(memo)
            Timber.d("Memo inserted: ${memo.id}")
            return true
        }
    }

    suspend fun deleteMemo(id: String) {
        memoDao.deleteMemoById(id)
        Timber.d("Memo deleted: $id")
    }

    suspend fun addToSyncQueue(item: SyncQueueItem) {
        syncQueueDao.insertSyncQueueItem(item)
    }

    suspend fun getSyncQueue(): List<SyncQueueItem> {
        return syncQueueDao.getAllQueueItems()
    }

    suspend fun removeSyncQueueItem(id: Int) {
        syncQueueDao.deleteQueueItem(id)
    }

    suspend fun clearMemoSyncQueue(memoId: String) {
        syncQueueDao.deleteQueueItemsByMemoId(memoId)
    }

    suspend fun updateMemoSyncStatus(
        id: String,
        status: xyz.polyserv.notum.data.model.SyncStatus,
        time: Long
    ) {
        memoDao.updateMemoSyncStatus(id, status, time)
    }
}
