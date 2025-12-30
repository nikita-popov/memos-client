package xyz.polyserv.memos.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import xyz.polyserv.memos.data.model.SyncQueueItem

@Dao
interface SyncQueueDao {
    @Insert
    suspend fun insertSyncQueueItem(item: SyncQueueItem): Long

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getAllQueueItems(): List<SyncQueueItem>

    @Query("SELECT * FROM sync_queue WHERE id = :id")
    suspend fun getQueueItem(id: Int): SyncQueueItem?

    @Update
    suspend fun updateQueueItem(item: SyncQueueItem)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteQueueItem(id: Int)

    @Query("DELETE FROM sync_queue WHERE memoId = :memoId")
    suspend fun deleteQueueItemsByMemoId(memoId: String)

    @Query("SELECT COUNT(*) FROM sync_queue")
    suspend fun getQueueSize(): Int
}
