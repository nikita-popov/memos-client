package xyz.polyserv.notum.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xyz.polyserv.notum.data.model.Memo
import xyz.polyserv.notum.data.model.SyncQueueItem

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
