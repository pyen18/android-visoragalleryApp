package com.example.visoragallery.ui.screens.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.visoragallery.data.PhotoItem
import com.example.visoragallery.repository.PhotoRepository
import com.example.visoragallery.settings.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class SearchFilter(
    val query: String = "",
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
    val minSize: Long? = null,
    val maxSize: Long? = null
)

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val photos: List<PhotoItem>, val query: String) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PhotoRepository(application)
    private val appConfig = AppConfig.getInstance(application)

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter())
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    private val _selectedPhotos = MutableStateFlow<Set<String>>(emptySet())
    val selectedPhotos: StateFlow<Set<String>> = _selectedPhotos.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private var allPhotos: List<PhotoItem> = emptyList()

    init {
        loadRecentSearches()
        loadAllPhotos()
    }

    private fun loadAllPhotos() {
        viewModelScope.launch {
            try {
                repository.getAllPhotos().collect { photos ->
                    allPhotos = photos
                }
            } catch (e: Exception) {
                // Silently fail, photos will be empty
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query

        if (query.isEmpty()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        // Update filter
        _searchFilter.value = _searchFilter.value.copy(query = query)

        // Perform search
        performSearch()
    }

    fun performSearch() {
        val filter = _searchFilter.value

        if (filter.query.isEmpty() &&
            filter.dateFrom == null &&
            filter.dateTo == null &&
            filter.minSize == null &&
            filter.maxSize == null) {
            _uiState.value = SearchUiState.Idle
            return
        }

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading

            try {
                val results = allPhotos.filter { photo ->
                    var matches = true

                    // Filter by query (filename)
                    if (filter.query.isNotEmpty()) {
                        matches = matches && photo.name.contains(filter.query, ignoreCase = true)
                    }

                    // Filter by date range
                    if (filter.dateFrom != null) {
                        matches = matches && photo.lastModified >= filter.dateFrom
                    }
                    if (filter.dateTo != null) {
                        matches = matches && photo.lastModified <= filter.dateTo
                    }

                    // Filter by size range
                    if (filter.minSize != null) {
                        matches = matches && photo.size >= filter.minSize
                    }
                    if (filter.maxSize != null) {
                        matches = matches && photo.size <= filter.maxSize
                    }

                    matches
                }

                _uiState.value = SearchUiState.Success(results, filter.query)

                // Save to recent searches
                if (filter.query.isNotEmpty()) {
                    saveRecentSearch(filter.query)
                }

            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Search failed")
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchFilter.value = SearchFilter()
        _uiState.value = SearchUiState.Idle
        exitSelectionMode()
    }

    fun setDateFilter(dateFrom: Long?, dateTo: Long?) {
        _searchFilter.value = _searchFilter.value.copy(
            dateFrom = dateFrom,
            dateTo = dateTo
        )
        performSearch()
    }

    fun setSizeFilter(minSize: Long?, maxSize: Long?) {
        _searchFilter.value = _searchFilter.value.copy(
            minSize = minSize,
            maxSize = maxSize
        )
        performSearch()
    }

    fun clearFilters() {
        val currentQuery = _searchFilter.value.query
        _searchFilter.value = SearchFilter(query = currentQuery)
        performSearch()
    }

    fun selectRecentSearch(query: String) {
        _searchQuery.value = query
        onSearchQueryChange(query)
    }

    private fun loadRecentSearches() {
        // Load from SharedPreferences
        val prefs = getApplication<Application>().getSharedPreferences("search_prefs", 0)
        val searches = prefs.getStringSet("recent_searches", emptySet())?.toList() ?: emptyList()
        _recentSearches.value = searches.take(10) // Limit to 10
    }

    private fun saveRecentSearch(query: String) {
        val prefs = getApplication<Application>().getSharedPreferences("search_prefs", 0)
        val current = prefs.getStringSet("recent_searches", emptySet())?.toMutableSet() ?: mutableSetOf()

        // Add to top
        current.remove(query)
        current.add(query)

        // Save
        prefs.edit().putStringSet("recent_searches", current.take(10).toSet()).apply()

        // Update state
        _recentSearches.value = current.take(10).toList()
    }

    fun clearRecentSearches() {
        val prefs = getApplication<Application>().getSharedPreferences("search_prefs", 0)
        prefs.edit().remove("recent_searches").apply()
        _recentSearches.value = emptyList()
    }

    // Selection mode functions
    fun togglePhotoSelection(photoPath: String) {
        val currentSelection = _selectedPhotos.value.toMutableSet()
        if (currentSelection.contains(photoPath)) {
            currentSelection.remove(photoPath)
        } else {
            currentSelection.add(photoPath)
        }
        _selectedPhotos.value = currentSelection

        if (currentSelection.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun enterSelectionMode() {
        _isSelectionMode.value = true
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedPhotos.value = emptySet()
    }

    fun selectAllPhotos() {
        val state = _uiState.value
        if (state is SearchUiState.Success) {
            _selectedPhotos.value = state.photos.map { it.path }.toSet()
        }
    }

    fun getFormattedDateRange(): String {
        val filter = _searchFilter.value
        if (filter.dateFrom == null && filter.dateTo == null) return ""

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        return when {
            filter.dateFrom != null && filter.dateTo != null -> {
                "${dateFormat.format(Date(filter.dateFrom))} - ${dateFormat.format(Date(filter.dateTo))}"
            }
            filter.dateFrom != null -> {
                "From ${dateFormat.format(Date(filter.dateFrom))}"
            }
            filter.dateTo != null -> {
                "Until ${dateFormat.format(Date(filter.dateTo))}"
            }
            else -> ""
        }
    }

    fun getFormattedSizeRange(): String {
        val filter = _searchFilter.value
        if (filter.minSize == null && filter.maxSize == null) return ""

        fun formatSize(bytes: Long): String {
            return when {
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            }
        }

        return when {
            filter.minSize != null && filter.maxSize != null -> {
                "${formatSize(filter.minSize)} - ${formatSize(filter.maxSize)}"
            }
            filter.minSize != null -> {
                "Min ${formatSize(filter.minSize)}"
            }
            filter.maxSize != null -> {
                "Max ${formatSize(filter.maxSize)}"
            }
            else -> ""
        }
    }
}