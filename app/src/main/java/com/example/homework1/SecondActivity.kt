package com.example.homework1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val studentInfo = intent.getStringExtra(EXTRA_STUDENT_INFO)
            ?: getString(R.string.missing_student_info)
        setContent {
            HomeworkTheme { SecondScreen(studentInfo) }
        }
    }

    companion object {
        const val EXTRA_STUDENT_INFO = "com.example.homework1.STUDENT_INFO"

        fun createIntent(context: Context, studentInfo: String): Intent =
            Intent(context, SecondActivity::class.java)
                .putExtra(EXTRA_STUDENT_INFO, studentInfo)
    }
}

@Composable
private fun SecondScreen(studentInfo: String) {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.second_screen_title), style = MaterialTheme.typography.headlineSmall)
            Text(studentInfo, style = MaterialTheme.typography.titleLarge)
        }
    }
}
