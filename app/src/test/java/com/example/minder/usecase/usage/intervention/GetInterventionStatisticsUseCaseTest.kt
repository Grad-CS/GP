
package com.example.minder.domain.usecase.intervention

import com.example.minder.domain.model.Intervention
import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.repository.InterventionRepository
import com.example.minder.domain.repository.RestrictedAppRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class GetInterventionStatisticsUseCaseTest {

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

    private val useCase = GetInterventionStatisticsUseCase(
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

    //Ragahd-: Create an intervention with configurable status and resolution time.
    private fun createIntervention(
        id: Int,
        appId: Int,
        triggerTime: Long,
        unlockTime: Long? = null,
        status: String
    ) = Intervention(
        id = id,
        appId = appId,
        challengeId = 1,
        triggerTime = triggerTime,
        unlockTime = unlockTime,
        status = status,
        savedTime = 0
    )

    @Test
    fun invoke_withDifferentStatuses_countsAllInterventionsCorrectly() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        interventionRepository.interventions[app.id] = mutableListOf(
            createIntervention(
                id = 1,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 5, 10),
                unlockTime = createDate(2026, Calendar.NOVEMBER, 5, 10, 5),
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
                unlockTime = createDate(2026, Calendar.NOVEMBER, 8, 10, 10),
                status = "UNLOCKED"
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(4, result.totalInterventions)
        assertEquals(2, result.unlockedInterventions)
        assertEquals(1, result.lockedInterventions)
        assertEquals(1, result.pendingInterventions)
    }

    @Test
    fun invoke_withUnlockedInterventions_calculatesAverageResolutionTime() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        interventionRepository.interventions[app.id] = mutableListOf(
            createIntervention(
                id = 1,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 5, 10),
                unlockTime = createDate(2026, Calendar.NOVEMBER, 5, 10, 5),
                status = "UNLOCKED"
            ),
            createIntervention(
                id = 2,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 6, 10),
                unlockTime = createDate(2026, Calendar.NOVEMBER, 6, 10, 15),
                status = "UNLOCKED"
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(2, result.unlockedInterventions)
        assertEquals(600.0, result.averageResolutionTimeSeconds, 0.001)
    }

    @Test
    fun invoke_withLockedAndPendingInterventions_excludesThemFromAverage() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        interventionRepository.interventions[app.id] = mutableListOf(
            createIntervention(
                id = 1,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 5, 10),
                unlockTime = createDate(2026, Calendar.NOVEMBER, 5, 10, 10),
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
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(3, result.totalInterventions)
        assertEquals(600.0, result.averageResolutionTimeSeconds, 0.001)
    }

    @Test
    fun invoke_withInvalidUnlockTimes_excludesThemFromAverage() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        val triggerTime = createDate(2026, Calendar.NOVEMBER, 5, 10)

        interventionRepository.interventions[app.id] = mutableListOf(
            createIntervention(
                id = 1,
                appId = app.id,
                triggerTime = triggerTime,
                unlockTime = null,
                status = "UNLOCKED"
            ),
            createIntervention(
                id = 2,
                appId = app.id,
                triggerTime = triggerTime,
                unlockTime = triggerTime,
                status = "UNLOCKED"
            ),
            createIntervention(
                id = 3,
                appId = app.id,
                triggerTime = triggerTime,
                unlockTime = triggerTime - 1000L,
                status = "UNLOCKED"
            ),
            createIntervention(
                id = 4,
                appId = app.id,
                triggerTime = triggerTime,
                unlockTime = triggerTime + 300_000L,
                status = "UNLOCKED"
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(4, result.unlockedInterventions)
        assertEquals(300.0, result.averageResolutionTimeSeconds, 0.001)
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
                unlockTime = createDate(2026, Calendar.NOVEMBER, 5, 11, 30),
                status = "UNLOCKED"
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(1, result.totalInterventions)
        assertEquals(0, result.unlockedInterventions)
        assertEquals(1, result.lockedInterventions)
        assertEquals(0.0, result.averageResolutionTimeSeconds, 0.001)
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
                unlockTime = createDate(2026, Calendar.NOVEMBER, 5, 11, 30),
                status = "UNLOCKED"
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(1, result.totalInterventions)
        assertEquals(0, result.unlockedInterventions)
        assertEquals(1, result.lockedInterventions)
    }

    @Test
    fun invoke_withInterventionsOutsidePeriod_excludesThem() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        interventionRepository.interventions[app.id] = mutableListOf(
            createIntervention(
                id = 1,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 3, 23, 59),
                status = "LOCKED"
            ),
            createIntervention(
                id = 2,
                appId = app.id,
                triggerTime = createDate(2026, Calendar.NOVEMBER, 4),
                status = "PENDING"
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
                status = "LOCKED"
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(2, result.totalInterventions)
        assertEquals(1, result.lockedInterventions)
        assertEquals(1, result.pendingInterventions)
    }

    @Test
    fun invoke_withoutInterventions_returnsZeroValues() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        val result = useCase(userId = 1, date = date)

        assertEquals(0, result.totalInterventions)
        assertEquals(0, result.unlockedInterventions)
        assertEquals(0, result.lockedInterventions)
        assertEquals(0, result.pendingInterventions)
        assertEquals(0.0, result.averageResolutionTimeSeconds, 0.001)
    }

    //Ragahd-: Fake restricted app repository for intervention statistics tests.
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

    //Ragahd-: Fake intervention repository for intervention statistics tests.
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
