package xyz.polyserv.notum.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import xyz.polyserv.notum.data.model.ListMemosResponse
import xyz.polyserv.notum.data.model.MemoRequest
import xyz.polyserv.notum.data.model.MemoResponse

interface MemosApiService {
    @GET("memos")
    suspend fun listMemos(): ListMemosResponse

    @GET("memos/{name}")
    suspend fun getMemo(@Path("name") name: String): MemoResponse

    @POST("memos")
    suspend fun createMemo(@Body request: MemoRequest): MemoResponse

    @PATCH("memos/{name}")
    suspend fun updateMemo(
        @Path("name") name: String,
        @Body request: MemoRequest
    ): MemoResponse

    @DELETE("memos/{name}")
    suspend fun deleteMemo(@Path("name") name: String)
}
