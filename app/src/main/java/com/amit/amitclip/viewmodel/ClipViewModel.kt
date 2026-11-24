package com.amit.amitclip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.amit.amitclip.AmitClipApp
import com.amit.amitclip.data.Clip
import com.amit.amitclip.data.ClipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClipViewModel(private val repository: ClipRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val clips: StateFlow<List<Clip>> = _query
        .flatMapLatest { repository.searchClips(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { _query.value = q }

    fun addClip(text: String) = viewModelScope.launch { repository.addClip(text) }

    fun delete(clip: Clip) = viewModelScope.launch { repository.delete(clip) }

    fun togglePin(clip: Clip) = viewModelScope.launch { repository.togglePin(clip) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as AmitClipApp
                ClipViewModel(app.repository)
            }
        }
    }
}
