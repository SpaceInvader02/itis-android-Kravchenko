package com.example.homework1

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun buttonOpensSecondActivityWithCombinedData() {
        val activity = composeRule.activity
        val name = activity.getString(R.string.student_name)
        val group = activity.getString(R.string.student_group)
        composeRule.onNodeWithText(name).assertIsDisplayed()
        composeRule.onNodeWithText(group).assertIsDisplayed()
        composeRule.onNodeWithText(activity.getString(R.string.open_second_screen)).performClick()
        composeRule.onNodeWithText(activity.getString(R.string.student_info_format, name, group))
            .assertIsDisplayed()
    }

    @Test
    fun secondActivityDisplaysActualIntentPayloadAfterRecreation() {
        val payload = "Тестовый Студент/TEST-42"
        ActivityScenario.launch<SecondActivity>(SecondActivity.createIntent(composeRule.activity, payload)).use {
            composeRule.onNodeWithText(payload).assertIsDisplayed()
            it.recreate()
            composeRule.onNodeWithText(payload).assertIsDisplayed()
        }
    }
}
