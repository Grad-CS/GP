
package com.example.minder.domain.usecase.challenge

import com.example.minder.domain.model.AttemptsAndChallengeTypesStatistics
import com.example.minder.domain.model.ChallengeAttempt
import com.example.minder.domain.model.ChallengeAttemptWithType
import com.example.minder.domain.model.ChallengeType
import com.example.minder.domain.repository.ChallengeAttemptRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar

class GetAttemptsAndChallengeTypesStatisticsUseCaseTest {

    private val challengeAttemptRepository =
        FakeChallengeAttemptRepository()

    private val useCase = GetAttemptsAndChallengeTypesStatisticsUseCase(
        challengeAttemptRepository = challengeAttemptRepository
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

    //Ragahd-: Create an attempt with a configurable challenge type and result.
    private fun createAttempt(
        interventionId: Int,
        challengeId: Int,
        isCorrect: Boolean,
        attemptTime: Long,
        challengeType: ChallengeType
    ) = ChallengeAttemptWithType(
        interventionId = interventionId,
        challengeId = challengeId,
        isCorrect = isCorrect,
        attemptTime = attemptTime,
        challengeType = challengeType
    )

    @Test
    fun invoke_withDifferentResults_countsAttemptsCorrectly() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        challengeAttemptRepository.attempts = listOf(
            createAttempt(1, 1, true, createDate(2026, Calendar.NOVEMBER, 5, 10), ChallengeType.MATH),
            createAttempt(1, 1, false, createDate(2026, Calendar.NOVEMBER, 5, 10, 1), ChallengeType.MATH),
            createAttempt(2, 2, true, createDate(2026, Calendar.NOVEMBER, 6, 10), ChallengeType.PUZZLE),
            createAttempt(3, 3, false, createDate(2026, Calendar.NOVEMBER, 7, 10), ChallengeType.LOGIC)
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(4, result.totalAttempts)
        assertEquals(2, result.correctAttempts)
        assertEquals(2, result.incorrectAttempts)
    }

    @Test
    fun invoke_withMultipleAttemptsPerIntervention_calculatesAverageCorrectly() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        challengeAttemptRepository.attempts = listOf(
            createAttempt(1, 1, true, createDate(2026, Calendar.NOVEMBER, 5, 10), ChallengeType.MATH),
            createAttempt(1, 1, false, createDate(2026, Calendar.NOVEMBER, 5, 10, 1), ChallengeType.MATH),
            createAttempt(1, 2, true, createDate(2026, Calendar.NOVEMBER, 5, 10, 2), ChallengeType.PUZZLE),
            createAttempt(2, 3, true, createDate(2026, Calendar.NOVEMBER, 6, 10), ChallengeType.LOGIC),
            createAttempt(2, 3, false, createDate(2026, Calendar.NOVEMBER, 6, 10, 1), ChallengeType.LOGIC)
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(5, result.totalAttempts)
        assertEquals(2.5, result.averageAttemptsPerIntervention, 0.001)
    }

    @Test
    fun invoke_withDifferentChallengeTypes_calculatesTypeStatisticsCorrectly() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        challengeAttemptRepository.attempts = listOf(
            createAttempt(1, 1, true, createDate(2026, Calendar.NOVEMBER, 5, 10), ChallengeType.MATH),
            createAttempt(1, 1, true, createDate(2026, Calendar.NOVEMBER, 5, 10, 1), ChallengeType.MATH),
            createAttempt(2, 2, false, createDate(2026, Calendar.NOVEMBER, 6, 10), ChallengeType.MATH),
            createAttempt(3, 3, true, createDate(2026, Calendar.NOVEMBER, 7, 10), ChallengeType.PUZZLE),
            createAttempt(4, 4, false, createDate(2026, Calendar.NOVEMBER, 8, 10), ChallengeType.LOGIC)
        )

        val result = useCase(userId = 1, date = date)

        val mathStats = result.attemptsByChallengeType[ChallengeType.MATH]
        val puzzleStats = result.attemptsByChallengeType[ChallengeType.PUZZLE]
        val logicStats = result.attemptsByChallengeType[ChallengeType.LOGIC]

        assertNotNull(mathStats)
        assertNotNull(puzzleStats)
        assertNotNull(logicStats)

        assertEquals(3, mathStats!!.totalAttempts)
        assertEquals(2, mathStats.correctAttempts)
        assertEquals(1, mathStats.incorrectAttempts)
        assertEquals(66.666, mathStats.successRatePercentage, 0.01)

        assertEquals(1, puzzleStats!!.totalAttempts)
        assertEquals(1, puzzleStats.correctAttempts)
        assertEquals(0, puzzleStats.incorrectAttempts)
        assertEquals(100.0, puzzleStats.successRatePercentage, 0.001)

        assertEquals(1, logicStats!!.totalAttempts)
        assertEquals(0, logicStats.correctAttempts)
        assertEquals(1, logicStats.incorrectAttempts)
        assertEquals(0.0, logicStats.successRatePercentage, 0.001)
    }

