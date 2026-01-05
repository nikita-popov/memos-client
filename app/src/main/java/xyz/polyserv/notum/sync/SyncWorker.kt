package xyz.polyserv.notum.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import xyz.polyserv.notum.data.repository.MemoRepository

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncWorkerEntryPoint {
        fun getMemoRepository(): MemoRepository
    }

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Sync started")

            // Getting EntryPoint and get Repository
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                SyncWorkerEntryPoint::class.java
            )
            val repository = entryPoint.getMemoRepository()

            // Call sync
            repository.syncWithServer()

            Timber.d("Sync successfully finished")
            Result.success()
        } catch (e: Exception) {
            Timber.e("Sync failed: ${e.message}")
            Result.retry()
        }
    }
}
