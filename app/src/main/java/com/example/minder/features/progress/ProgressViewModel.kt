
package com.example.minder.features.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minder.domain.model.AttemptsAndChallengeTypesStatistics
import com.example.minder.domain.model.ChallengeSuccessRate
import com.example.minder.domain.model.InterventionStatistics
import com.example.minder.domain.usecase.challenge.GetAttemptsAndChallengeTypesStatisticsUseCase
import com.example.minder.domain.usecase.challenge.GetChallengeSuccessRateUseCase
import com.example.minder.domain.usecase.intervention.GetInterventionStatisticsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

//Ragahd-: Represents the loading, success, and error states of a progress result.
sealed class ProgressDataState<out T> {
    data object Loading : ProgressDataState<Nothing>()
    data class Success<T>(val data: T) : ProgressDataState<T>()
    data class Error(val message: String) : ProgressDataState<Nothing>()
}

class ProgressViewModel(
    private val getInterventionStatisticsUseCase: GetInterventionStatisticsUseCase,
    private val getChallengeSuccessRateUseCase: GetChallengeSuccessRateUseCase,
    private val getAttemptsAndChallengeTypesStatisticsUseCase: GetAttemptsAndChallengeTypesStatisticsUseCase
) : ViewModel() {

    //Ragahd-: Holds the state of intervention statistics.
    private val _interventionStatisticsState =
        MutableStateFlow<ProgressDataState<InterventionStatistics>>(ProgressDataState.Loading)
    val interventionStatisticsState: StateFlow<ProgressDataState<InterventionStatistics>> =
        _interventionStatisticsState.asStateFlow()

    //Ragahd-: Holds the state of challenge success rate.
    private val _challengeSuccessRateState =
        MutableStateFlow<ProgressDataState<ChallengeSuccessRate>>(ProgressDataState.Loading)
    val challengeSuccessRateState: StateFlow<ProgressDataState<ChallengeSuccessRate>> =
        _challengeSuccessRateState.asStateFlow()

    //Ragahd-: Holds the state of attempts and challenge types statistics.
    private val _attemptsStatisticsState =
        MutableStateFlow<ProgressDataState<AttemptsAndChallengeTypesStatistics>>(ProgressDataState.Loading)
    val attemptsStatisticsState: StateFlow<ProgressDataState<AttemptsAndChallengeTypesStatistics>> =
        _attemptsStatisticsState.asStateFlow()

    //Ragahd-: Loads all three progress statistics independently for the selected user and date.
    fun loadProgress(
        userId: Int,
        date: Long = System.currentTimeMillis()
    ) {
        loadData(_interventionStatisticsState) {
            getInterventionStatisticsUseCase(userId, date)
        }

        loadData(_challengeSuccessRateState) {
            getChallengeSuccessRateUseCase(userId, date)
        }

        loadData(_attemptsStatisticsState) {
            getAttemptsAndChallengeTypesStatisticsUseCase(userId, date)
        }
    }

    //Ragahd-: Executes a progress use case and updates its state independently.
    private fun <T> loadData(
        state: MutableStateFlow<ProgressDataState<T>>,
        operation: suspend () -> T
    ) {
        viewModelScope.launch {
            state.value = ProgressDataState.Loading

            try {
                state.value = ProgressDataState.Success(operation())
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                state.value = ProgressDataState.Error(
                    exception.message ?: "An unexpected error occurred"
                )
            }
        }
    }
}
