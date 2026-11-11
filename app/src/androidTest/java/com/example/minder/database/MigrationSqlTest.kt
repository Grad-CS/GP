
package com.example.minder.data.local.database

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationSqlTest {

    @Test
    fun migration_preservesExistingChallengeAndAddsDefaultGameData() {
        //Ragahd-: Creates an isolated in-memory SQLite database.
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
            .name(null)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    //Ragahd-: Creates the original Version 1 challenge table.
                    db.execSQL(
                        """
                        CREATE TABLE challenge (
                            challengeId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            question TEXT NOT NULL,
                            correctAnswer TEXT NOT NULL,
                            difficulty TEXT NOT NULL,
                            challengeType TEXT NOT NULL
                        )
                        """.trimIndent()
                    )

                    //Ragahd-: Inserts an existing record before migration.
                    db.execSQL(
                        """
                        INSERT INTO challenge
                            (challengeId, question, correctAnswer, difficulty, challengeType)
                        VALUES (1, '2 + 2', '4', 'EASY', 'MATH')
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    //Ragahd-: Leaves migration execution to the test.
                }
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)

        try {
            val db = helper.writableDatabase

            //Ragahd-: Applies the same migration used by the app.
            MinderDatabase.MIGRATION_1_2.migrate(db)

            //Ragahd-: Reads the existing challenge after migration.
            db.query(
                """
                SELECT challengeId, question, correctAnswer, difficulty,
                       challengeType, gameData
                FROM challenge
                WHERE challengeId = 1
                """
            ).use { cursor ->
                assertTrue("Existing challenge should remain", cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
                assertEquals("2 + 2", cursor.getString(1))
                assertEquals("4", cursor.getString(2))
                assertEquals("EASY", cursor.getString(3))
                assertEquals("MATH", cursor.getString(4))
                assertEquals("{}", cursor.getString(5))
            }
        } finally {
            //Ragahd-: Closes the temporary database helper.
            helper.close()
        }
    }

    //Ragahd-: Verifies Version 2 to Version 3 migration.
    @Test
    fun migration_2_3_createsBaselineTableAndPreservesExistingData() {

        val configuration = SupportSQLiteOpenHelper.Configuration.builder(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
            .name(null)
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {

                    //Ragahd-: Creates the existing restricted app table.
                    db.execSQL(
                        """
                    CREATE TABLE restricted_app (
                        appId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL
                    )
                    """.trimIndent()
                    )

                    //Ragahd-: Inserts an existing app before migration.
                    db.execSQL(
                        """
                    INSERT INTO restricted_app (appId, userId)
                    VALUES (1, 1)
                    """.trimIndent()
                    )
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    //Ragahd-: Leaves migration execution to the test.
                }
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)

        try {
            val db = helper.writableDatabase

            //Ragahd-: Applies the Version 2 to Version 3 migration.
            MinderDatabase.MIGRATION_2_3.migrate(db)

            //Ragahd-: Verifies that the baseline table exists.
            db.query(
                """
            SELECT name FROM sqlite_master
            WHERE type = 'table'
            AND name = 'usage_baseline'
            """
            ).use { cursor ->
                assertTrue("Baseline table should exist", cursor.moveToFirst())
            }

            //Ragahd-: Verifies all baseline table columns.
            val expectedColumns = setOf(
                "baselineId",
                "appId",
                "dayOfWeek",
                "averageUsageSeconds",
                "sampleCount",
                "periodStart",
                "periodEnd",
                "capturedAt"
            )

            val actualColumns = mutableSetOf<String>()

            db.query("PRAGMA table_info(usage_baseline)").use { cursor ->
                while (cursor.moveToNext()) {
                    actualColumns.add(cursor.getString(1))
                }
            }

            assertEquals(expectedColumns, actualColumns)

            //Ragahd-: Verifies the baseline indexes.
            val indexes = mutableMapOf<String, Boolean>()

            db.query("PRAGMA index_list(usage_baseline)").use { cursor ->
                while (cursor.moveToNext()) {
                    indexes[cursor.getString(1)] = cursor.getInt(2) == 1
                }
            }

            assertTrue(
                indexes.containsKey("index_usage_baseline_appId")
            )

            assertTrue(
                indexes["index_usage_baseline_appId_dayOfWeek"] == true
            )

            //Ragahd-: Verifies the foreign key configuration.
            db.query("PRAGMA foreign_key_list(usage_baseline)").use { cursor ->
                var foreignKeyFound = false

                while (cursor.moveToNext()) {
                    if (
                        cursor.getString(2) == "restricted_app" &&
                        cursor.getString(3) == "appId" &&
                        cursor.getString(4) == "appId" &&
                        cursor.getString(6) == "CASCADE"
                    ) {
                        foreignKeyFound = true
                    }
                }

                assertTrue(
                    "Baseline foreign key should reference restricted_app with CASCADE",
                    foreignKeyFound
                )
            }

            //Ragahd-: Verifies that existing restricted app data remains unchanged.
            db.query(
                "SELECT appId, userId FROM restricted_app WHERE appId = 1"
            ).use { cursor ->
                assertTrue("Existing app should remain", cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
                assertEquals(1, cursor.getInt(1))
            }

        } finally {
            //Ragahd-: Closes the temporary database helper.
            helper.close()
        }
    }
}
