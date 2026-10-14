package com.example.minder.services.monitoring

class UsageLimitMonitor {

    private var challengeIntervalSeconds: Long = 15L * 60L

    // Lama - Stores when the current restricted-app usage period started
    private var activeStartTime: Long? = null

    // Lama - Stores accumulated restricted-app usage before a pause
    private var accumulatedUsageMillis: Long = 0L

    // Lama - Stores the currently active restricted application
    private var currentPackageName: String? = null

    fun setChallengeInterval(minutes: Int) {
        require(minutes > 0) {
            "Challenge interval must be greater than zero"
        }

        challengeIntervalSeconds = minutes.toLong() * 60L
    }

    // Lama - Starts or continues the shared timer for restricted applications
    fun startMonitoring(packageName: String) {

        // Lama - Updates the currently active restricted application
        // without resetting the shared timer
        currentPackageName = packageName

        // Lama - Starts timing only if the shared timer is currently paused
        if (activeStartTime == null) {
            activeStartTime = System.currentTimeMillis()
        }
    }

    // Lama - Pauses the shared timer while preserving accumulated usage
    fun pauseMonitoring() {

        val startTime = activeStartTime

        if (startTime != null) {
            accumulatedUsageMillis +=
                System.currentTimeMillis() - startTime
        }

        activeStartTime = null
        currentPackageName = null
    }

    // Lama - Returns total accumulated restricted-app usage in seconds
    fun getElapsedSeconds(): Long {

        var totalUsageMillis =
            accumulatedUsageMillis

        val startTime =
            activeStartTime

        if (startTime != null) {

            totalUsageMillis +=
                System.currentTimeMillis() - startTime
        }

        return totalUsageMillis / 1000
    }

    // Lama - Checks whether the shared temporary usage limit has been reached
    fun hasReachedLimit(): Boolean {
        return getElapsedSeconds() >= challengeIntervalSeconds
    }

    // Lama - Returns the restricted application currently being used
    fun getCurrentPackage(): String? {

        return currentPackageName
    }

    // Lama - Resets the shared timer after completing an intervention cycle
    fun resetMonitoring() {

        activeStartTime = null
        accumulatedUsageMillis = 0L
        currentPackageName = null
    }

}