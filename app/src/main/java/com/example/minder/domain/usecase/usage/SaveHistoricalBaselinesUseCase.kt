
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.HistoricalUsageRepository
import com.example.minder.domain.repository.UsageBaselineRepository

class SaveHistoricalBaselinesUseCase(
    private val restrictedAppRepository: RestrictedAppRepository,
    private val historicalUsageRepository: HistoricalUsageRepository,
    private val calculateHistoricalBaselineUseCase: CalculateHistoricalBaselineUseCase,
    private val usageBaselineRepository: UsageBaselineRepository
) {

    //Ragahd-: Generate and save fixed historical baselines for restricted apps.
    suspend operator fun invoke(userId: Int): List<Int> {

        val savedAppIds = mutableListOf<Int>()

        //Ragahd-: Retrieve all restricted apps belonging to the user.
        val restrictedApps =
            restrictedAppRepository.getAppsByUserId(userId)

        for (app in restrictedApps) {

            //Ragahd-: Skip apps that already have a fixed baseline.
            val existingBaseline =
                usageBaselineRepository.getBaselineByAppId(app.id)

            if (existingBaseline.isNotEmpty()) continue

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

            //Ragahd-: Calculate seven weekday baselines from valid historical data.
            val baselines = calculateHistoricalBaselineUseCase.calculate(
                appId = app.id,
                packageName = app.packageName,
                addedAt = app.addedAt,
                dailyUsages = dailyUsages,
                capturedAt = System.currentTimeMillis()
            ) ?: continue

            //Ragahd-: Save the baseline only if no previous baseline exists.
            val saved = usageBaselineRepository.saveBaselines(baselines)

            if (saved) {
                savedAppIds.add(app.id)
            }
        }

        return savedAppIds
    }
}
