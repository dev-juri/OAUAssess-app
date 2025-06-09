import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oau.assess.models.LoginRequest
import com.oau.assess.models.StudentData
import com.oau.assess.repositories.StudentRepository
import com.oau.assess.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: StudentData) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    val matricNo = MutableStateFlow("")
    val password = MutableStateFlow("")

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onMatricNoChanged(value: String) {
        matricNo.value = value
        _errorMessage.value = null
    }

    fun onPasswordChanged(value: String) {
        password.value = value
        _errorMessage.value = null
    }

    fun login() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = repository.login(LoginRequest(matricNo.value, password.value))) {
                is NetworkResult.Success -> {
                    // Save the student data before marking as success
                    repository.setCurrentStudent(result.data)
                    _uiState.value = LoginUiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = LoginUiState.Error(result.message)
                    _errorMessage.value = result.message
                }
                is NetworkResult.Loading -> {
                    _uiState.value = LoginUiState.Loading
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}