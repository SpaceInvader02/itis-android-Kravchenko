package com.example.homework1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                FirstScreen { studentInfo ->
                    startActivity(SecondActivity.createIntent(this, studentInfo))
                }
            }
        }
    }
}

@Composable
private fun FirstScreen(onOpenSecondScreen: (String) -> Unit) {
    val name = stringResource(R.string.student_name)
    val group = stringResource(R.string.student_group)
    val studentInfo = stringResource(R.string.student_info_format, name, group)

    Scaffold { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(name, style = MaterialTheme.typography.titleLarge)
            Text(group, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = { onOpenSecondScreen(studentInfo) }) {
                Text(stringResource(R.string.open_second_screen))
            }
        }
    }
}
