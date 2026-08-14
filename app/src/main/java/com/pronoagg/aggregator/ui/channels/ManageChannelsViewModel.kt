package com.pronoagg.aggregator.ui.channels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pronoagg.aggregator.data.local.AppDatabase
import com.pronoagg.aggregator.data.local.ChannelEntity
import com.pronoagg.aggregator.data.repository.PronoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ManageChannelsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PronoRepository(AppDatabase.getInstance(application))

    val channels: StateFlow<List<ChannelEntity>> = repository.channels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAdding = MutableStateFlow(false)
    val isAdding: StateFlow<Boolean> = _isAdding.asStateFlow()

    fun addChannel(rawUsername: String) {
        if (rawUsername.isBlank()) return
        viewModelScope.launch {
            _isAdding.value = true
            try {
                repository.addChannel(rawUsername)
            } finally {
                _isAdding.value = false
            }
        }
    }

    fun removeChannel(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.removeChannel(channel)
        }
    }
}
