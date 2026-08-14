package com.pronoagg.aggregator.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pronoagg.aggregator.data.local.AppDatabase
import com.pronoagg.aggregator.data.local.ChannelStatsRow
import com.pronoagg.aggregator.data.repository.PronoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PronoRepository(AppDatabase.getInstance(application))

    val stats: StateFlow<List<ChannelStatsRow>> = repository.channelStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
