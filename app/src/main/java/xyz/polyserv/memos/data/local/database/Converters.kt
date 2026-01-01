package xyz.polyserv.memos.data.local.database

import androidx.room.TypeConverter
//import xyz.polyserv.memos.data.model.SyncOperation
import xyz.polyserv.memos.data.model.SyncStatus

enum class SyncAction(val value: String) {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE")
}

class Converters {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)

    //@TypeConverter
    //fun fromSyncOperation(value: SyncOperation): String = value.name

    //@TypeConverter
    //fun toSyncOperation(value: String): SyncOperation = SyncOperation.valueOf(value)

    @TypeConverter
    fun toSyncAction(value: String): SyncAction {
        return SyncAction.values().first { it.value == value }
    }

    @TypeConverter
    fun fromSyncAction(action: SyncAction): String {
        return action.value
    }

    // Для преобразования Long в String и обратно
    @TypeConverter
    fun fromTimestamp(value: Long?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toTimestamp(value: String?): Long? {
        return value?.toLongOrNull()
    }

    // Для SyncQueueItem action (если нужно)
    //@TypeConverter
    //fun fromSyncAction(value: String?): String? {
    //    return value
    //}

    //@TypeConverter
    //fun toSyncAction(value: String?): String? {
    //    return value
    //}
}
