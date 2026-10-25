package com.example.bh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class UiState(
    val loading: Boolean = true,
    val error: String? = null,
    val channels: List<Channel> = emptyList()
)

class MainViewModel : ViewModel() {
    private val _all = MutableStateFlow<List<Channel>>(emptyList())
    private val _query = MutableStateFlow("")
    private val _country = MutableStateFlow<String?>(null)
    private val _loading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    private val _countries = MutableStateFlow<List<String>>(emptyList())
    val countries: StateFlow<List<String>> = _countries.asStateFlow()

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_all, _query, _country, _loading, _error) { all, q, c, loading, err ->
                val filtered = all.filter { ch ->
                    (q.isBlank() || ch.name.contains(q, ignoreCase = true)) &&
                        (c == null || ch.country == c)
                }
                UiState(loading = loading, error = err, channels = filtered)
            }.collect { _state.value = it }
        }

        viewModelScope.launch {
            _all.collect { list ->
                _countries.value = list.mapNotNull { it.country }.distinct().sorted()
            }
        }

        load()
    }

    fun load(force: Boolean = false) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            runCatching { IptvRepository.loadChannels(force) }
                .onSuccess { _all.value = it }
                .onFailure { _error.value = it.message ?: "Error al cargar canales" }
            _loading.value = false
        }
    }

    fun setQuery(q: String) { _query.value = q }
    fun setCountry(c: String?) { _country.value = c }
}
