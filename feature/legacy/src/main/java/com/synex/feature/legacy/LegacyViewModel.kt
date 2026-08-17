package com.synex.feature.legacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.synex.core.data.SynexRepository
import com.synex.core.model.ActivityRow
import com.synex.core.ui.customerMessage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 100

data class LegacyUiState(
    val isLoading: Boolean = true,
    val migrationStatus: String = "unknown",
    val loginIds: List<String> = emptyList(),
    val selectedLoginId: String = "",
    val rows: List<ActivityRow> = emptyList(),
    val count: Int = 0,
    val offset: Int = 0,
    val dateFrom: String = "",
    val dateTo: String = "",
    val actionType: String = "",
    val errorMessage: String? = null,
) {
    val hasPrevious get() = offset > 0
    val hasNext get() = rows.size == PAGE_SIZE && (count <= rows.size || offset + rows.size < count)
}

class LegacyViewModel(private val repository: SynexRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(LegacyUiState())
    val state: StateFlow<LegacyUiState> = mutableState.asStateFlow()

    init { loadSummary() }

    fun setLoginId(value: String) { mutableState.update { it.copy(selectedLoginId = value, offset = 0) }; refresh() }
    fun setActionType(value: String) = mutableState.update { it.copy(actionType = value, offset = 0) }
    fun setDateFrom(value: String) = mutableState.update { it.copy(dateFrom = value, offset = 0) }
    fun setDateTo(value: String) = mutableState.update { it.copy(dateTo = value, offset = 0) }
    fun previous() { mutableState.update { it.copy(offset = (it.offset - PAGE_SIZE).coerceAtLeast(0)) }; refresh() }
    fun next() { mutableState.update { it.copy(offset = it.offset + PAGE_SIZE) }; refresh() }

    fun loadSummary() {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.legacyAccountSummary() }.fold(
                onSuccess = { summary ->
                    val selected = mutableState.value.selectedLoginId.takeIf(summary.loginIds::contains) ?: summary.loginIds.firstOrNull().orEmpty()
                    mutableState.update { it.copy(isLoading = false, migrationStatus = summary.migrationStatus, loginIds = summary.loginIds, selectedLoginId = selected) }
                    if (selected.isNotBlank()) refresh()
                },
                onFailure = { error -> mutableState.update { it.copy(isLoading = false, errorMessage = error.customerMessage("load legacy history")) } },
            )
        }
    }

    fun refresh() {
        val current = state.value
        if (current.selectedLoginId.isBlank()) return
        val from = current.dateFrom.takeIf(String::isNotBlank)?.let { parseDate(it, false) }
        val to = current.dateTo.takeIf(String::isNotBlank)?.let { parseDate(it, true) }
        if (current.dateFrom.isNotBlank() && from == null || current.dateTo.isNotBlank() && to == null) {
            mutableState.update { it.copy(errorMessage = "Use YYYY-MM-DD for legacy history dates.") }
            return
        }
        if (from != null && to != null && from > to) {
            mutableState.update { it.copy(errorMessage = "The start date must be before the end date.") }
            return
        }
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.legacyStatement(current.selectedLoginId, current.offset, PAGE_SIZE, from, to, current.actionType.ifBlank { null }) }.fold(
                onSuccess = { page -> mutableState.update { it.copy(isLoading = false, rows = page.rows.sortedByDescending(ActivityRow::epochSeconds), count = page.count) } },
                onFailure = { error -> mutableState.update { it.copy(isLoading = false, errorMessage = error.customerMessage("load the legacy statement")) } },
            )
        }
    }

    private fun parseDate(raw: String, endOfDay: Boolean): Long? = runCatching {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(raw.trim()) ?: return@runCatching null
        Calendar.getInstance().apply { time = parsed; if (endOfDay) { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) } }.timeInMillis / 1000
    }.getOrNull()

    companion object {
        fun factory(repository: SynexRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = LegacyViewModel(repository) as T
        }
    }
}
