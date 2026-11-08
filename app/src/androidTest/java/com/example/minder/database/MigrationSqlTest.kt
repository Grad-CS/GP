
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
}
