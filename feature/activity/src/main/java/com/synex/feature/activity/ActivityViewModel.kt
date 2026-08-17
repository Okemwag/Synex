package com.synex.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.synex.core.data.SynexRepository
import com.synex.core.model.AccountTransactionUpdate
import com.synex.core.model.ActivityRow
import com.synex.core.ui.customerMessage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ActivityTab { TRANSACTIONS, TRADE_RESULTS }

data class ActivityUiState(
    val isLoading: Boolean = true,
    val hasAccount: Boolean = true,
    val tab: ActivityTab = ActivityTab.TRANSACTIONS,
    val rows: List<ActivityRow> = emptyList(),
    val count: Int = 0,
    val offset: Int = 0,
    val dateFrom: String = "",
    val dateTo: String = "",
    val actionType: String = "",
    val sort: String = "DESC",
    val errorMessage: String? = null,
) {
    val hasPrevious get() = offset > 0
    val hasNext get() = rows.size == PAGE_SIZE && (count <= rows.size || offset + rows.size < count)
}

const val PAGE_SIZE = 50

class ActivityViewModel(private val repository: SynexRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(ActivityUiState())
    val state: StateFlow<ActivityUiState> = mutableState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            repository.activeLoginId.collectLatest {
                mutableState.update { current -> current.copy(offset = 0) }
                refresh()
            }
        }
        viewModelScope.launch {
            repository.activeLoginId.filterNotNull().collectLatest { loginId ->
                repository.accountUpdates(loginId).collect { update ->
                    if (update is AccountTransactionUpdate) refresh()
                }
            }
        }
    }

    fun selectTab(tab: ActivityTab) {
        mutableState.update { it.copy(tab = tab, offset = 0, errorMessage = null) }
        refresh()
    }

    fun setDateFrom(value: String) = updateFilter { it.copy(dateFrom = value) }
    fun setDateTo(value: String) = updateFilter { it.copy(dateTo = value) }
    fun setActionType(value: String) = updateFilter { it.copy(actionType = value) }
    fun setSort(value: String) = updateFilter { it.copy(sort = value) }

    fun clearFilters() {
        mutableState.update { it.copy(dateFrom = "", dateTo = "", actionType = "", sort = "DESC", offset = 0) }
        refresh()
    }

    fun previous() {
        mutableState.update { it.copy(offset = (it.offset - PAGE_SIZE).coerceAtLeast(0)) }
        refresh()
    }

    fun next() {
        mutableState.update { it.copy(offset = it.offset + PAGE_SIZE) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val accounts = runCatching { repository.accounts() }.getOrElse {
                mutableState.update { state -> state.copy(isLoading = false, hasAccount = false, errorMessage = it.customerMessage("load activity")) }
                return@launch
            }
            if (accounts.isEmpty()) {
                mutableState.update { it.copy(isLoading = false, hasAccount = false, rows = emptyList()) }
                return@launch
            }
            val current = mutableState.value
            val fromEpoch = current.dateFrom.takeIf(String::isNotBlank)?.let { parseDate(it, endOfDay = false) }
            val toEpoch = current.dateTo.takeIf(String::isNotBlank)?.let { parseDate(it, endOfDay = true) }
            if (current.dateFrom.isNotBlank() && fromEpoch == null || current.dateTo.isNotBlank() && toEpoch == null) {
                mutableState.update { it.copy(errorMessage = "Use YYYY-MM-DD for activity dates.") }
                return@launch
            }
            if (fromEpoch != null && toEpoch != null && fromEpoch > toEpoch) {
                mutableState.update { it.copy(errorMessage = "The start date must be before the end date.") }
                return@launch
            }
            mutableState.update { it.copy(isLoading = true, hasAccount = true, errorMessage = null) }
            runCatching {
                if (current.tab == ActivityTab.TRANSACTIONS) {
                    repository.statement(current.offset, PAGE_SIZE, fromEpoch, toEpoch, current.actionType.ifBlank { null })
                } else {
                    repository.profitTable(
                        current.offset,
                        PAGE_SIZE,
                        current.dateFrom.ifBlank { null },
                        current.dateTo.ifBlank { null },
                        current.sort,
                    )
                }
            }.onSuccess { page ->
                val ordered = page.rows.sortedBy { it.epochSeconds }.let { if (current.sort == "DESC") it.reversed() else it }
                mutableState.update { it.copy(isLoading = false, rows = ordered, count = page.count) }
            }.onFailure { error ->
                mutableState.update { it.copy(isLoading = false, errorMessage = error.customerMessage("load activity")) }
            }
        }
    }

    private fun updateFilter(update: (ActivityUiState) -> ActivityUiState) {
        mutableState.update { update(it).copy(offset = 0) }
    }

    private fun parseDate(raw: String, endOfDay: Boolean): Long? = runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val date = parser.parse(raw.trim()) ?: return@runCatching null
        Calendar.getInstance().apply {
            time = date
            if (endOfDay) set(Calendar.HOUR_OF_DAY, 23)
            if (endOfDay) set(Calendar.MINUTE, 59)
            if (endOfDay) set(Calendar.SECOND, 59)
        }.timeInMillis / 1000
    }.getOrNull()

    companion object {
        fun factory(repository: SynexRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ActivityViewModel(repository) as T
        }
    }
}
