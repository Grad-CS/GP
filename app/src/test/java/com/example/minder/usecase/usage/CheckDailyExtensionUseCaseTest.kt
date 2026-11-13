
package com.example.minder.usecase.usage

import com.example.minder.domain.model.ExtensionRequest
import com.example.minder.domain.repository.ExtensionRequestRepository
import com.example.minder.domain.usecase.usage.CheckDailyExtensionUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class CheckDailyExtensionUseCaseTest {

    private lateinit var repository: FakeExtensionRequestRepository
    private lateinit var useCase: CheckDailyExtensionUseCase

    @Before
    fun setup() {
        repository = FakeExtensionRequestRepository()
        useCase = CheckDailyExtensionUseCase(repository)
    }

    @Test
    fun returnsFalseWhenNoExtensionExists() = runTest {
        val result = useCase(
            appId = 1,
            currentTime = createTime(2026, Calendar.NOVEMBER, 13, 10)
        )

        assertFalse(result)
    }

    @Test
    fun returnsTrueBeforeExtensionExpires() = runTest {
        val requestTime = createTime(2026, Calendar.NOVEMBER, 13, 10)
        repository.addRequest(
            appId = 1,
            requestTime = requestTime,
            grantedMinutes = 10
        )

        val result = useCase(
            appId = 1,
            currentTime = requestTime + 5 * 60_000L
        )

        assertTrue(result)
    }

    @Test
    fun returnsFalseExactlyAtExpirationTime() = runTest {
        val requestTime = createTime(2026, Calendar.NOVEMBER, 13, 10)
        repository.addRequest(
            appId = 1,
            requestTime = requestTime,
            grantedMinutes = 10
        )

        val result = useCase(
            appId = 1,
            currentTime = requestTime + 10 * 60_000L
        )

        assertFalse(result)
    }

    @Test
    fun returnsFalseAfterExtensionExpires() = runTest {
        val requestTime = createTime(2026, Calendar.NOVEMBER, 13, 10)
        repository.addRequest(
            appId = 1,
            requestTime = requestTime,
            grantedMinutes = 15
        )

        val result = useCase(
            appId = 1,
            currentTime = requestTime + 16 * 60_000L
        )

        assertFalse(result)
    }

    @Test
    fun previousDayExtensionIsNotActiveToday() = runTest {
        val previousDay = createTime(2026, Calendar.NOVEMBER, 12, 23)
        val currentDay = createTime(2026, Calendar.NOVEMBER, 13, 1)

        repository.addRequest(
            appId = 1,
            requestTime = previousDay,
            grantedMinutes = 20
        )

        val result = useCase(
            appId = 1,
            currentTime = currentDay
        )

        assertFalse(result)
    }

    @Test
    fun extensionForAnotherAppDoesNotApply() = runTest {
        val requestTime = createTime(2026, Calendar.NOVEMBER, 13, 10)
        repository.addRequest(
            appId = 2,
            requestTime = requestTime,
            grantedMinutes = 20
        )

        val result = useCase(
            appId = 1,
            currentTime = requestTime + 5 * 60_000L
        )

        assertFalse(result)
    }

    private fun createTime(
        year: Int,
        month: Int,
        day: Int,
        hour: Int
    ): Long {
        return Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, 0, 0)
        }.timeInMillis
    }

    private class FakeExtensionRequestRepository :
        ExtensionRequestRepository {

        private val requests = mutableListOf<ExtensionRequest>()

        fun addRequest(
            appId: Int,
            requestTime: Long,
            grantedMinutes: Int
        ) {
            requests.add(
                ExtensionRequest(
                    id = requests.size + 1,
                    appId = appId,
                    requestedMinutes = grantedMinutes,
                    grantedMinutes = grantedMinutes,
                    requestTime = requestTime,
                    status = "ACTIVE"
                )
            )
        }

        override suspend fun getRequestForAppOnDate(
            appId: Int,
            dayStart: Long,
            nextDayStart: Long
        ): ExtensionRequest? {
            return requests.firstOrNull {
                it.appId == appId &&
                        it.requestTime >= dayStart &&
                        it.requestTime < nextDayStart
            }
        }

        override suspend fun addDailyExtensionIfAbsent(
            request: ExtensionRequest,
            dayStart: Long,
            nextDayStart: Long
        ): Long? {
            if (
                getRequestForAppOnDate(
                    request.appId,
                    dayStart,
                    nextDayStart
                ) != null
            ) {
                return null
            }

            requests.add(request)
            return request.id.toLong()
        }
    }
}
