package com.rajnish.rakshabandhan.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.rajnish.rakshabandhan.features.home.data.HomeApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: HomeData) : HomeUiState
    data object Offline : HomeUiState
    data class Error(val message: String) : HomeUiState
    data object Unauthorized : HomeUiState
}

data class HomeData(
    val sisterId: String,
    val email: String,
    val name: String,
    val enrollmentStatus: String,
)

class HomeViewModel(
    private val api: HomeApi = HomeApi(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val user = firebaseAuth.currentUser
            if (user == null) {
                _uiState.value = HomeUiState.Unauthorized
                return@launch
            }

            runCatching {
                val token = Tasks.await(user.getIdToken(false))?.token
                    ?: error("Authentication token unavailable")
                api.getMe(token)
            }.onSuccess { data ->
                _uiState.value = HomeUiState.Success(
                    HomeData(data.sisterId, data.email, data.name, data.enrollmentStatus)
                )
            }.onFailure { error ->
                _uiState.value = when (error) {
                    is SecurityException -> HomeUiState.Unauthorized
                    is java.net.UnknownHostException,
                    is java.net.ConnectException,
                    is java.net.SocketTimeoutException -> HomeUiState.Offline
                    else -> HomeUiState.Error("Something went wrong. Please try again.")
                }
            }
        }
    }
}
