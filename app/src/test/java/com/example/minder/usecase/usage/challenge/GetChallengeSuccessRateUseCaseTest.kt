
package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.Intervention
import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.repository.InterventionRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class GetChallengeSuccessRateUseCaseTest {

    private val app = RestrictedApp(
        id = 1,
        userId = 1,
        appName = "Test App",
        packageName = "com.example.testapp",
        dailyLimit = 60,
        isEnabled = true,
        addedAt = 1L
    )

    private val restrictedAppRepository =
        FakeRestrictedAppRepository(listOf(app))

    private val interventionRepository =
        FakeInterventionRepository()

    private val useCase = GetChallengeSuccessRateUseCase(
        restrictedAppRepository = restrictedAppRepository,
        interventionRepository = interventionRepository
    )

    //Ragahd-: Create a fixed timestamp for a specific date and time.
    private fun createDate(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 0,
        minute: Int = 0
    ): Long {
        return Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, minute, 0)
        }.timeInMillis
    }

    //Ragahd-: Create an intervention with a configurable status.
    private fun createIntervention(
        id: Int,
        appId: Int,
        triggerTime: Long,
        status: String
    ) = Intervention(
        id = id,
        appId = appId,
        challengeId = 1,
        triggerTime = triggerTime,
        unlockTime = null,
        status = status,
        savedTime = 0
    )

    @Test
    fun invoke_withSuccessfulAndFailedInterventions_calculatesSuccessRate() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        interventionRepository.interventions[app.id] = mutableListOf(
            createIntervention(
                id = 1,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 5, 10),
                status = "UNLOCKED"
            ),
            createIntervention(
                id = 2,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 6, 10),
                status = "LOCKED"
            ),
            createIntervention(
                id = 3,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 7, 10),
                status = "UNLOCKED"
            ),
            createIntervention(
                id = 4,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 8, 10),
                status = "LOCKED"
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(4, result.totalCompletedInterventions)
        assertEquals(2, result.successfulInterventions)
        assertEquals(2, result.failedInterventions)
        assertEquals(50.0, result.successRatePercentage, 0.001)
    }

    @Test
    fun invoke_withPendingInterventions_excludesThemFromSuccessRate() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        interventionRepository.interventions[app.id] = mutableListOf(
            createIntervention(
                id = 1,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 5, 10),
                status = "UNLOCKED"
            ),
            createIntervention(
                id = 2,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 6, 10),
                status = "LOCKED"
            ),
            createIntervention(
                id = 3,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 7, 10),
                status = "PENDING"
            ),
            createIntervention(
                id = 4,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 8, 10),
                status = "PENDING"
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(2, result.totalCompletedInterventions)
        assertEquals(1, result.successfulInterventions)
        assertEquals(1, result.failedInterventions)
        assertEquals(50.0, result.successRatePercentage, 0.001)
    }

    @Test
    fun invoke_withOnlyPendingInterventions_returnsZeroSuccessRate() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        interventionRepository.interventions[app.id] = mutableListOf(
            createIntervention(
                id = 1,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 7, 10),
                status = "PENDING"
            ),
            createIntervention(
                id = 2,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 8, 10),
                status = "PENDING"
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(0, result.totalCompletedInterventions)
        assertEquals(0, result.successfulInterventions)
        assertEquals(0, result.failedInterventions)
        assertEquals(0.0, result.successRatePercentage, 0.001)
    }

    @Test
    fun invoke_withDisabledApp_excludesItsInterventions() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        val disabledApp = app.copy(
            id = 2,
            isEnabled = false
        )

        restrictedAppRepository.apps = listOf(app, disabledApp)

        interventionRepository.interventions[app.id] = mutableListOf(
            createIntervention(
                id = 1,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 5, 10),
                status = "LOCKED"
            )
        )

        interventionRepository.interventions[disabledApp.id] = mutableListOf(
            createIntervention(
                id = 2,
                appId = disabledApp.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 5, 11),
                status = "UNLOCKED"
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(1, result.totalCompletedInterventions)
        assertEquals(0, result.successfulInterventions)
        assertEquals(1, result.failedInterventions)
        assertEquals(0.0, result.successRatePercentage, 0.001)
    }

    @Test
    fun invoke_withAppsFromDifferentUsers_excludesOtherUsersInterventions() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        val anotherUserApp = app.copy(
            id = 2,
            userId = 2
        )

        restrictedAppRepository.apps = listOf(app, anotherUserApp)

        interventionRepository.interventions[app.id] = mutableListOf(
            createIntervention(
                id = 1,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 5, 10),
                status = "LOCKED"
            )
        )

        interventionRepository.interventions[anotherUserApp.id] = mutableListOf(
            createIntervention(
                id = 2,
                appId = anotherUserApp.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 5, 11),
                status = "UNLOCKED"
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(1, result.totalCompletedInterventions)
        assertEquals(0, result.successfulInterventions)
        assertEquals(1, result.failedInterventions)
        assertEquals(0.0, result.successRatePercentage, 0.001)
    }

    @Test
    fun invoke_withInterventionsOutsidePeriod_excludesThem() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        interventionRepository.interventions[app.id] = mutableListOf(
            createIntervention(
                id = 1,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 3, 23, 59),
                status = "UNLOCKED"
            ),
            createIntervention(
                id = 2,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 4),
                status = "UNLOCKED"
            ),
            createIntervention(
                id = 3,
                appId = app.id,
                triggerTime = date,
                status = "LOCKED"
            ),
            createIntervention(
                id = 4,
                appId = app.id,
                triggerTime = date + 1000L,
                status = "UNLOCKED"
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(2, result.totalCompletedInterventions)
        assertEquals(1, result.successfulInterventions)
        assertEquals(1, result.failedInterventions)
        assertEquals(50.0, result.successRatePercentage, 0.001)
    }

    @Test
    fun invoke_withoutInterventions_returnsZeroValues() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        val result = useCase(userId = 1, date = date)

        assertEquals(0, result.totalCompletedInterventions)
        assertEquals(0, result.successfulInterventions)
        assertEquals(0, result.failedInterventions)
        assertEquals(0.0, result.successRatePercentage, 0.001)
    }

    //Ragahd-: Fake restricted app repository for challenge success rate tests.
    private class FakeRestrictedAppRepository(
        var apps: List<RestrictedApp>
    ) : RestrictedAppRepository {

        override suspend fun getAppsByUserId(userId: Int) =
            apps.filter { it.userId == userId }

        override suspend fun getAppById(appId: Int) =
            apps.find { it.id == appId }

        override suspend fun getAppByPackageName(
            userId: Int,
            packageName: String
        ) = apps.find {
            it.userId == userId && it.packageName == packageName
        }

        override suspend fun addApp(app: RestrictedApp): Long =
            app.id.toLong()

        override suspend fun updateApp(app: RestrictedApp) {}

        override suspend fun deleteApp(app: RestrictedApp) {}
    }

    //Ragahd-: Fake intervention repository for challenge success rate tests.
    private class FakeInterventionRepository : InterventionRepository {

        val interventions = mutableMapOf<Int, MutableList<Intervention>>()

        override suspend fun addIntervention(intervention: Intervention): Long {
            interventions.getOrPut(intervention.appId) { mutableListOf() }
                .add(intervention)
            return intervention.id.toLong()
        }

        override suspend fun getInterventionById(interventionId: Int) =
            interventions.values.flatten().find {
                it.id == interventionId
            }

        override suspend fun getPendingIntervention(appId: Int) =
            interventions[appId].orEmpty().find {
                it.status == "PENDING"
            }

        override suspend fun getInterventionsByAppId(appId: Int) =
            interventions[appId].orEmpty()

        //Ragahd-: Retrieve interventions triggered within the requested period.
        override suspend fun getInterventionsForPeriod(
            appId: Int,
            startTime: Long,
            endTime: Long
        ) = interventions[appId].orEmpty().filter {
            it.triggerTime >= startTime && it.triggerTime <= endTime
        }

        override suspend fun completeIntervention(
            interventionId: Int,
            unlockTime: Long?,
            status: String
        ) {}

        override suspend fun getPendingInterventionForUser(userId: Int) =
            null
    }
}
