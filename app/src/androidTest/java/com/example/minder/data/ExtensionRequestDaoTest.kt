package com.example.minder.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.minder.data.local.dao.ExtensionRequestDao
import com.example.minder.data.local.entities.ExtensionRequestEntity
import com.example.minder.data.local.entities.RestrictedAppEntity
import com.example.minder.data.local.entities.UserEntity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import kotlinx.coroutines.test.runTest
import com.example.minder.data.local.database.MinderDatabase

@RunWith(AndroidJUnit4::class)
class ExtensionRequestDaoTest {

    private lateinit var database: MinderDatabase
    private lateinit var extensionRequestDao: ExtensionRequestDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        extensionRequestDao = database.extensionRequestDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertRequest_andGetRequestById_returnsCorrectRequest() = runTest {
        val appId = createApp()

        val requestId = extensionRequestDao.insertRequest(
            ExtensionRequestEntity(
                appId = appId,
                requestedMinutes = 30,
                grantedMinutes = 15,
                requestTime = 1000L,
                status = "GRANTED"
            )
        ).toInt()

        val request = extensionRequestDao.getRequestById(requestId)

        assertNotNull(request)
        assertEquals(requestId, request!!.extensionId)
        assertEquals(appId, request!!.appId)
        assertEquals(30, request!!.requestedMinutes)
        assertEquals(15, request!!.grantedMinutes)
        assertEquals("GRANTED", request!!.status)
    }

    @Test
    fun getRequestsByAppId_returnsRequestsForCorrectApp() = runTest {
        val appId = createApp()

        extensionRequestDao.insertRequest(
            ExtensionRequestEntity(
                appId = appId,
                requestedMinutes = 30,
                grantedMinutes = 10,
                requestTime = 1000L,
                status = "GRANTED"
            )
        )

        extensionRequestDao.insertRequest(
            ExtensionRequestEntity(
                appId = appId,
                requestedMinutes = 20,
                grantedMinutes = 0,
                requestTime = 2000L,
                status = "REJECTED"
            )
        )

        val requests = extensionRequestDao.getRequestsByAppId(appId)

        assertEquals(2, requests.size)
        assertEquals(appId, requests[0].appId)
        assertEquals(appId, requests[1].appId)
    }

    @Test
    fun getRequestsByAppId_returnsNewestRequestFirst() = runTest {
        val appId = createApp()

        val firstId = extensionRequestDao.insertRequest(
            ExtensionRequestEntity(
                appId = appId,
                requestedMinutes = 10,
                grantedMinutes = 10,
                requestTime = 1000L,
                status = "GRANTED"
            )
        ).toInt()

        val secondId = extensionRequestDao.insertRequest(
            ExtensionRequestEntity(
                appId = appId,
                requestedMinutes = 20,
                grantedMinutes = 20,
                requestTime = 3000L,
                status = "GRANTED"
            )
        ).toInt()

        val thirdId = extensionRequestDao.insertRequest(
            ExtensionRequestEntity(
                appId = appId,
                requestedMinutes = 30,
                grantedMinutes = 0,
                requestTime = 2000L,
                status = "REJECTED"
            )
        ).toInt()

        val requests = extensionRequestDao.getRequestsByAppId(appId)

        assertEquals(3, requests.size)
        assertEquals(secondId, requests[0].extensionId)
        assertEquals(thirdId, requests[1].extensionId)
        assertEquals(firstId, requests[2].extensionId)
    }

    @Test
    fun updateRequestStatus_updatesGrantedMinutesAndStatus() = runTest {
        val appId = createApp()

        val requestId = extensionRequestDao.insertRequest(
            ExtensionRequestEntity(
                appId = appId,
                requestedMinutes = 30,
                grantedMinutes = 0,
                requestTime = 1000L,
                status = "PENDING"
            )
        ).toInt()

        extensionRequestDao.updateRequestStatus(
            extensionId = requestId,
            grantedMinutes = 15,
            status = "GRANTED"
        )

        val updatedRequest =
            extensionRequestDao.getRequestById(requestId)

        assertNotNull(updatedRequest)
        assertEquals(15, updatedRequest!!.grantedMinutes)
        assertEquals("GRANTED", updatedRequest!!.status)
    }

    @Test
    fun deleteRequest_removesRequest() = runTest {
        val appId = createApp()

        val requestId = extensionRequestDao.insertRequest(
            ExtensionRequestEntity(
                appId = appId,
                requestedMinutes = 30,
                grantedMinutes = 15,
                requestTime = 1000L,
                status = "GRANTED"
            )
        ).toInt()

        val request =
            extensionRequestDao.getRequestById(requestId)

        assertNotNull(request)

        extensionRequestDao.deleteRequest(request!!)

        val deletedRequest =
            extensionRequestDao.getRequestById(requestId)

        assertNull(deletedRequest)
    }

    @Test
    fun deletingRestrictedApp_deletesItsExtensionRequests() = runTest {
        val appId = createApp()

        extensionRequestDao.insertRequest(
            ExtensionRequestEntity(
                appId = appId,
                requestedMinutes = 30,
                grantedMinutes = 15,
                requestTime = 1000L,
                status = "GRANTED"
            )
        )

        extensionRequestDao.insertRequest(
            ExtensionRequestEntity(
                appId = appId,
                requestedMinutes = 20,
                grantedMinutes = 10,
                requestTime = 2000L,
                status = "GRANTED"
            )
        )

        database.restrictedAppDao().deleteApp(
            database.restrictedAppDao().getAppById(appId)!!
        )

        val requests =
            extensionRequestDao.getRequestsByAppId(appId)

        assertEquals(0, requests.size)
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