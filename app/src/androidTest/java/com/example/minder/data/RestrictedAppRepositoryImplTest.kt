package com.example.minder.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.minder.data.local.entities.RestrictedAppEntity
import com.example.minder.data.local.entities.UserEntity
import com.example.minder.domain.model.RestrictedApp
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.example.minder.data.local.database.MinderDatabase

@RunWith(AndroidJUnit4::class)
class RestrictedAppRepositoryImplTest {

    private lateinit var database: MinderDatabase
    private lateinit var repository: RestrictedAppRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = RestrictedAppRepositoryImpl(
            database.restrictedAppDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addApp_returnsCorrectId_andSavesApp() = runTest {
        val userId = createUser()

        val app = createRestrictedApp(
            userId = userId,
            appName = "YouTube",
            packageName = "com.google.android.youtube"
        )

        val appId = repository.addApp(app).toInt()

        val savedApp = repository.getAppById(appId)

        assertNotNull(savedApp)
        assertEquals(appId, savedApp!!.id)
        assertEquals(userId, savedApp.userId)
        assertEquals("YouTube", savedApp.appName)
        assertEquals("com.google.android.youtube", savedApp.packageName)
    }

    @Test
    fun getAppsByUserId_returnsOnlyAppsBelongingToUser() = runTest {
        val user1 = createUser()
        val user2 = createUser()

        repository.addApp(
            createRestrictedApp(
                userId = user1,
                appName = "YouTube",
                packageName = "com.google.android.youtube"
            )
        )

        repository.addApp(
            createRestrictedApp(
                userId = user1,
                appName = "Instagram",
                packageName = "com.instagram.android"
            )
        )

        repository.addApp(
            createRestrictedApp(
                userId = user2,
                appName = "TikTok",
                packageName = "com.tiktok.android"
            )
        )

        val user1Apps = repository.getAppsByUserId(user1)

        assertEquals(2, user1Apps.size)
        assertEquals(user1, user1Apps[0].userId)
        assertEquals(user1, user1Apps[1].userId)
    }

    @Test
    fun getAppById_returnsCorrectApp() = runTest {
        val userId = createUser()

        val insertedId = repository.addApp(
            createRestrictedApp(
                userId = userId,
                appName = "Instagram",
                packageName = "com.instagram.android"
            )
        ).toInt()

        val app = repository.getAppById(insertedId)

        assertNotNull(app)
        assertEquals(insertedId, app!!.id)
        assertEquals(userId, app.userId)
        assertEquals("Instagram", app.appName)
        assertEquals("com.instagram.android", app.packageName)
    }

    @Test
    fun getAppByPackageName_usesUserIdAndPackageName() = runTest {
        val user1 = createUser()
        val user2 = createUser()

        repository.addApp(
            createRestrictedApp(
                userId = user1,
                appName = "YouTube",
                packageName = "com.google.android.youtube"
            )
        )

        repository.addApp(
            createRestrictedApp(
                userId = user2,
                appName = "YouTube",
                packageName = "com.google.android.youtube"
            )
        )

        val user1App = repository.getAppByPackageName(
            userId = user1,
            packageName = "com.google.android.youtube"
        )

        val user2App = repository.getAppByPackageName(
            userId = user2,
            packageName = "com.google.android.youtube"
        )

        assertNotNull(user1App)
        assertNotNull(user2App)

        assertEquals(user1, user1App!!.userId)
        assertEquals(user2, user2App!!.userId)
    }

    @Test
    fun updateApp_updatesAppData() = runTest {
        val userId = createUser()

        val appId = repository.addApp(
            createRestrictedApp(
                userId = userId,
                appName = "YouTube",
                packageName = "com.google.android.youtube",
                dailyLimit = 60,
                isEnabled = true
            )
        ).toInt()

        val app = repository.getAppById(appId)

        assertNotNull(app)

        val updatedApp = app!!.copy(
            appName = "YouTube Updated",
            dailyLimit = 120,
            isEnabled = false
        )

        repository.updateApp(updatedApp)

        val result = repository.getAppById(appId)

        assertNotNull(result)
        assertEquals(appId, result!!.id)
        assertEquals(userId, result.userId)
        assertEquals("YouTube Updated", result.appName)
        assertEquals(120, result.dailyLimit)
        assertEquals(false, result.isEnabled)
    }

    @Test
    fun deleteApp_removesApp() = runTest {
        val userId = createUser()

        val appId = repository.addApp(
            createRestrictedApp(
                userId = userId,
                appName = "Instagram",
                packageName = "com.instagram.android"
            )
        ).toInt()

        val app = repository.getAppById(appId)

        assertNotNull(app)

        repository.deleteApp(app!!)

        val deletedApp = repository.getAppById(appId)

        assertNull(deletedApp)
    }

    private suspend fun createUser(): Int {
        return database.userDao().insertUser(
            UserEntity(
                createdAt = System.currentTimeMillis()
            )
        ).toInt()
    }

    private fun createRestrictedApp(
        userId: Int,
        appName: String,
        packageName: String,
        dailyLimit: Int = 60,
        isEnabled: Boolean = true
    ): RestrictedApp {
        return RestrictedApp(
            id = 0,
            userId = userId,
            appName = appName,
            packageName = packageName,
            dailyLimit = dailyLimit,
            isEnabled = isEnabled,
            addedAt = System.currentTimeMillis()
        )
    }
}