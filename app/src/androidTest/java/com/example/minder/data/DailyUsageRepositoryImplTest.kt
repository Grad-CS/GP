package com.example.minder.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.local.entities.DailyUsageEntity
import com.example.minder.data.local.entities.RestrictedAppEntity
import com.example.minder.data.local.entities.UserEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.example.minder.domain.model.UsageSummary

class DailyUsageRepositoryImplTest {

    private lateinit var database: MinderDatabase
    private lateinit var repository: DailyUsageRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = DailyUsageRepositoryImpl(
            database.dailyUsageDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun createUser(): Int {
        return database.userDao().insertUser(
            UserEntity(
                createdAt = System.currentTimeMillis()
            )
        ).toInt()
    }

    private suspend fun createApp(userId: Int): Int {
        return database.restrictedAppDao().insertApp(
            RestrictedAppEntity(
                userId = userId,
                appName = "Instagram",
                packageName = "com.instagram.android",
                dailyLimit = 60,
                isEnabled = true,
                addedAt = System.currentTimeMillis()
            )
        ).toInt()
    }

    @Test
    fun addDailyUsage_returnsIdAndSavesUsage() = runTest {
        val userId = createUser()
        val appId = createApp(userId)

        val usage = UsageSummary(
            id = 0,
            appId = appId,
            date = 1000L,
            totalUsage = 30,
            savedTime = 10,
            challengeCount = 2
        )

        val dailyUsageId = repository.addDailyUsage(usage)

        val result = repository.getUsageForDate(
            appId = appId,
            date = 1000L
        )

        assertNotNull(result)

        result!!

        assertEquals(dailyUsageId.toInt(), result.id)
        assertEquals(appId, result.appId)
        assertEquals(1000L, result.date)
        assertEquals(30, result.totalUsage)
        assertEquals(10, result.savedTime)
        assertEquals(2, result.challengeCount)
    }

    @Test
    fun getDailyUsageByAppId_returnsCorrectUsageRecords() = runTest {
        val userId = createUser()
        val appId = createApp(userId)

        repository.addDailyUsage(
            UsageSummary(
                id = 0,
                appId = appId,
                date = 1000L,
                totalUsage = 30,
                savedTime = 10,
                challengeCount = 1
            )
        )

        repository.addDailyUsage(
            UsageSummary(
                id = 0,
                appId = appId,
                date = 2000L,
                totalUsage = 45,
                savedTime = 15,
                challengeCount = 2
            )
        )

        val result = repository.getDailyUsageByAppId(appId)

        assertEquals(2, result.size)

        assertTrue(
            result.all {
                it.appId == appId
            }
        )

        assertEquals(2000L, result[0].date)
        assertEquals(1000L, result[1].date)
    }

    @Test
    fun getUsageForPeriod_returnsOnlyRecordsInsidePeriod() = runTest {
        val userId = createUser()
        val appId = createApp(userId)

        repository.addDailyUsage(
            UsageSummary(
                id = 0,
                appId = appId,
                date = 1000L,
                totalUsage = 20,
                savedTime = 5,
                challengeCount = 1
            )
        )

        repository.addDailyUsage(
            UsageSummary(
                id = 0,
                appId = appId,
                date = 3000L,
                totalUsage = 40,
                savedTime = 10,
                challengeCount = 2
            )
        )

        repository.addDailyUsage(
            UsageSummary(
                id = 0,
                appId = appId,
                date = 7000L,
                totalUsage = 60,
                savedTime = 20,
                challengeCount = 3
            )
        )

        val result = repository.getUsageForPeriod(
            appId = appId,
            startDate = 2000L,
            endDate = 6000L
        )

        assertEquals(1, result.size)
        assertEquals(3000L, result[0].date)
        assertEquals(40, result[0].totalUsage)
    }

    @Test
    fun getUsageForDate_returnsCorrectDailyUsage() = runTest {
        val userId = createUser()
        val appId = createApp(userId)

        repository.addDailyUsage(
            UsageSummary(
                id = 0,
                appId = appId,
                date = 5000L,
                totalUsage = 50,
                savedTime = 20,
                challengeCount = 3
            )
        )

        val result = repository.getUsageForDate(
            appId = appId,
            date = 5000L
        )

        assertNotNull(result)

        result!!

        assertEquals(appId, result.appId)
        assertEquals(5000L, result.date)
        assertEquals(50, result.totalUsage)
        assertEquals(20, result.savedTime)
        assertEquals(3, result.challengeCount)
    }

    @Test
    fun updateDailyUsage_updatesExistingRecord() = runTest {
        val userId = createUser()
        val appId = createApp(userId)

        val dailyUsageId = repository.addDailyUsage(
            UsageSummary(
                id = 0,
                appId = appId,
                date = 1000L,
                totalUsage = 30,
                savedTime = 5,
                challengeCount = 1
            )
        ).toInt()

        val updatedUsage = UsageSummary(
            id = dailyUsageId,
            appId = appId,
            date = 1000L,
            totalUsage = 50,
            savedTime = 15,
            challengeCount = 3
        )

        repository.updateDailyUsage(updatedUsage)

        val result = repository.getUsageForDate(
            appId = appId,
            date = 1000L
        )

        assertNotNull(result)

        result!!

        assertEquals(dailyUsageId, result.id)
        assertEquals(50, result.totalUsage)
        assertEquals(15, result.savedTime)
        assertEquals(3, result.challengeCount)
    }

    @Test
    fun repository_mapsEntityToDomainCorrectly() = runTest {
        val userId = createUser()
        val appId = createApp(userId)

        val dailyUsageId = database.dailyUsageDao().insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 9000L,
                totalUsage = 75,
                savedTime = 25,
                challengeCount = 4
            )
        ).toInt()

        val result = repository.getUsageForDate(
            appId = appId,
            date = 9000L
        )

        assertNotNull(result)

        result!!

        assertEquals(dailyUsageId, result.id)
        assertEquals(appId, result.appId)
        assertEquals(9000L, result.date)
        assertEquals(75, result.totalUsage)
        assertEquals(25, result.savedTime)
        assertEquals(4, result.challengeCount)
    }
}