    @Test
    fun invoke_withoutAttemptsForSomeTypes_returnsZeroStatisticsForThoseTypes() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        challengeAttemptRepository.attempts = listOf(
            createAttempt(1, 1, true, createDate(2026, Calendar.NOVEMBER, 5, 10), ChallengeType.MATH)
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(3, result.attemptsByChallengeType.size)

        val mathStats = result.attemptsByChallengeType[ChallengeType.MATH]
        val puzzleStats = result.attemptsByChallengeType[ChallengeType.PUZZLE]
        val logicStats = result.attemptsByChallengeType[ChallengeType.LOGIC]

        assertNotNull(mathStats)
        assertNotNull(puzzleStats)
        assertNotNull(logicStats)

        assertEquals(0, puzzleStats!!.totalAttempts)
        assertEquals(0, puzzleStats.correctAttempts)
        assertEquals(0, puzzleStats.incorrectAttempts)
        assertEquals(0.0, puzzleStats.successRatePercentage, 0.001)

        assertEquals(0, logicStats!!.totalAttempts)
        assertEquals(0, logicStats.correctAttempts)
        assertEquals(0, logicStats.incorrectAttempts)
        assertEquals(0.0, logicStats.successRatePercentage, 0.001)
    }

    @Test
    fun invoke_withAttemptsOutsidePeriod_excludesThem() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        challengeAttemptRepository.attempts = listOf(
            createAttempt(1, 1, true, createDate(2026, Calendar.NOVEMBER, 3, 23, 59), ChallengeType.MATH),
            createAttempt(2, 2, true, createDate(2026, Calendar.NOVEMBER, 4), ChallengeType.PUZZLE),
            createAttempt(3, 3, false, date, ChallengeType.LOGIC),
            createAttempt(4, 4, true, date + 1000L, ChallengeType.MATH)
        )

        val result = useCase(userId = 1, date = date)

        assertEquals(2, result.totalAttempts)
        assertEquals(1, result.correctAttempts)
        assertEquals(1, result.incorrectAttempts)
    }

    @Test
    fun invoke_withoutAttempts_returnsZeroValues() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        val result = useCase(userId = 1, date = date)

        assertEquals(0, result.totalAttempts)
        assertEquals(0, result.correctAttempts)
        assertEquals(0, result.incorrectAttempts)
        assertEquals(0.0, result.averageAttemptsPerIntervention, 0.001)

        assertEquals(3, result.attemptsByChallengeType.size)

        ChallengeType.values().forEach { type ->
            val stats = result.attemptsByChallengeType[type]

            assertNotNull(stats)
            assertEquals(0, stats!!.totalAttempts)
            assertEquals(0, stats.correctAttempts)
            assertEquals(0, stats.incorrectAttempts)
            assertEquals(0.0, stats.successRatePercentage, 0.001)
        }
    }

    @Test
    fun invoke_passesUserAndPeriodToRepository() = runTest {

        val date = createDate(2026, Calendar.NOVEMBER, 10, 18)

        useCase(userId = 7, date = date)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -6)
        }

        assertEquals(7, challengeAttemptRepository.lastUserId)
        assertEquals(calendar.timeInMillis, challengeAttemptRepository.lastStartTime)
        assertEquals(date, challengeAttemptRepository.lastEndTime)
    }

    //Ragahd-: Fake challenge attempt repository for statistics tests.
    private class FakeChallengeAttemptRepository : ChallengeAttemptRepository {

        var attempts: List<ChallengeAttemptWithType> = emptyList()

        var lastUserId: Int? = null
        var lastStartTime: Long? = null
        var lastEndTime: Long? = null

        override suspend fun addAttempt(
            attempt: ChallengeAttempt
        ): Long = attempt.id.toLong()

        override suspend fun getAttemptsByInterventionId(
            interventionId: Int
        ): List<ChallengeAttempt> = emptyList()

        override suspend fun getAttemptsByChallengeId(
            challengeId: Int
        ): List<ChallengeAttempt> = emptyList()

        override suspend fun getSuccessfulAttempts(
            interventionId: Int
        ): List<ChallengeAttempt> = emptyList()

        override suspend fun getAttemptCount(
            interventionId: Int
        ): Int = 0

        //Ragahd-: Retrieves attempts matching the requested user and period.
        override suspend fun getAttemptsWithTypeForPeriod(
            userId: Int,
            startTime: Long,
            endTime: Long
        ): List<ChallengeAttemptWithType> {
            lastUserId = userId
            lastStartTime = startTime
            lastEndTime = endTime

            return attempts.filter {
                it.attemptTime >= startTime && it.attemptTime <= endTime
            }
        }
    }
}
