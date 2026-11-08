
package com.example.minder.features.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minder.domain.model.Challenge
import com.example.minder.domain.usecase.challenge.ChallengeResult
import com.example.minder.domain.usecase.challenge.HandleChallengeResultUseCase
import com.example.minder.domain.usecase.intervention.GetInterventionChallengeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

//Ragahd-: Holds all UI state needed by ChallengeScreen.
data class ChallengeUiState(
    val challenge: Challenge? = null,
    val userAnswer: String = "",
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val isWrong: Boolean = false,
    val errorMessage: String? = null,
    val result: ChallengeResult? = null,

    //Ragahd-: Tracks whether the terminal result has been handled.
    val isResultHandled: Boolean = false
)

class ChallengeViewModel(
    private val interventionId: Int,
    private val getInterventionChallengeUseCase: GetInterventionChallengeUseCase,
    private val handleChallengeResultUseCase: HandleChallengeResultUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        loadChallenge()
    }
    //Ragahd-: Marks the terminal result as handled to prevent duplicate callbacks.
    fun markResultHandled() {
        _uiState.value = _uiState.value.copy(isResultHandled = true)
    }

    //Ragahd-: Loads the challenge linked to the current intervention.
    private fun loadChallenge() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val challenge = getInterventionChallengeUseCase(
                    interventionId = interventionId
                )

                _uiState.value = _uiState.value.copy(
                    challenge = challenge,
                    isLoading = false,
                    errorMessage = if (challenge == null) {
                        "Challenge could not be found."
                    } else {
                        null
                    }
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unable to load the challenge."
                )
            }
        }
    }

    //Ragahd-: Updates the answer and clears the previous incorrect-answer message.
    fun updateAnswer(answer: String) {
        val currentState = _uiState.value

        if (currentState.isSubmitting || currentState.result != null) {
            return
        }

        _uiState.value = currentState.copy(
            userAnswer = answer,
            isWrong = false,
            errorMessage = null
        )
    }

    //Ragahd-: Submits one answer and delegates validation and attempt recording to the use case.
    fun submitAnswer() {
        val currentState = _uiState.value
        val challenge = currentState.challenge ?: return

        if (
            currentState.userAnswer.isBlank() ||
            currentState.isLoading ||
            currentState.isSubmitting ||
            currentState.result != null
        ) {
            return
        }

        val submittedAnswer = currentState.userAnswer

        _uiState.value = currentState.copy(
            isSubmitting = true,
            isWrong = false,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                val result = handleChallengeResultUseCase(
                    challenge = challenge,
                    interventionId = interventionId,
                    userAnswer = submittedAnswer
                )

                //Ragahd-: Keeps retry results non-terminal so the user can submit another attempt.
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    userAnswer = if (result == ChallengeResult.Retry) {
                        ""
                    } else {
                        submittedAnswer
                    },
                    isWrong = result == ChallengeResult.Retry,
                    result = if (result == ChallengeResult.Retry) {
                        null
                    } else {
                        result
                    }
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Unable to submit the answer. Please try again."
                )
            }
        }
    }
}
