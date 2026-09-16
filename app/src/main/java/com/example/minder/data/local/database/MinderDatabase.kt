package com.example.minder.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.minder.data.local.dao.ChallengeAttemptDao
import com.example.minder.data.local.dao.ChallengeDao
import com.example.minder.data.local.dao.DailyUsageDao
import com.example.minder.data.local.dao.ExtensionRequestDao
import com.example.minder.data.local.dao.InterventionDao
import com.example.minder.data.local.dao.RestrictedAppDao
import com.example.minder.data.local.dao.UsageSessionDao
import com.example.minder.data.local.dao.UserDao
import com.example.minder.data.local.dao.UserSettingsDao
import com.example.minder.data.local.entities.ChallengeAttemptEntity
import com.example.minder.data.local.entities.ChallengeEntity
import com.example.minder.data.local.entities.DailyUsageEntity
import com.example.minder.data.local.entities.ExtensionRequestEntity
import com.example.minder.data.local.entities.InterventionEntity
import com.example.minder.data.local.entities.RestrictedAppEntity
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
        DailyUsageEntity::class
    ],
    version = 1,
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
}