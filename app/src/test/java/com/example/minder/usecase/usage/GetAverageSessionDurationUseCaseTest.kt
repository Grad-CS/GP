
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.model.UsageSession
import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.UsageSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class GetAverageSessionDurationUseCaseTest {

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

    private val usageSessionRepository =
        FakeUsageSessionRepository()

    private val useCase = GetAverageSessionDurationUseCase(
        restrictedAppRepository = restrictedAppRepository,
        usageSessionRepository = usageSessionRepository
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

    //Ragahd-: Create a usage session with a specified duration.
    private fun createSession(
        appId: Int,
        startTime: Long,
        duration: Int
    ) = UsageSession(
        id = 0,
        appId = appId,
        startTime = startTime,
        endTime = startTime + duration * 1000L,
        duration = duration
    )

    @Test
    fun invoke_withMultipleValidSessions_calculatesAverageDuration() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        usageSessionRepository.sessions[app.id] =
            mutableListOf(
                createSession(app.id, createDate(2026, Calendar.NOVEMBER, 5, 10), 600),
                createSession(app.id, createDate(2026, Calendar.NOVEMBER, 7, 12), 1200),
                createSession(app.id, createDate(2026, Calendar.NOVEMBER, 9, 15), 1800)
            )

        val result = useCase(userId = 1, date = date)

        assertEquals(3, result.totalSessions)
        assertEquals(1200.0, result.averageDurationSeconds, 0.001)
    }

    @Test
    fun invoke_withZeroDurationSessions_excludesThem() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        usageSessionRepository.sessions[app.id] =
            mutableListOf(
                createSession(app.id, createDate(2026, Calendar.NOVEMBER, 5), 600),
                createSession(app.id, createDate(2026, Calendar.NOVEMBER, 6), 0),
                createSession(app.id, createDate(2026, Calendar.NOVEMBER, 7), 1200)
            )

        val result = useCase(userId = 1, date = date)

        assertEquals(2, result.totalSessions)
        assertEquals(900.0, result.averageDurationSeconds, 0.001)
    }

    @Test
    fun invoke_withNegativeDurationSessions_excludesThem() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        usageSessionRepository.sessions[app.id] =
            mutableListOf(
                createSession(app.id, createDate(2026, Calendar.NOVEMBER, 5), 600),
                createSession(app.id, createDate(2026, Calendar.NOVEMBER, 6), -300)
            )

        val result = useCase(userId = 1, date = date)

        assertEquals(1, result.totalSessions)
        assertEquals(600.0, result.averageDurationSeconds, 0.001)
    }

    @Test
    fun invoke_withSessionsOutsidePeriod_excludesThem() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        val beforePeriod = createDate(2026, Calendar.NOVEMBER, 3, 23, 59)
        val periodStart = createDate(2026, Calendar.NOVEMBER, 4)
        val periodEnd = date

        usageSessionRepository.sessions[app.id] =
            mutableListOf(
                createSession(app.id, beforePeriod, 500),
                createSession(app.id, periodStart, 600),
                createSession(app.id, periodEnd, 1200)
            )

        val result = useCase(userId = 1, date = date)

        assertEquals(1, result.totalSessions)
        assertEquals(600.0, result.averageDurationSeconds, 0.001)
    }

    @Test
    fun invoke_withDisabledApp_excludesItsSessions() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        val disabledApp = app.copy(
            id = 2,
            isEnabled = false
        )

        restrictedAppRepository.apps =
            listOf(app, disabledApp)

        usageSessionRepository.sessions[app.id] =
            mutableListOf(
                createSession(app.id, createDate(2026, Calendar.NOVEMBER, 5), 600)
            )

        usageSessionRepository.sessions[disabledApp.id] =
            mutableListOf(
                createSession(disabledApp.id, createDate(2026, Calendar.NOVEMBER, 5), 5000)
            )

        val result = useCase(userId = 1, date = date)

        assertEquals(1, result.totalSessions)
        assertEquals(600.0, result.averageDurationSeconds, 0.001)
    }

    @Test
    fun invoke_withAppsFromDifferentUsers_excludesOtherUsersSessions() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        val anotherUserApp = app.copy(
            id = 2,
            userId = 2
        )

        restrictedAppRepository.apps =
            listOf(app, anotherUserApp)

        usageSessionRepository.sessions[app.id] =
            mutableListOf(
                createSession(app.id, createDate(2026, Calendar.NOVEMBER, 5), 600)
            )

        usageSessionRepository.sessions[anotherUserApp.id] =
            mutableListOf(
                createSession(anotherUserApp.id, createDate(2026, Calendar.NOVEMBER, 5), 5000)
            )

        val result = useCase(userId = 1, date = date)

        assertEquals(1, result.totalSessions)
        assertEquals(600.0, result.averageDurationSeconds, 0.001)
    }

    @Test
    fun invoke_withoutValidSessions_returnsZeroValues() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        usageSessionRepository.sessions[app.id] =
            mutableListOf(
                createSession(app.id, createDate(2026, Calendar.NOVEMBER, 5), 0),
                createSession(app.id, createDate(2026, Calendar.NOVEMBER, 6), -100)
            )

        val result = useCase(userId = 1, date = date)

        assertEquals(0, result.totalSessions)
        assertEquals(0.0, result.averageDurationSeconds, 0.001)
    }

    //Ragahd-: Fake restricted app repository for average session duration tests.
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

    //Ragahd-: Fake usage session repository for average session duration tests.
    private class FakeUsageSessionRepository : UsageSessionRepository {

        val sessions = mutableMapOf<Int, MutableList<UsageSession>>()

        override suspend fun addSession(session: UsageSession): Long {
            sessions.getOrPut(session.appId) { mutableListOf() }
                .add(session)
            return session.id.toLong()
        }

        override suspend fun getSessionsByAppId(appId: Int) =
            sessions[appId].orEmpty()

        override suspend fun getSessionsForPeriod(
            appId: Int,
            startTime: Long,
            endTime: Long
        ) = sessions[appId].orEmpty().filter {
            it.startTime >= startTime && it.startTime <= endTime
        }

        override suspend fun sessionExists(
            appId: Int,
            startTime: Long,
            endTime: Long
        ) = sessions[appId].orEmpty().any {
            it.startTime >= startTime && it.startTime <= endTime
        }
    }
}
