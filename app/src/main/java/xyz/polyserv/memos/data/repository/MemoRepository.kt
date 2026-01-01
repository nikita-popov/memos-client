package xyz.polyserv.memos.data.repository

import kotlinx.coroutines.flow.Flow
import xyz.polyserv.memos.data.local.LocalDataSource
import xyz.polyserv.memos.data.remote.RemoteDataSource
import xyz.polyserv.memos.data.model.Memo
import xyz.polyserv.memos.data.model.SyncQueueItem
import xyz.polyserv.memos.data.model.SyncAction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) {

    // Получить все заметки локально
    fun getMemos(): Flow<List<Memo>> = localDataSource.getAllMemosFlow()

    // Добавить новую заметку
    suspend fun addMemo(memo: Memo) {
        localDataSource.saveMemo(memo)
        localDataSource.addToSyncQueue(
            SyncQueueItem(
                memoId = memo.id,
                action = SyncAction.CREATE,
                payload = memo.content,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Обновить заметку
    suspend fun updateMemo(memo: Memo) {
        localDataSource.saveMemo(memo)
        localDataSource.addToSyncQueue(
            SyncQueueItem(
                memoId = memo.id,
                action = SyncAction.UPDATE,
                payload = memo.content,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Удалить заметку
    suspend fun deleteMemo(memoId: String) {
        localDataSource.deleteMemo(memoId)
        localDataSource.addToSyncQueue(
            SyncQueueItem(
                memoId = memoId,
                action = SyncAction.DELETE,
                payload = "",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun searchMemos(query: String): Flow<List<Memo>> =  localDataSource.searchMemos(query)

    // 👇 ГЛАВНЫЙ МЕТОД СИНХРОНИЗАЦИИ 👇
    suspend fun syncWithServer() {
        try {
            // 1️⃣ Сначала загружаем свежие заметки с сервера
            val remoteMemos = remoteDataSource.getAllMemos()

            // Сохраняем их локально (обновляем или вставляем)
            for (remoteMemo in remoteMemos) {
                val existingMemo = localDataSource.getMemoById(remoteMemo.id)
                if (existingMemo != null) {
                    // Если заметка уже существует, обновляем только если серверная версия новее
                    if (remoteMemo.updatedTs > existingMemo.updatedTs) {
                        localDataSource.saveMemo(remoteMemo)
                    }
                } else {
                    // Если заметки нет, вставляем новую
                    localDataSource.saveMemo(remoteMemo)
                }
            }

            // 2️⃣ Затем синхронизируем очередь локальных изменений
            val syncQueue = localDataSource.getSyncQueue()

            for (queueItem in syncQueue) {
                try {
                    when (queueItem.action) {
                        SyncAction.CREATE -> {
                            val memo = localDataSource.getMemoById(queueItem.memoId)
                            if (memo != null) {
                                remoteDataSource.createMemo(memo.content)
                            }
                        }
                        SyncAction.UPDATE -> {
                            val memo = localDataSource.getMemoById(queueItem.memoId)
                            if (memo != null) {
                                remoteDataSource.updateMemo(memo.serverId, memo.content)
                            }
                        }
                        SyncAction.DELETE -> {
                            remoteDataSource.deleteMemo(queueItem.memoId)
                        }
                    }
                    // Если успешно отправилось, удаляем из очереди
                    localDataSource.removeSyncQueueItem(queueItem.id)
                } catch (e: Exception) {
                    // Если ошибка, оставляем в очереди (повторим позже)
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            // Ошибка при загрузке с сервера - просто логируем и продолжаем
            e.printStackTrace()
        }
    }
}
