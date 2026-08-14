package com.pronoagg.aggregator.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pronoagg.aggregator.data.local.AppDatabase
import com.pronoagg.aggregator.data.repository.PronoRepository

class RefreshPronosWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = PronoRepository(AppDatabase.getInstance(applicationContext))
            repository.refreshAll()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "refresh_pronos_periodic"
    }
}
