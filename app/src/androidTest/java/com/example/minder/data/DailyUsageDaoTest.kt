package com.example.minder.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.minder.data.local.dao.DailyUsageDao
import com.example.minder.data.local.entities.DailyUsageEntity
import com.example.minder.data.local.entities.RestrictedAppEntity
import com.example.minder.data.local.entities.UserEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.runner.RunWith
import android.database.sqlite.SQLiteConstraintException
import com.example.minder.data.local.database.MinderDatabase

@RunWith(AndroidJUnit4::class)
class DailyUsageDaoTest {

    private lateinit var database: MinderDatabase
    private lateinit var dailyUsageDao: DailyUsageDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dailyUsageDao = database.dailyUsageDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertDailyUsage_andGetUsageForDate_returnsCorrectUsage() = runTest {
        val appId = createApp()

        val usageId = dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 1000L,
                totalUsage = 60,
                savedTime = 15,
                challengeCount = 2
            )
        ).toInt()

        val usage = dailyUsageDao.getUsageForDate(
            appId = appId,
            date = 1000L
        )

        assertNotNull(usage)
        assertEquals(usageId, usage!!.dailyUsageId)
        assertEquals(appId, usage.appId)
        assertEquals(1000L, usage.date)
        assertEquals(60, usage.totalUsage)
        assertEquals(15, usage.savedTime)
        assertEquals(2, usage.challengeCount)
    }

    @Test
    fun getDailyUsageByAppId_returnsUsageForCorrectApp() = runTest {
        val appId = createApp()

        dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 1000L,
                totalUsage = 60,
                savedTime = 10,
                challengeCount = 1
            )
        )

        dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 2000L,
                totalUsage = 90,
                savedTime = 20,
                challengeCount = 2
            )
        )

        val usageRecords =
            dailyUsageDao.getDailyUsageByAppId(appId)

        assertEquals(2, usageRecords.size)
        assertEquals(appId, usageRecords[0].appId)
        assertEquals(appId, usageRecords[1].appId)
    }

    @Test
    fun getDailyUsageByAppId_returnsNewestDateFirst() = runTest {
        val appId = createApp()

        dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 1000L,
                totalUsage = 30,
                savedTime = 5,
                challengeCount = 1
            )
        )

        dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 3000L,
                totalUsage = 90,
                savedTime = 15,
                challengeCount = 3
            )
        )

        dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 2000L,
                totalUsage = 60,
                savedTime = 10,
                challengeCount = 2
            )
        )

        val usageRecords =
            dailyUsageDao.getDailyUsageByAppId(appId)

        assertEquals(3, usageRecords.size)
        assertEquals(3000L, usageRecords[0].date)
        assertEquals(2000L, usageRecords[1].date)
        assertEquals(1000L, usageRecords[2].date)
    }

    @Test
    fun getUsageForPeriod_returnsUsageWithinDateRange() = runTest {
        val appId = createApp()

        dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 1000L,
                totalUsage = 30,
                savedTime = 5,
                challengeCount = 1
            )
        )

        dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 2000L,
                totalUsage = 60,
                savedTime = 10,
                challengeCount = 2
            )
        )

        dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 3000L,
                totalUsage = 90,
                savedTime = 15,
                challengeCount = 3
            )
        )

        val usageRecords =
            dailyUsageDao.getUsageForPeriod(
                appId = appId,
                startDate = 2000L,
                endDate = 3000L
            )

        assertEquals(1, usageRecords.size)
        assertEquals(2000L, usageRecords[0].date)
    }

    @Test
    fun updateDailyUsage_updatesUsageValues() = runTest {
        val appId = createApp()

        val usageId = dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 1000L,
                totalUsage = 60,
                savedTime = 10,
                challengeCount = 1
            )
        ).toInt()

        val usage = dailyUsageDao.getUsageForDate(
            appId = appId,
            date = 1000L
        )

        assertNotNull(usage)

        val updatedUsage = usage!!.copy(
            dailyUsageId = usageId,
            totalUsage = 120,
            savedTime = 30,
            challengeCount = 4
        )

        dailyUsageDao.updateDailyUsage(updatedUsage)

        val result = dailyUsageDao.getUsageForDate(
            appId = appId,
            date = 1000L
        )

        assertNotNull(result)
        assertEquals(120, result!!.totalUsage)
        assertEquals(30, result.savedTime)
        assertEquals(4, result.challengeCount)
    }

    @Test
    fun deleteDailyUsage_removesUsageRecord() = runTest {
        val appId = createApp()

        dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 1000L,
                totalUsage = 60,
                savedTime = 10,
                challengeCount = 1
            )
        )

        val usage = dailyUsageDao.getUsageForDate(
            appId = appId,
            date = 1000L
        )

        assertNotNull(usage)

        dailyUsageDao.deleteDailyUsage(usage!!)

        val deletedUsage =
            dailyUsageDao.getUsageForDate(
                appId = appId,
                date = 1000L
            )

        assertNull(deletedUsage)
    }

    @Test
    fun sameApp_sameDate_cannotHaveTwoDailyUsageRecords() = runTest {
        val appId = createApp()

        dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 1000L,
                totalUsage = 60,
                savedTime = 10,
                challengeCount = 1
            )
        )

        try {
            dailyUsageDao.insertDailyUsage(
                DailyUsageEntity(
                    appId = appId,
                    date = 1000L,
                    totalUsage = 90,
                    savedTime = 20,
                    challengeCount = 2
                )
            )

            throw AssertionError("Expected duplicate insert to fail")

        } catch (e: SQLiteConstraintException) {
            // Expected
        }
    }

    @Test
    fun deletingRestrictedApp_deletesItsDailyUsageRecords() = runTest {
        val appId = createApp()

        dailyUsageDao.insertDailyUsage(
            DailyUsageEntity(
                appId = appId,
                date = 1000L,
                totalUsage = 60,
                savedTime = 10,
                challengeCount = 1
            )
        )

        database.restrictedAppDao().deleteApp(
            database.restrictedAppDao().getAppById(appId)!!
        )

        val usageRecords =
            dailyUsageDao.getDailyUsageByAppId(appId)

        assertEquals(0, usageRecords.size)
    }

    private suspend fun createApp(): Int {
        val userId = database.userDao().insertUser(
            UserEntity(
                createdAt = System.currentTimeMillis()
            )
        ).toInt()

        return database.restrictedAppDao().insertApp(
            RestrictedAppEntity(
                userId = userId,
                appName = "Test App",
                packageName = "com.example.testapp.$userId",
                dailyLimit = 60,
                isEnabled = true,
                addedAt = System.currentTimeMillis()
            )
        ).toInt()
    }
}