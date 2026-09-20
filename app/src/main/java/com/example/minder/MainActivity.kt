package com.example.minder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.minder.features.onboarding.OnboardingScreen
import com.example.minder.features.questionnaire.QuestionnaireScreen
import com.example.minder.ui.theme.MinderTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MinderTheme {

                OnboardingScreen(
                    onGetStarted = {
                        setContent {
                            MinderTheme {
                                QuestionnaireScreen(
                                    onNext = {
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}