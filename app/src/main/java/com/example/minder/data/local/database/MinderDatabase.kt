package com.example.minder.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.minder.data.local.dao.ChallengeAttemptDao
import com.example.minder.data.local.dao.ChallengeDao
import com.example.minder.data.local.dao.DailyUsageDao
import com.example.minder.data.local.dao.ExtensionRequestDao
import com.example.minder.data.local.dao.InterventionDao
import com.example.minder.data.local.dao.RestrictedAppDao
import com.example.minder.data.local.dao.UsageBaselineDao
import com.example.minder.data.local.dao.UsageSessionDao
import com.example.minder.data.local.dao.UserDao
import com.example.minder.data.local.dao.UserSettingsDao
import com.example.minder.data.local.entities.ChallengeAttemptEntity
import com.example.minder.data.local.entities.ChallengeEntity
import com.example.minder.data.local.entities.DailyUsageEntity
import com.example.minder.data.local.entities.ExtensionRequestEntity
import com.example.minder.data.local.entities.InterventionEntity
import com.example.minder.data.local.entities.RestrictedAppEntity
import com.example.minder.data.local.entities.UsageBaselineEntity
import com.example.minder.data.local.entities.UsageSessionEntity
import com.example.minder.data.local.entities.UserEntity
import com.example.minder.data.local.entities.UserSettingsEntity

@Database(
    entities = [
        UserEntity::class,
        UserSettingsEntity::class,
        RestrictedAppEntity::class,
        UsageSessionEntity::class,
        ChallengeEntity::class,
        InterventionEntity::class,
        ChallengeAttemptEntity::class,
        ExtensionRequestEntity::class,
        DailyUsageEntity::class,
        UsageBaselineEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class MinderDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun restrictedAppDao(): RestrictedAppDao
    abstract fun usageSessionDao(): UsageSessionDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun interventionDao(): InterventionDao
    abstract fun challengeAttemptDao(): ChallengeAttemptDao
    abstract fun extensionRequestDao(): ExtensionRequestDao
    abstract fun dailyUsageDao(): DailyUsageDao
    abstract fun usageBaselineDao(): UsageBaselineDao

    companion object {
        // Lama - Preserve existing challenges when adding generated game data.
        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE challenge ADD COLUMN gameData TEXT NOT NULL DEFAULT '{}'"
                )
            }
        }

        // Lama - Add historical usage baselines without dropping existing tables.
        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS usage_baseline (
                        baselineId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        appId INTEGER NOT NULL,
                        dayOfWeek INTEGER NOT NULL,
                        averageUsageSeconds INTEGER NOT NULL,
                        sampleCount INTEGER NOT NULL,
                        periodStart INTEGER NOT NULL,
                        periodEnd INTEGER NOT NULL,
                        capturedAt INTEGER NOT NULL,
                        FOREIGN KEY(appId) REFERENCES restricted_app(appId)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_usage_baseline_appId ON usage_baseline(appId)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_usage_baseline_appId_dayOfWeek ON usage_baseline(appId, dayOfWeek)"
                )
            }
        }

        @Volatile
        private var INSTANCE: MinderDatabase? = null

        // Lama - Reuse one database instance and register both migrations.
        fun getDatabase(context: Context): MinderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MinderDatabase::class.java,
                    "minder_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
