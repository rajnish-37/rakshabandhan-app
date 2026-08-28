package com.rajnish.rakshabandhan.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.rajnish.rakshabandhan.features.home.data.ClaimApi
import com.rajnish.rakshabandhan.features.home.data.GiftClaimData
import com.rajnish.rakshabandhan.features.home.data.GiftData
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
    val gift: GiftData?,
    val claim: GiftClaimData?,
)

class HomeViewModel(
    private val api: HomeApi = HomeApi(),
    private val claimApi: ClaimApi = ClaimApi(),
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
                val token = Tasks.await(user.getIdToken(false))?.token ?: error("Authentication token unavailable")
                val home = api.getMe(token)
                val claim = when (val result = claimApi.getMyClaim(token)) {
                    is com.rajnish.rakshabandhan.features.home.data.ClaimResult.Success -> result.claim
                    is com.rajnish.rakshabandhan.features.home.data.ClaimResult.Failure -> null
                }
                HomeData(home.sisterId, home.email, home.name, home.enrollmentStatus, home.gift, claim)
            }.onSuccess { data ->
                _uiState.value = HomeUiState.Success(data)
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

    fun submitClaim(upiId: String) {
        val current = _uiState.value
        if (current !is HomeUiState.Success || current.data.claim != null) return
        val user = firebaseAuth.currentUser ?: run {
            _uiState.value = HomeUiState.Unauthorized
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val token = Tasks.await(user.getIdToken(false))?.token ?: error("Authentication token unavailable")
                when (val result = claimApi.submitClaim(token, upiId)) {
                    is com.rajnish.rakshabandhan.features.home.data.ClaimResult.Success -> result.claim ?: error("Invalid claim response")
                    is com.rajnish.rakshabandhan.features.home.data.ClaimResult.Failure -> error(result.message)
                }
            }.onSuccess { claim ->
                val state = _uiState.value
                if (state is HomeUiState.Success) {
                    _uiState.value = state.copy(data = state.data.copy(claim = claim))
                }
            }.onFailure { error ->
                val message = error.message ?: "Unable to submit your claim."
                val state = _uiState.value
                if (state is HomeUiState.Success) {
                    _uiState.value = state.copy(data = state.data.copy(claim = null))
                    claimError = message
                } else {
                    _uiState.value = HomeUiState.Error(message)
                }
            }
        }
    }

    private var claimError: String? = null
    fun consumeClaimError(): String? {
        val error = claimError
        claimError = null
        return error
    }
}
