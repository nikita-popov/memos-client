package xyz.polyserv.memos.data.remote

import xyz.polyserv.memos.data.model.Memo
import xyz.polyserv.memos.data.model.MemoRequest
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiService: MemosApiService
) {
    suspend fun getAllMemos(): List<Memo> {
        val response = apiService.listMemos()
        return response.memos.map { memoData ->
            Memo(
                id = memoData.name,
                content = memoData.content,
                createdTs = memoData.createdTs,
                updatedTs = memoData.updatedTs,
                resourceName = memoData.resourceName,
                rowStatus = memoData.rowStatus,
                isLocalOnly = false,
                serverId = memoData.name
            )
        }
    }

    suspend fun createMemo(content: String): Memo {
        val response = apiService.createMemo(MemoRequest(content = content))
        return response.memo?.let { memoData ->
            Memo(
                id = memoData.name,
                content = memoData.content,
                createdTs = memoData.createdTs,
                updatedTs = memoData.updatedTs,
                resourceName = memoData.resourceName,
                isLocalOnly = false,
                serverId = memoData.name
            )
        } ?: throw Exception("Failed to create memo")
    }

    suspend fun updateMemo(serverId: String, content: String): Memo {
        val response = apiService.updateMemo(serverId, MemoRequest(content = content))
        return response.memo?.let { memoData ->
            Memo(
                id = memoData.name,
                content = memoData.content,
                createdTs = memoData.createdTs,
                updatedTs = memoData.updatedTs,
                resourceName = memoData.resourceName,
                isLocalOnly = false,
                serverId = memoData.name
            )
        } ?: throw Exception("Failed to update memo")
    }

    suspend fun deleteMemo(serverId: String) =
        apiService.deleteMemo(serverId)
}
