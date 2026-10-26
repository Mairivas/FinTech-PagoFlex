package com.example.fintechpagoflex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fintechpagoflex.data.AppDao
import com.example.fintechpagoflex.data.Commitment
import com.example.fintechpagoflex.data.PaymentHistoryItem
import com.example.fintechpagoflex.data.ProfilePreferences
import com.example.fintechpagoflex.util.validatePaymentForm
import com.example.fintechpagoflex.util.validateProfileForm
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dao: AppDao,
    private val profilePreferences: ProfilePreferences
) : ViewModel() {

    val profileName: StateFlow<String> = profilePreferences.name
    val profilePhotoUri: StateFlow<String?> = profilePreferences.photoUri

    val commitments: StateFlow<List<Commitment>> = dao.getAllCommitments()
        .map { list ->
            list.sortedWith(
                compareBy<Commitment> {
                    when (it.status.lowercase()) {
                        "vencido" -> 1
                        "pendiente" -> 2
                        "pagado" -> 3
                        else -> 4
                    }
                }.thenBy { it.dueDate }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPending: StateFlow<Double> = dao.getAllCommitments()
        .map { list ->
            list.filter {
                it.status.equals("Pendiente", ignoreCase = true) || it.status.equals("Vencido", ignoreCase = true)
            }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val nextDueDate: StateFlow<Long?> = dao.getAllCommitments()
        .map { list ->
            val pendingOrOverdue = list.filter {
                it.status.equals("Pendiente", ignoreCase = true) || it.status.equals("Vencido", ignoreCase = true)
            }
            val futureCommitments = pendingOrOverdue.filter { it.dueDate >= System.currentTimeMillis() }
            if (futureCommitments.isNotEmpty()) {
                futureCommitments.minOfOrNull { it.dueDate }
            } else {
                pendingOrOverdue.minOfOrNull { it.dueDate }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun getCommitment(id: Long): StateFlow<Commitment?> = dao.getCommitmentById(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun attachPhoto(id: Long, uri: String) {
        viewModelScope.launch {
            dao.updateCommitmentPhoto(id, uri)
        }
    }

    class Factory(
        private val dao: AppDao,
        private val profilePreferences: ProfilePreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(dao, profilePreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

class HistoryViewModel(private val dao: AppDao) : ViewModel() {
    val history: StateFlow<List<PaymentHistoryItem>> = dao.getPaymentHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getPayment(id: Long): StateFlow<PaymentHistoryItem?> = dao.getPaymentHistoryById(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun getPaymentByCommitment(commitmentId: Long): StateFlow<PaymentHistoryItem?> = dao.getPaymentHistoryByCommitmentId(commitmentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    class Factory(private val dao: AppDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class SimulatePaymentUiState(
    val selectedMethod: String? = null,
    val isSimulationChecked: Boolean = false,
    val methodError: String? = null,
    val confirmationError: String? = null,
    val isProcessing: Boolean = false
)

class SimulatePaymentViewModel(private val dao: AppDao) : ViewModel() {
    private val _uiState = MutableStateFlow(SimulatePaymentUiState())
    val uiState: StateFlow<SimulatePaymentUiState> = _uiState.asStateFlow()

    fun getCommitment(id: Long): StateFlow<Commitment?> = dao.getCommitmentById(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onMethodSelected(method: String) {
        _uiState.update { current ->
            val validation = validatePaymentForm(method, current.isSimulationChecked)
            current.copy(
                selectedMethod = method,
                methodError = if (current.methodError != null) validation.methodError else null
            )
        }
    }

    fun onSimulationCheckedChanged(checked: Boolean) {
        _uiState.update { current ->
            val validation = validatePaymentForm(current.selectedMethod, checked)
            current.copy(
                isSimulationChecked = checked,
                confirmationError = if (current.confirmationError != null) validation.confirmationError else null
            )
        }
    }

    fun processSimulatedPayment(commitmentId: Long, amount: Double, onSuccess: (Long) -> Unit) {
        val currentState = _uiState.value
        val validation = validatePaymentForm(currentState.selectedMethod, currentState.isSimulationChecked)
        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    methodError = validation.methodError,
                    confirmationError = validation.confirmationError
                )
            }
            return
        }
        _uiState.update {
            it.copy(
                methodError = null,
                confirmationError = null,
                isProcessing = true
            )
        }
        viewModelScope.launch {
            delay(2000)
            val method = currentState.selectedMethod ?: "Tarjeta"
            val now = System.currentTimeMillis()
            dao.processPaymentTransaction(commitmentId, amount, method, now.toString())
            val paymentId = dao.getPaymentIdForCommitment(commitmentId) ?: 0L
            _uiState.update { it.copy(isProcessing = false) }
            onSuccess(paymentId)
        }
    }

    class Factory(private val dao: AppDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SimulatePaymentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SimulatePaymentViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class ProfileUiState(
    val name: String = "Juan Pérez",
    val photoUriString: String? = null,
    val nameError: String? = null,
    val showSuccessSnackbar: Boolean = false
)

class ProfileViewModel(private val prefs: ProfilePreferences) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProfileUiState(
            name = prefs.getName(),
            photoUriString = prefs.getPhotoUri()
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onNameChanged(newName: String) {
        _uiState.update { current ->
            current.copy(
                name = newName,
                nameError = if (current.nameError != null) validateProfileForm(newName) else null,
                showSuccessSnackbar = false
            )
        }
    }

    fun onPhotoUpdated(uriString: String?) {
        prefs.savePhotoUri(uriString)
        _uiState.update { current ->
            current.copy(
                photoUriString = uriString,
                showSuccessSnackbar = false
            )
        }
    }

    fun removePhoto() {
        prefs.savePhotoUri(null)
        _uiState.update { current ->
            current.copy(
                photoUriString = null,
                showSuccessSnackbar = false
            )
        }
    }

    fun saveProfile() {
        val currentName = _uiState.value.name
        val error = validateProfileForm(currentName)
        if (error != null) {
            _uiState.update { it.copy(nameError = error, showSuccessSnackbar = false) }
        } else {
            prefs.saveName(currentName)
            _uiState.update { it.copy(nameError = null, showSuccessSnackbar = true) }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(showSuccessSnackbar = false) }
    }

    class Factory(private val prefs: ProfilePreferences) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(prefs) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}