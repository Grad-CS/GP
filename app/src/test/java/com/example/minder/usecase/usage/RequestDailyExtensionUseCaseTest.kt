
package com.example.minder.usecase.usage

import com.example.minder.domain.model.ExtensionRequest
import com.example.minder.domain.repository.ExtensionRequestRepository
import com.example.minder.domain.usecase.usage.RequestDailyExtensionUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import org.junit.Assert.fail

class RequestDailyExtensionUseCaseTest {

    private lateinit var repository: FakeExtensionRequestRepository
    private lateinit var useCase: RequestDailyExtensionUseCase

    @Before
    fun setup() {
        repository = FakeExtensionRequestRepository()
        useCase = RequestDailyExtensionUseCase(repository)
    }

    @Test
    fun validTenMinuteExtensionIsSaved() = runTest {
        val result = useCase(
            appId = 1,
            selectedMinutes = 10,
            requestTime = createTime(2026, Calendar.NOVEMBER, 13, 10)
        )

        assertNotNull(result)
        assertEquals(10, repository.savedRequest?.requestedMinutes)
        assertEquals(10, repository.savedRequest?.grantedMinutes)
        assertEquals("ACTIVE", repository.savedRequest?.status)
    }

    @Test
    fun validFifteenMinuteExtensionIsSaved() = runTest {
        val result = useCase(
            appId = 1,
            selectedMinutes = 15,
            requestTime = createTime(2026, Calendar.NOVEMBER, 13, 10)
        )

        assertNotNull(result)
        assertEquals(15, repository.savedRequest?.grantedMinutes)
    }

    @Test
    fun validTwentyMinuteExtensionIsSaved() = runTest {
        val result = useCase(
            appId = 1,
            selectedMinutes = 20,
            requestTime = createTime(2026, Calendar.NOVEMBER, 13, 10)
        )

        assertNotNull(result)
        assertEquals(20, repository.savedRequest?.grantedMinutes)
    }


    @Test
    fun invalidExtensionDurationIsRejected() = runTest {
        try {
            useCase(
                appId = 1,
                selectedMinutes = 5,
                requestTime = createTime(2026, Calendar.NOVEMBER, 13, 10)
            )

            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            //Ragahd-: Confirms that unsupported extension durations are rejected.
        }
    }


    @Test
    fun secondExtensionOnSameDayIsRejected() = runTest {
        val requestTime = createTime(2026, Calendar.NOVEMBER, 13, 10)

        val firstResult = useCase(
            appId = 1,
            selectedMinutes = 10,
            requestTime = requestTime
        )

        val secondResult = useCase(
            appId = 1,
            selectedMinutes = 15,
            requestTime = requestTime + 60 * 60 * 1000L
        )

        assertNotNull(firstResult)
        assertNull(secondResult)
    }

    @Test
    fun extensionCanBeRequestedAgainOnNextDay() = runTest {
        val firstDay = createTime(2026, Calendar.NOVEMBER, 13, 10)
        val secondDay = createTime(2026, Calendar.NOVEMBER, 14, 10)

        val firstResult = useCase(
            appId = 1,
            selectedMinutes = 10,
            requestTime = firstDay
        )

        val secondResult = useCase(
            appId = 1,
            selectedMinutes = 20,
            requestTime = secondDay
        )

        assertNotNull(firstResult)
        assertNotNull(secondResult)
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

        var savedRequest: ExtensionRequest? = null
            private set

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

            val saved = request.copy(id = requests.size + 1)
            requests.add(saved)
            savedRequest = saved

            return saved.id.toLong()
        }
    }
}
