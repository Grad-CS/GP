
package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.model.UsageSession
import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.UsageSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class GetPeakUsageTimesUseCaseTest {

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

    private val useCase = GetPeakUsageTimesUseCase(
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

    //Ragahd-: Create a usage session with explicit start and end times.
    private fun createSession(
        appId: Int,
        startTime: Long,
        endTime: Long,
        duration: Int
    ) = UsageSession(
        id = 0,
        appId = appId,
        startTime = startTime,
        endTime = endTime,
        duration = duration
    )

    @Test
    fun invoke_withSessionAcrossMultipleHours_distributesUsageCorrectly() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        usageSessionRepository.sessions[app.id] = mutableListOf(
            createSession(
                appId = app.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 8, 9, 30),
                endTime = createDate(2026, Calendar.NOVEMBER, 8, 11, 30),
                duration = 7200
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(24, result.hourlyStats.size)
        assertEquals(1800L, result.hourlyStats[9].totalUsageSeconds)
        assertEquals(3600L, result.hourlyStats[10].totalUsageSeconds)
        assertEquals(1800L, result.hourlyStats[11].totalUsageSeconds)
    }

    @Test
    fun invoke_withMultipleSessions_identifiesCorrectPeakHour() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        usageSessionRepository.sessions[app.id] = mutableListOf(
            createSession(
                appId = app.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 8, 9),
                endTime = createDate(2026, Calendar.NOVEMBER, 8, 9, 30),
                duration = 1800
            ),
            createSession(
                appId = app.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 8, 10),
                endTime = createDate(2026, Calendar.NOVEMBER, 8, 11),
                duration = 3600
            ),
            createSession(
                appId = app.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 9, 10),
                endTime = createDate(2026, Calendar.NOVEMBER, 9, 10, 30),
                duration = 1800
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(10, result.peakHour)
        assertEquals(5400L, result.peakUsageSeconds)
    }

    @Test
    fun invoke_withSessionAcrossMidnight_distributesUsageAcrossBothDays() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        usageSessionRepository.sessions[app.id] = mutableListOf(
            createSession(
                appId = app.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 6, 23, 30),
                endTime = createDate(2026, Calendar.NOVEMBER, 7, 0, 30),
                duration = 3600
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(1800L, result.hourlyStats[23].totalUsageSeconds)
        assertEquals(1800L, result.hourlyStats[0].totalUsageSeconds)
    }

    @Test
    fun invoke_withSessionStartingBeforePeriod_clipsUsageToPeriodStart() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        val periodStart = createDate(2026, Calendar.NOVEMBER, 4)

        usageSessionRepository.sessions[app.id] = mutableListOf(
            createSession(
                appId = app.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 3, 23),
                endTime = createDate(2026, Calendar.NOVEMBER, 4, 1),
                duration = 7200
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(periodStart, result.periodStart)
        assertEquals(3600L, result.hourlyStats[0].totalUsageSeconds)
        assertEquals(3600L, result.peakUsageSeconds)
        assertEquals(0, result.peakHour)
    }

    @Test
    fun invoke_withSessionEndingAfterPeriod_clipsUsageToPeriodEnd() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        usageSessionRepository.sessions[app.id] = mutableListOf(
            createSession(
                appId = app.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 10, 17),
                endTime = createDate(2026, Calendar.NOVEMBER, 10, 19),
                duration = 7200
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(date, result.periodEnd)
        assertEquals(3600L, result.hourlyStats[17].totalUsageSeconds)
        assertEquals(3600L, result.peakUsageSeconds)
        assertEquals(17, result.peakHour)
    }

    @Test
    fun invoke_withInvalidSessions_excludesThem() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        usageSessionRepository.sessions[app.id] = mutableListOf(
            createSession(
                appId = app.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 8, 10),
                endTime = createDate(2026, Calendar.NOVEMBER, 8, 10, 10),
                duration = 600
            ),
            createSession(
                appId = app.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 8, 11),
                endTime = createDate(2026, Calendar.NOVEMBER, 8, 11),
                duration = 0
            ),
            createSession(
                appId = app.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 8, 12),
                endTime = createDate(2026, Calendar.NOVEMBER, 8, 11),
                duration = 600
            ),
            createSession(
                appId = app.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 8, 13),
                endTime = createDate(2026, Calendar.NOVEMBER, 8, 13, 10),
                duration = -600
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(600L, result.hourlyStats[10].totalUsageSeconds)
        assertEquals(600L, result.peakUsageSeconds)
    }

    @Test
    fun invoke_withDisabledApp_excludesItsSessions() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        val disabledApp = app.copy(
            id = 2,
            isEnabled = false
        )

        restrictedAppRepository.apps = listOf(app, disabledApp)

        usageSessionRepository.sessions[app.id] = mutableListOf(
            createSession(
                appId = app.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 8, 10),
                endTime = createDate(2026, Calendar.NOVEMBER, 8, 10, 10),
                duration = 600
            )
        )

        usageSessionRepository.sessions[disabledApp.id] = mutableListOf(
            createSession(
                appId = disabledApp.id,
                startTime = createDate(2026, Calendar.NOVEMBER, 8, 11),
                endTime = createDate(2026, Calendar.NOVEMBER, 8, 12),
                duration = 3600
            )
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(600L, result.hourlyStats[10].totalUsageSeconds)
        assertEquals(600L, result.peakUsageSeconds)
        assertEquals(10, result.peakHour)
    }

    @Test
    fun invoke_withoutSessions_returnsZeroValues() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        val result = useCase(userId = 1, date = date)

        assertEquals(24, result.hourlyStats.size)
        assertNull(result.peakHour)
        assertEquals(0L, result.peakUsageSeconds)

        result.hourlyStats.forEachIndexed { hour, stats ->
            assertEquals(hour, stats.hourOfDay)
            assertEquals(0L, stats.totalUsageSeconds)
        }
    }

    //Ragahd-: Fake restricted app repository for peak usage tests.
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

    //Ragahd-: Fake usage session repository for peak usage tests.
    private class FakeUsageSessionRepository : UsageSessionRepository {

        val sessions = mutableMapOf<Int, MutableList<UsageSession>>()

        override suspend fun addSession(session: UsageSession): Long {
            sessions.getOrPut(session.appId) { mutableListOf() }.add(session)
            return session.id.toLong()
        }

        override suspend fun getSessionsByAppId(appId: Int) =
            sessions[appId].orEmpty()

        //Ragahd-: Retrieve sessions that overlap with the requested period, matching the Room DAO query.
        override suspend fun getSessionsForPeriod(
            appId: Int,
            startTime: Long,
            endTime: Long
        ) = sessions[appId].orEmpty().filter {
            it.endTime > startTime && it.startTime < endTime
        }

        override suspend fun sessionExists(
            appId: Int,
            startTime: Long,
            endTime: Long
        ) = sessions[appId].orEmpty().any {
            it.startTime == startTime && it.endTime == endTime
        }
    }
}
