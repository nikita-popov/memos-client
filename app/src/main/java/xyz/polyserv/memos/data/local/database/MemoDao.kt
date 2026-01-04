package xyz.polyserv.memos.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import xyz.polyserv.memos.data.model.Memo
import xyz.polyserv.memos.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos ORDER BY updateTime DESC")
    fun getAllMemosFlow(): Flow<List<Memo>>

    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getMemoById(id: String): Memo?

    @Query("SELECT * FROM memos WHERE content LIKE '%' || :query || '%'")
    fun searchMemos(query: String): Flow<List<Memo>>

    @Insert
    suspend fun insertMemo(memo: Memo): Long

    @Update
    suspend fun updateMemo(memo: Memo)

    @Delete
    suspend fun deleteMemo(memo: Memo)

    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun deleteMemoById(id: String)

    @Query("SELECT * FROM memos WHERE syncStatus = :status")
    suspend fun getMemosBySyncStatus(status: SyncStatus): List<Memo>

    @Query("UPDATE memos SET syncStatus = :status, lastSyncTime = :time WHERE id = :id")
    suspend fun updateMemoSyncStatus(id: String, status: SyncStatus, time: Long)

    @Query("UPDATE memos SET isLocalOnly = :isLocal WHERE id = :id")
    suspend fun updateIsLocalOnly(id: String, isLocal: Boolean)

    @Query("DELETE FROM memos WHERE isLocalOnly = 1 AND syncStatus = :status")
    suspend fun deleteLocalOnlyFailed(status: SyncStatus)
}
