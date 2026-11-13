
package com.example.minder.features.blocking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.minder.services.monitoring.AppAccessibilityService
import com.example.minder.ui.theme.MinderTheme

class ChallengeLockedActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            MinderTheme {
                ChallengeLockedScreen(
                    onExitApp = {
                        // Lama - Return to Home without unlocking apps.
                        AppAccessibilityService.requestExitToHome()
                        finish()
                    }
                )
            }
        }
    }

    // Lama - Release the screen visibility flag when leaving the lock screen.
    override fun onStop() {
        super.onStop()
        AppAccessibilityService.notifyChallengeLockScreenClosed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
