package xyz.polyserv.memos

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import xyz.polyserv.memos.sync.SyncScheduler
import timber.log.Timber

@HiltAndroidApp
class MemosApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncScheduler: SyncScheduler

    //override val getWorkManagerConfiguration: Configuration =
    override val workManagerConfiguration: Configuration =
        // Используем lazy инициализацию и проверку
        if (::workerFactory.isInitialized) {
            Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        } else {
            // Если ещё не инжектен, возвращаем дефолтную конфигурацию
            Configuration.Builder().build()
        }

    /*private val workManagerConfig: Configuration by lazy {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun workManagerConfiguration(): Configuration = workManagerConfig*/
    //override fun getWorkManagerConfiguration(): Configuration {
    //    return Configuration.Builder().build()
    //}

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Запланировать синхронизацию при старте
        syncScheduler.scheduleSyncWork(this)
    }
}
