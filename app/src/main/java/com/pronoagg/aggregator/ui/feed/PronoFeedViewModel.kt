package com.pronoagg.aggregator.ui.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pronoagg.aggregator.data.PronoClassifier
import com.pronoagg.aggregator.data.local.AppDatabase
import com.pronoagg.aggregator.data.local.PronoEntity
import com.pronoagg.aggregator.data.local.PronoOutcome
import com.pronoagg.aggregator.data.repository.PronoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PronoFeedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PronoRepository(AppDatabase.getInstance(application))

    val pronos: StateFlow<List<PronoEntity>> = repository.feed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showOnlyPronos = MutableStateFlow(true)
    val showOnlyPronos: StateFlow<Boolean> = _showOnlyPronos.asStateFlow()

    val visiblePronos: StateFlow<List<PronoEntity>> = combine(pronos, showOnlyPronos) { list, onlyPronos ->
        if (onlyPronos) list.filter { PronoClassifier.looksLikeProno(it.text) } else list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshAll()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun setOutcome(pronoId: Long, outcome: PronoOutcome) {
        viewModelScope.launch {
            repository.setOutcome(pronoId, outcome)
        }
    }

    fun toggleShowOnlyPronos() {
        _showOnlyPronos.value = !_showOnlyPronos.value
    }
}
