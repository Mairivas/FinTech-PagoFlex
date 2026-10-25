package com.example.fintechpagoflex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fintechpagoflex.data.AppDao
import com.example.fintechpagoflex.data.Recommendation
import com.example.fintechpagoflex.data.Reminder
import com.example.fintechpagoflex.util.validateRecommendForm
import com.example.fintechpagoflex.util.validateReminderForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class RecommendUiState(
    val orgName: String = "",
    val orgType: String = "Colegio",
    val reason: String = "",
    val orgNameError: String? = null,
    val orgTypeError: String? = null,
    val isSuccess: Boolean = false
)

class RecommendViewModel(private val dao: AppDao) : ViewModel() {
    private val _uiState = MutableStateFlow(RecommendUiState())
    val uiState: StateFlow<RecommendUiState> = _uiState.asStateFlow()

    val recommendations: StateFlow<List<Recommendation>> = dao.getAllRecommendations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onOrgNameChanged(name: String) {
        _uiState.update { current ->
            val validation = validateRecommendForm(name, current.orgType)
            current.copy(
                orgName = name,
                orgNameError = if (current.orgNameError != null) validation.orgNameError else null,
                isSuccess = false
            )
        }
    }

    fun onOrgTypeChanged(type: String) {
        _uiState.update { current ->
            val validation = validateRecommendForm(current.orgName, type)
            current.copy(
                orgType = type,
                orgTypeError = if (current.orgTypeError != null) validation.orgTypeError else null,
                isSuccess = false
            )
        }
    }

    fun onReasonChanged(reason: String) {
        _uiState.update { current ->
            current.copy(reason = reason, isSuccess = false)
        }
    }

    fun submitRecommendation() {
        val currentState = _uiState.value
        val validation = validateRecommendForm(currentState.orgName, currentState.orgType)
        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    orgNameError = validation.orgNameError,
                    orgTypeError = validation.orgTypeError,
                    isSuccess = false
                )
            }
            return
        }
        viewModelScope.launch {
            dao.insertRecommendation(
                Recommendation(
                    organizationName = currentState.orgName,
                    type = currentState.orgType,
                    reason = currentState.reason
                )
            )
            _uiState.update {
                RecommendUiState(
                    orgName = "",
                    orgType = "Colegio",
                    reason = "",
                    isSuccess = true
                )
            }
        }
    }

    class Factory(private val dao: AppDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecommendViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecommendViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class ReminderSetupUiState(
    val dateTimeMillis: Long = Calendar.getInstance().apply { add(Calendar.MINUTE, 5) }.timeInMillis,
    val message: String = "",
    val dateTimeError: String? = null,
    val messageError: String? = null,
    val isSaved: Boolean = false
)

class ReminderViewModel(private val dao: AppDao) : ViewModel() {
    private val _uiState = MutableStateFlow(ReminderSetupUiState())
    val uiState: StateFlow<ReminderSetupUiState> = _uiState.asStateFlow()

    val reminders: StateFlow<List<Reminder>> = dao.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onDateTimeChanged(dateTimeMillis: Long) {
        _uiState.update { current ->
            val validation = validateReminderForm(dateTimeMillis, current.message)
            current.copy(
                dateTimeMillis = dateTimeMillis,
                dateTimeError = if (current.dateTimeError != null) validation.dateTimeError else null,
                isSaved = false
            )
        }
    }

    fun onMessageChanged(message: String) {
        _uiState.update { current ->
            val validation = validateReminderForm(current.dateTimeMillis, message)
            current.copy(
                message = message,
                messageError = if (current.messageError != null) validation.messageError else null,
                isSaved = false
            )
        }
    }

    fun saveReminder(commitmentId: Long, onReminderSaved: (Long, Long, String) -> Unit) {
        val currentState = _uiState.value
        val validation = validateReminderForm(currentState.dateTimeMillis, currentState.message)
        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    dateTimeError = validation.dateTimeError,
                    messageError = validation.messageError,
                    isSaved = false
                )
            }
            return
        }
        _uiState.update {
            it.copy(
                dateTimeError = null,
                messageError = null
            )
        }
        viewModelScope.launch {
            val date = currentState.dateTimeMillis
            val message = currentState.message
            val newId = dao.insertReminder(
                Reminder(
                    commitmentId = commitmentId,
                    date = date,
                    time = date,
                    message = message
                )
            )
            _uiState.update { it.copy(isSaved = true) }
            onReminderSaved(newId, date, message)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            dao.deleteReminderById(reminder.id)
        }
    }

    class Factory(private val dao: AppDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ReminderViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}