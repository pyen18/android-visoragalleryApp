package com.example.visoragallery.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.visoragallery.data.PhotoDataHolder
import com.example.visoragallery.ui.components.PhotoGrid
import com.example.visoragallery.ui.screens.search.SearchUiState
import com.example.visoragallery.ui.screens.search.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val selectedPhotos by viewModel.selectedPhotos.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedPhotos.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.exitSelectionMode() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Exit selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selectAllPhotos() }) {
                            Icon(Icons.Filled.SelectAll, contentDescription = "Select all")
                        }
                        IconButton(onClick = { /* TODO: Share */ }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("Search Photos") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onClear = { viewModel.clearSearch() },
                onFilterClick = { showFilterDialog = true },
                hasActiveFilters = searchFilter.dateFrom != null ||
                        searchFilter.dateTo != null ||
                        searchFilter.minSize != null ||
                        searchFilter.maxSize != null,
                onSearch = {
                    keyboardController?.hide()
                    viewModel.performSearch()
                }
            )

            // Active Filters Chips
            if (searchFilter.dateFrom != null || searchFilter.dateTo != null ||
                searchFilter.minSize != null || searchFilter.maxSize != null) {
                ActiveFiltersRow(
                    dateRange = viewModel.getFormattedDateRange(),
                    sizeRange = viewModel.getFormattedSizeRange(),
                    onClearAll = { viewModel.clearFilters() }
                )
            }

            // Content
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    RecentSearches(
                        searches = recentSearches,
                        onSearchClick = { viewModel.selectRecentSearch(it) },
                        onClearAll = { viewModel.clearRecentSearches() }
                    )
                }

                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is SearchUiState.Success -> {
                    if (state.photos.isEmpty()) {
                        EmptySearchResult(query = state.query)
                    } else {
                        Column {
                            Text(
                                text = "${state.photos.size} photo${if (state.photos.size > 1) "s" else ""} found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(16.dp)
                            )

                            PhotoGrid(
                                photos = state.photos,
                                spanCount = 4,
                                selectedPhotos = selectedPhotos,
                                isSelectionMode = isSelectionMode,
                                onPhotoClick = { photo, index ->
                                    if (isSelectionMode) {
                                        viewModel.togglePhotoSelection(photo.path)
                                    } else {
                                        // Navigate to single photo
                                        val photoPaths = state.photos.map { it.path }.toTypedArray()
                                        PhotoDataHolder.setPhotoPaths(photoPaths)
                                        // TODO: Navigate to single photo screen
                                    }
                                },
                                onPhotoLongClick = { photo ->
                                    if (!isSelectionMode) {
                                        viewModel.enterSelectionMode()
                                    }
                                    viewModel.togglePhotoSelection(photo.path)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                is SearchUiState.Error -> {
                    ErrorState(message = state.message)
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            currentFilter = searchFilter,
            onDismiss = { showFilterDialog = false },
            onApply = { dateFrom, dateTo, minSize, maxSize ->
                viewModel.setDateFilter(dateFrom, dateTo)
                viewModel.setSizeFilter(minSize, maxSize)
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search photos...") },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            shape = RoundedCornerShape(12.dp)
        )

        Badge(
            containerColor = if (hasActiveFilters)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            IconButton(onClick = onFilterClick) {
                Icon(
                    Icons.Filled.FilterList,
                    contentDescription = "Filters",
                    tint = if (hasActiveFilters)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ActiveFiltersRow(
    dateRange: String,
    sizeRange: String,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dateRange.isNotEmpty()) {
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text(dateRange) },
                leadingIcon = { Icon(Icons.Filled.CalendarToday, null, modifier = Modifier.size(16.dp)) }
            )
        }

        if (sizeRange.isNotEmpty()) {
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text(sizeRange) },
                leadingIcon = { Icon(Icons.Filled.Storage, null, modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onClearAll) {
            Text("Clear All")
        }
    }
}

@Composable
fun RecentSearches(
    searches: List<String>,
    onSearchClick: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (searches.isEmpty()) {
            EmptyRecentSearches()
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onClearAll) {
                    Text("Clear All")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(searches) { search ->
                    RecentSearchItem(
                        search = search,
                        onClick = { onSearchClick(search) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecentSearchItem(
    search: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = search,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Icon(
            Icons.Filled.NorthWest,
            contentDescription = "Use search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun EmptyRecentSearches() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No recent searches",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun EmptySearchResult(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No results found",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "No photos match \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Search Error",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FilterDialog(
    currentFilter: com.example.visoragallery.ui.screens.search.SearchFilter,
    onDismiss: () -> Unit,
    onApply: (dateFrom: Long?, dateTo: Long?, minSize: Long?, maxSize: Long?) -> Unit
) {
    var dateFrom by remember { mutableStateOf(currentFilter.dateFrom) }
    var dateTo by remember { mutableStateOf(currentFilter.dateTo) }
    var minSize by remember { mutableStateOf(currentFilter.minSize) }
    var maxSize by remember { mutableStateOf(currentFilter.maxSize) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Photos") },
        text = {
            Column {
                Text(
                    "Advanced filters coming soon!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• Date range picker\n• Size range slider\n• File type filter",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(dateFrom, dateTo, minSize, maxSize) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}