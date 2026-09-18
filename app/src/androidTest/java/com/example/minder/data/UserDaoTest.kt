package com.example.minder.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.minder.data.local.dao.UserDao
import com.example.minder.data.local.entities.UserEntity
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
class UserDaoTest {

    private lateinit var database: MinderDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            MinderDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        userDao = database.userDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertUser_andGetUserById_returnsCorrectUser() = runTest {
        val userId = userDao.insertUser(
            UserEntity(
                createdAt = 1000L
            )
        ).toInt()

        val user = userDao.getUserById(userId)

        assertNotNull(user)
        assertEquals(userId, user!!.userId)
        assertEquals(1000L, user.createdAt)
    }

    @Test
    fun getUser_returnsFirstUser() = runTest {
        val firstUserId = userDao.insertUser(
            UserEntity(
                createdAt = 1000L
            )
        ).toInt()

        userDao.insertUser(
            UserEntity(
                createdAt = 2000L
            )
        )

        val user = userDao.getUser()

        assertNotNull(user)
        assertEquals(firstUserId, user!!.userId)
        assertEquals(1000L, user.createdAt)
    }

    @Test
    fun updateUser_updatesUserData() = runTest {
        val userId = userDao.insertUser(
            UserEntity(
                createdAt = 1000L
            )
        ).toInt()

        val user = userDao.getUserById(userId)

        assertNotNull(user)

        val updatedUser = user!!.copy(
            createdAt = 5000L
        )

        userDao.updateUser(updatedUser)

        val result = userDao.getUserById(userId)

        assertNotNull(result)
        assertEquals(userId, result!!.userId)
        assertEquals(5000L, result.createdAt)
    }

    @Test
    fun deleteUser_removesUser() = runTest {
        val userId = userDao.insertUser(
            UserEntity(
                createdAt = 1000L
            )
        ).toInt()

        val user = userDao.getUserById(userId)

        assertNotNull(user)

        userDao.deleteUser(user!!)

        val deletedUser = userDao.getUserById(userId)

        assertNull(deletedUser)
    }
}