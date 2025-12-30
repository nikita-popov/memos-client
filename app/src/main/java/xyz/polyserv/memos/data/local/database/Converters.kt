package xyz.polyserv.memos.data.local.database

import androidx.room.TypeConverter
import xyz.polyserv.memos.data.model.SyncOperation
import xyz.polyserv.memos.data.model.SyncStatus

class Converters {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)

    @TypeConverter
    fun fromSyncOperation(value: SyncOperation): String = value.name

    @TypeConverter
    fun toSyncOperation(value: String): SyncOperation = SyncOperation.valueOf(value)
}
