package com.pronoagg.aggregator

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pronoagg.aggregator.data.DefaultChannels
import com.pronoagg.aggregator.data.local.AppDatabase
import com.pronoagg.aggregator.data.repository.PronoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PronoAggApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        schedulePeriodicRefresh()
        seedDefaultChannelsIfNeeded()
    }

    private fun schedulePeriodicRefresh() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<RefreshPronosWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RefreshPronosWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun seedDefaultChannelsIfNeeded() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return

        applicationScope.launch {
            val repository = PronoRepository(AppDatabase.getInstance(this@PronoAggApplication))
            DefaultChannels.SEED_USERNAMES.forEach { repository.addChannel(it) }
            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "prono_agg_prefs"
        private const val KEY_SEEDED = "seeded_default_channels_v1"
    }
}
