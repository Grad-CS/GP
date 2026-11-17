
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.HistoricalUsageRepository
import com.example.minder.domain.repository.UsageBaselineRepository

//Ragahd-: Represent the outcome of processing each app's historical baseline.
enum class BaselineSaveStatus {
    SAVED,
    ALREADY_EXISTS,
    INCOMPLETE_HISTORY,
    INVALID_HISTORICAL_DATA,
    SAVE_FAILED
}

//Ragahd-: Store the processing result for each restricted app.
data class BaselineSaveResult(
    val appId: Int,
    val packageName: String,
    val status: BaselineSaveStatus,
    val recordsFound: Int
)

class SaveHistoricalBaselinesUseCase(
    private val restrictedAppRepository: RestrictedAppRepository,
    private val historicalUsageRepository: HistoricalUsageRepository,
    private val calculateHistoricalBaselineUseCase: CalculateHistoricalBaselineUseCase,
    private val usageBaselineRepository: UsageBaselineRepository
) {

    //Ragahd-: Generate and save fixed historical baselines for restricted apps.
    suspend operator fun invoke(userId: Int): List<BaselineSaveResult> {

        val results = mutableListOf<BaselineSaveResult>()

        //Ragahd-: Retrieve all restricted apps belonging to the user.
        val restrictedApps =
            restrictedAppRepository.getAppsByUserId(userId)

        for (app in restrictedApps) {

            //Ragahd-: Skip apps that already have a fixed baseline.
            val existingBaseline =
                usageBaselineRepository.getBaselineByAppId(app.id)

            if (existingBaseline.isNotEmpty()) {
                results.add(
                    BaselineSaveResult(
                        appId = app.id,
                        packageName = app.packageName,
                        status = BaselineSaveStatus.ALREADY_EXISTS,
                        recordsFound = 0
                    )
                )
                continue
            }

            //Ragahd-: Calculate the historical period before the first app addition.
            val (periodStart, periodEndExclusive) =
                calculateHistoricalBaselineUseCase.getHistoricalPeriod(
                    app.addedAt
                )

            //Ragahd-: Retrieve historical daily usage for the app.
            val dailyUsages = historicalUsageRepository.getDailyUsage(
                packageName = app.packageName,
                startDate = periodStart,
                endDateExclusive = periodEndExclusive
            )

            //Ragahd-: Report incomplete history without treating missing records as zero usage.
            if (dailyUsages.size != 14) {
                results.add(
                    BaselineSaveResult(
                        appId = app.id,
                        packageName = app.packageName,
                        status = BaselineSaveStatus.INCOMPLETE_HISTORY,
                        recordsFound = dailyUsages.size
                    )
                )
                continue
            }

            //Ragahd-: Calculate seven weekday baselines from valid historical data.
            val baselines = calculateHistoricalBaselineUseCase.calculate(
                appId = app.id,
                packageName = app.packageName,
                addedAt = app.addedAt,
                dailyUsages = dailyUsages,
                capturedAt = System.currentTimeMillis()
            )

            if (baselines == null) {
                results.add(
                    BaselineSaveResult(
                        appId = app.id,
                        packageName = app.packageName,
                        status = BaselineSaveStatus.INVALID_HISTORICAL_DATA,
                        recordsFound = dailyUsages.size
                    )
                )
                continue
            }

            //Ragahd-: Save the baseline only if no previous baseline exists.
            val saved = usageBaselineRepository.saveBaselines(baselines)

            results.add(
                BaselineSaveResult(
                    appId = app.id,
                    packageName = app.packageName,
                    status = if (saved) {
                        BaselineSaveStatus.SAVED
                    } else {
                        BaselineSaveStatus.SAVE_FAILED
                    },
                    recordsFound = dailyUsages.size
                )
            )
        }

        return results
    }
}
