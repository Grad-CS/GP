package com.example.minder.data.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.minder.data.local.dao.RestrictedAppDao
import com.example.minder.data.local.entities.RestrictedAppEntity
import com.example.minder.data.local.entities.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.example.minder.data.local.database.MinderDatabase

@RunWith(AndroidJUnit4::class)
class RestrictedAppDaoTest {

    private lateinit var database: MinderDatabase
    private lateinit var restrictedAppDao: RestrictedAppDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        restrictedAppDao = database.restrictedAppDao()
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

    @Test
    fun insertApp_andGetAppById_returnsCorrectApp() = runBlocking {

        val userId = createUser()

        val appId = restrictedAppDao.insertApp(
            RestrictedAppEntity(
                userId = userId,
                appName = "Instagram",
                packageName = "com.instagram.android",
                dailyLimit = 30,
                isEnabled = true,
                addedAt = System.currentTimeMillis()
            )
        ).toInt()

        val result = restrictedAppDao.getAppById(appId)

        assertNotNull(result)
        assertEquals("Instagram", result?.appName)
        assertEquals("com.instagram.android", result?.packageName)
        assertEquals(30, result?.dailyLimit)
        assertEquals(userId, result?.userId)
    }

    @Test
    fun samePackageName_forSameUser_isRejected() = runBlocking {

        val userId = createUser()

        val app = RestrictedAppEntity(
            userId = userId,
            appName = "Instagram",
            packageName = "com.instagram.android",
            dailyLimit = 30,
            isEnabled = true,
            addedAt = System.currentTimeMillis()
        )

        restrictedAppDao.insertApp(app)

        try {
            restrictedAppDao.insertApp(
                app.copy(appId = 0)
            )

            fail("Expected SQLiteConstraintException")
        } catch (e: SQLiteConstraintException) {
            // Expected: duplicate userId + packageName is not allowed
        }
    }

    @Test
    fun samePackageName_forDifferentUsers_isAllowed() = runBlocking {

        val firstUserId = createUser()
        val secondUserId = createUser()

        val firstAppId = restrictedAppDao.insertApp(
            RestrictedAppEntity(
                userId = firstUserId,
                appName = "Instagram",
                packageName = "com.instagram.android",
                dailyLimit = 30,
                isEnabled = true,
                addedAt = System.currentTimeMillis()
            )
        ).toInt()

        val secondAppId = restrictedAppDao.insertApp(
            RestrictedAppEntity(
                userId = secondUserId,
                appName = "Instagram",
                packageName = "com.instagram.android",
                dailyLimit = 30,
                isEnabled = true,
                addedAt = System.currentTimeMillis()
            )
        ).toInt()

        assertNotNull(restrictedAppDao.getAppById(firstAppId))
        assertNotNull(restrictedAppDao.getAppById(secondAppId))
        assertEquals(
            firstUserId,
            restrictedAppDao.getAppById(firstAppId)?.userId
        )
        assertEquals(
            secondUserId,
            restrictedAppDao.getAppById(secondAppId)?.userId
        )
    }

    @Test
    fun app_withNonExistingUser_isRejected() = runBlocking {

        val invalidUserId = 9999

        try {
            restrictedAppDao.insertApp(
                RestrictedAppEntity(
                    userId = invalidUserId,
                    appName = "Instagram",
                    packageName = "com.instagram.android",
                    dailyLimit = 30,
                    isEnabled = true,
                    addedAt = System.currentTimeMillis()
                )
            )

            fail("Expected SQLiteConstraintException")
        } catch (e: SQLiteConstraintException) {
            // Expected: userId must exist in USER table
        }
    }

    @Test
    fun deletingUser_deletesItsRestrictedApps() = runBlocking {

        val userId = createUser()

        val appId = restrictedAppDao.insertApp(
            RestrictedAppEntity(
                userId = userId,
                appName = "Instagram",
                packageName = "com.instagram.android",
                dailyLimit = 30,
                isEnabled = true,
                addedAt = System.currentTimeMillis()
            )
        ).toInt()

        assertNotNull(restrictedAppDao.getAppById(appId))

        database.userDao().deleteUser(
            UserEntity(
                userId = userId,
                createdAt = System.currentTimeMillis()
            )
        )

        val result = restrictedAppDao.getAppById(appId)

        assertEquals(null, result)
    }
}