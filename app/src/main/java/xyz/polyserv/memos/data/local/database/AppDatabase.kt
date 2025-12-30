package xyz.polyserv.memos.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xyz.polyserv.memos.data.model.Memo
import xyz.polyserv.memos.data.model.SyncQueueItem

@Database(
    entities = [Memo::class, SyncQueueItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun syncQueueDao(): SyncQueueDao
}
