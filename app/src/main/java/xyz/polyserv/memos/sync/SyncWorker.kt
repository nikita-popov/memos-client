package xyz.polyserv.memos.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import xyz.polyserv.memos.data.repository.MemoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val memoRepository: MemoRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        Timber.d("Starting sync work")
        memoRepository.syncPendingChanges()
        Timber.d("Sync completed successfully")
        Result.success()
    } catch (e: Exception) {
        Timber.e(e, "Sync failed")
        Result.retry()
    }
}
