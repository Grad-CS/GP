
package com.example.minder.features.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minder.domain.model.AverageSessionDuration
import com.example.minder.domain.model.DailyUsageStats
import com.example.minder.domain.model.PeakUsageTimes
import com.example.minder.domain.model.SavedTimeStats
import com.example.minder.domain.model.UsageComparisonStats
import com.example.minder.domain.model.WeeklyUsageStats
import com.example.minder.domain.usecase.usage.GetAverageSessionDurationUseCase
import com.example.minder.domain.usecase.usage.GetDailyUsageStatsUseCase
import com.example.minder.domain.usecase.usage.GetPeakUsageTimesUseCase
import com.example.minder.domain.usecase.usage.GetSavedTimeStatsUseCase
import com.example.minder.domain.usecase.usage.GetUsageComparisonUseCase
import com.example.minder.domain.usecase.usage.GetWeeklyUsageStatsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

//Ragahd-: Represents the loading, success, and error states of a statistics result.
sealed class StatisticsDataState<out T> {
    data object Loading : StatisticsDataState<Nothing>()
    data class Success<T>(val data: T) : StatisticsDataState<T>()
    data class Error(val message: String) : StatisticsDataState<Nothing>()
}

class StatisticsViewModel(
    private val getDailyUsageStatsUseCase: GetDailyUsageStatsUseCase,
    private val getWeeklyUsageStatsUseCase: GetWeeklyUsageStatsUseCase,
    private val getUsageComparisonUseCase: GetUsageComparisonUseCase,
    private val getSavedTimeStatsUseCase: GetSavedTimeStatsUseCase,
    private val getAverageSessionDurationUseCase: GetAverageSessionDurationUseCase,
    private val getPeakUsageTimesUseCase: GetPeakUsageTimesUseCase
) : ViewModel() {

    //Ragahd-: Holds the state of daily usage statistics.
    private val _dailyUsageState =
        MutableStateFlow<StatisticsDataState<DailyUsageStats>>(StatisticsDataState.Loading)
    val dailyUsageState: StateFlow<StatisticsDataState<DailyUsageStats>> =
        _dailyUsageState.asStateFlow()

    //Ragahd-: Holds the state of weekly usage statistics.
    private val _weeklyUsageState =
        MutableStateFlow<StatisticsDataState<WeeklyUsageStats>>(StatisticsDataState.Loading)
    val weeklyUsageState: StateFlow<StatisticsDataState<WeeklyUsageStats>> =
        _weeklyUsageState.asStateFlow()

    //Ragahd-: Holds the state of usage comparison statistics.
    private val _usageComparisonState =
        MutableStateFlow<StatisticsDataState<UsageComparisonStats>>(StatisticsDataState.Loading)
    val usageComparisonState: StateFlow<StatisticsDataState<UsageComparisonStats>> =
        _usageComparisonState.asStateFlow()

    //Ragahd-: Holds the state of saved time statistics.
    private val _savedTimeState =
        MutableStateFlow<StatisticsDataState<SavedTimeStats>>(StatisticsDataState.Loading)
    val savedTimeState: StateFlow<StatisticsDataState<SavedTimeStats>> =
        _savedTimeState.asStateFlow()

    //Ragahd-: Holds the state of average session duration statistics.
    private val _averageSessionDurationState =
        MutableStateFlow<StatisticsDataState<AverageSessionDuration>>(StatisticsDataState.Loading)
    val averageSessionDurationState: StateFlow<StatisticsDataState<AverageSessionDuration>> =
        _averageSessionDurationState.asStateFlow()

    //Ragahd-: Holds the state of peak usage times statistics.
    private val _peakUsageTimesState =
        MutableStateFlow<StatisticsDataState<PeakUsageTimes>>(StatisticsDataState.Loading)
    val peakUsageTimesState: StateFlow<StatisticsDataState<PeakUsageTimes>> =
        _peakUsageTimesState.asStateFlow()

    //Ragahd-: Loads all six statistics independently for the selected user and date.
    fun loadStatistics(
        userId: Int,
        date: Long = System.currentTimeMillis()
    ) {
        loadData(_dailyUsageState) {
            getDailyUsageStatsUseCase(userId, date)
        }

        loadData(_weeklyUsageState) {
            getWeeklyUsageStatsUseCase(userId, date)
        }

        loadData(_usageComparisonState) {
            getUsageComparisonUseCase(userId, date)
        }

        loadData(_savedTimeState) {
            getSavedTimeStatsUseCase(userId)
        }

        loadData(_averageSessionDurationState) {
            getAverageSessionDurationUseCase(userId, date)
        }

        loadData(_peakUsageTimesState) {
            getPeakUsageTimesUseCase(userId, date)
        }
    }

    //Ragahd-: Executes a statistics use case and updates its state independently.
    private fun <T> loadData(
        state: MutableStateFlow<StatisticsDataState<T>>,
        operation: suspend () -> T
    ) {
        viewModelScope.launch {
            state.value = StatisticsDataState.Loading

            try {
                state.value = StatisticsDataState.Success(operation())
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                state.value = StatisticsDataState.Error(
                    exception.message ?: "An unexpected error occurred"
                )
            }
        }
    }
}
