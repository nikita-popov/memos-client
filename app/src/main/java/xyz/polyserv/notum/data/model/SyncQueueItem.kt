package xyz.polyserv.notum.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncAction {
    CREATE, UPDATE, DELETE
}

@Entity(tableName = "sync_queue")
data class SyncQueueItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val memoId: String,
    val action: SyncAction,
    val payload: String, // JSON Memo
    val createdAt: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val maxRetries: Int = 3
)
