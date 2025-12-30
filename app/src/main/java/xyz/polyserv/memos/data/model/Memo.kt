package xyz.polyserv.memos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

enum class SyncStatus {
    SYNCED, PENDING, SYNCING, FAILED
}

@Entity(tableName = "memos")
@Serializable
data class Memo(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val content: String,
    val resourceName: String = "",
    val createdTs: Long = System.currentTimeMillis(),
    val updatedTs: Long = System.currentTimeMillis(),
    val rowStatus: String = "NORMAL", // NORMAL, ARCHIVED

    // Offline fields
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncTime: Long = 0,
    val isLocalOnly: Boolean = true,
    val serverId: String = ""
)

@Serializable
data class MemoRequest(
    val content: String,

    @SerialName("resource_name")
    val resourceName: String = ""
)

@Serializable
data class MemoResponse(
    val memo: MemoData? = null
)

@Serializable
data class MemoData(
    val name: String = "",
    val uid: String = "",
    val content: String = "",
    @SerialName("resource_name")
    val resourceName: String = "",
    @SerialName("created_ts")
    val createdTs: Long = 0,
    @SerialName("updated_ts")
    val updatedTs: Long = 0,
    @SerialName("row_status")
    val rowStatus: String = "NORMAL"
)

@Serializable
data class ListMemosResponse(
    val memos: List<MemoData> = emptyList()
)
