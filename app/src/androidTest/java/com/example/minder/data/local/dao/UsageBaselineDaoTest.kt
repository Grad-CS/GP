package com.example.minder.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.minder.data.local.database.MinderDatabase
import com.example.minder.data.local.entities.RestrictedAppEntity
import com.example.minder.data.local.entities.UsageBaselineEntity
import com.example.minder.data.local.entities.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class UsageBaselineDaoTest {

    private lateinit var database: MinderDatabase
    private lateinit var usageBaselineDao: UsageBaselineDao
    private lateinit var restrictedAppDao: RestrictedAppDao
    private lateinit var userDao: UserDao

    private var appId: Int = 0

    private var userId: Int = 0

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        usageBaselineDao = database.usageBaselineDao()
        restrictedAppDao = database.restrictedAppDao()
        userDao = database.userDao()

        //Ragahd-: Insert a user to satisfy the restricted app foreign key.
        userId = userDao.insertUser(
            UserEntity(createdAt = System.currentTimeMillis())
        ).toInt()

        //Ragahd-: Insert a restricted app for baseline test records.
        appId = restrictedAppDao.insertApp(
            RestrictedAppEntity(
                userId = userId,
                appName = "Test App",
                packageName = "com.example.testapp",
                dailyLimit = 60,
                isEnabled = true,
                addedAt = System.currentTimeMillis()
            )
        ).toInt()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun saveBaselinesIfAbsent_withSevenValidDays_returnsTrue() = runBlocking {
        val records = createBaselines(appId, 100)

        val result = usageBaselineDao.saveBaselinesIfAbsent(records)

        assertTrue(result)
        assertEquals(7, usageBaselineDao.getBaselineByAppId(appId).size)
    }

    @Test
    fun saveBaselinesIfAbsent_whenBaselineAlreadyExists_returnsFalseAndPreservesOldData() =
        runBlocking {
            val originalRecords = createBaselines(appId, 100)
            val newRecords = createBaselines(appId, 500)

            assertTrue(
                usageBaselineDao.saveBaselinesIfAbsent(originalRecords)
            )

            val result = usageBaselineDao.saveBaselinesIfAbsent(newRecords)

            assertFalse(result)

            val savedRecords = usageBaselineDao.getBaselineByAppId(appId)

            assertEquals(7, savedRecords.size)
            assertTrue(
                savedRecords.all { it.averageUsageSeconds == 100 }
            )
        }

    @Test
    fun saveBaselinesIfAbsent_withIncompleteRecords_returnsFalse() = runBlocking {
        val records = createBaselines(appId, 100).take(6)

        val result = usageBaselineDao.saveBaselinesIfAbsent(records)

        assertFalse(result)
        assertTrue(usageBaselineDao.getBaselineByAppId(appId).isEmpty())
    }

    @Test
    fun saveBaselinesIfAbsent_withDuplicateWeekday_returnsFalse() = runBlocking {
        val records = createBaselines(appId, 100).toMutableList()
        records[6] = records[0].copy(baselineId = 0)

        val result = usageBaselineDao.saveBaselinesIfAbsent(records)

        assertFalse(result)
        assertTrue(usageBaselineDao.getBaselineByAppId(appId).isEmpty())
    }

    @Test
    fun saveBaselinesIfAbsent_withMixedApplications_returnsFalse() = runBlocking {
        //Ragahd-: Create a second restricted app for the mixed-app test.
        val secondAppId = restrictedAppDao.insertApp(
            RestrictedAppEntity(
                userId = 1,
                appName = "Second App",
                packageName = "com.example.secondapp",
                dailyLimit = 30,
                isEnabled = true,
                addedAt = System.currentTimeMillis()
            )
        ).toInt()

        val records = createBaselines(appId, 100).toMutableList()
        records[6] = records[6].copy(appId = secondAppId)

        val result = usageBaselineDao.saveBaselinesIfAbsent(records)

        assertFalse(result)
        assertTrue(usageBaselineDao.getBaselineByAppId(appId).isEmpty())
        assertTrue(usageBaselineDao.getBaselineByAppId(secondAppId).isEmpty())
    }

    @Test
    fun saveBaselinesIfAbsent_withInvalidWeekday_returnsFalse() = runBlocking {
        val records = createBaselines(appId, 100).toMutableList()
        records[6] = records[6].copy(dayOfWeek = 8)

        val result = usageBaselineDao.saveBaselinesIfAbsent(records)

        assertFalse(result)
        assertTrue(usageBaselineDao.getBaselineByAppId(appId).isEmpty())
    }

    //Ragahd-: Generate seven baseline records for Sunday through Saturday.
    private fun createBaselines(
        targetAppId: Int,
        usageSeconds: Int
    ): List<UsageBaselineEntity> {
        return (Calendar.SUNDAY..Calendar.SATURDAY).map { day ->
            UsageBaselineEntity(
                baselineId = 0,
                appId = targetAppId,
                dayOfWeek = day,
                averageUsageSeconds = usageSeconds,
                sampleCount = 2,
                periodStart = 1_790_000_000_000L,
                periodEnd = 1_791_209_600_000L,
                capturedAt = System.currentTimeMillis()
            )
        }
    }
}
