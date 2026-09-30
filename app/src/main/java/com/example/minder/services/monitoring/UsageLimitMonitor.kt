package com.example.minder.services.monitoring

class UsageLimitMonitor {

    // Lama - Stores when the current restricted-app usage period started
    private var activeStartTime: Long? = null

    // Lama - Stores accumulated restricted-app usage before a pause
    private var accumulatedUsageMillis: Long = 0L

    // Lama - Stores the currently active restricted application
    private var currentPackageName: String? = null

    // Lama - Starts or continues the shared timer for restricted applications
    fun startMonitoring(packageName: String) {

        currentPackageName = packageName

        // Lama - Starts timing only if the shared timer is currently paused
        if (activeStartTime == null) {
            activeStartTime = System.currentTimeMillis()
        }
    }

    // Lama - Updates the current restricted application without resetting the timer
    fun switchTargetApp(packageName: String) {
        currentPackageName = packageName

        if (activeStartTime == null) {
            activeStartTime = System.currentTimeMillis()
        }
    }

    // Lama - Pauses the shared timer when the user leaves restricted applications
    fun pauseMonitoring() {

        val startTime = activeStartTime ?: return

        accumulatedUsageMillis +=
            System.currentTimeMillis() - startTime

        activeStartTime = null
        currentPackageName = null
    }

    // Lama - Returns total accumulated restricted-app usage in seconds
    fun getElapsedSeconds(): Long {

        var totalUsageMillis = accumulatedUsageMillis

        val startTime = activeStartTime

        if (startTime != null) {
            totalUsageMillis +=
                System.currentTimeMillis() - startTime
        }

        return totalUsageMillis / 1000
    }

    // Lama - Checks whether the shared temporary usage limit has been reached
    fun hasReachedLimit(): Boolean {
        return getElapsedSeconds() >= TEST_LIMIT_SECONDS
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

    companion object {

        // Lama - Temporary shared 30-second limit used for testing
        private const val TEST_LIMIT_SECONDS = 30L
    }
}