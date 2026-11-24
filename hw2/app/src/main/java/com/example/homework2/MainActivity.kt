package com.example.homework2

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import java.security.SecureRandom

private object AppPassword {
    val value: String = run {
        val random = SecureRandom()
        val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val digits = "0123456789"
        val alphabet = letters + digits
        val characters = mutableListOf(letters[random.nextInt(letters.length)], digits[random.nextInt(digits.length)])
        repeat(8) { characters.add(alphabet[random.nextInt(alphabet.length)]) }
        java.util.Collections.shuffle(characters, random)
        characters.joinToString("")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val generatedPassword = AppPassword.value
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuthorizationApp(generatedPassword)
                }
            }
        }
    }
}

@Composable
private fun AuthorizationApp(generatedPassword: String) {
    val navController = rememberNavController()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submittedEmail by remember { mutableStateOf("") }
    var submittedPassword by remember { mutableStateOf("") }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(email, password, { email = it }, { password = it }) { validEmail, validPassword ->
                submittedEmail = validEmail
                submittedPassword = validPassword
                navController.navigate("result")
            }
        }
        composable("result") {
            ResultScreen(submittedEmail, submittedPassword, generatedPassword) {
                navController.popBackStack()
            }
        }
    }
}

@Composable
private fun LoginScreen(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current
    val submit = {
        emailError = when {
            email.isBlank() -> "Введите e-mail"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Некорректный e-mail"
            else -> null
        }
        passwordError = when {
            password.isBlank() -> "Введите пароль"
            password.length < 8 -> "Пароль должен содержать минимум 8 символов"
            !password.any { it in 'a'..'z' || it in 'A'..'Z' } || !password.any { it in '0'..'9' } ->
                "Добавьте латинские буквы и цифры"
            else -> null
        }
        if (emailError == null && passwordError == null) {
            keyboard?.hide()
            onSubmit(email, password)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { onEmailChange(it); emailError = null },
            label = { Text("E-mail") },
            singleLine = true,
            isError = emailError != null,
            supportingText = { emailError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { onPasswordChange(it); passwordError = null },
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation('*'),
            isError = passwordError != null,
            supportingText = { passwordError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { submit() }),
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { submit() }, modifier = Modifier.fillMaxWidth()) { Text("Войти") }
    }
}

@Composable
private fun ResultScreen(email: String, password: String, generatedPassword: String, onBack: () -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val matches = password == generatedPassword
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
        Text(email, modifier = Modifier.align(Alignment.TopCenter))
        AsyncImage(
            model = if (matches) "https://http.cat/200.jpg" else "https://http.cat/401.jpg",
            contentDescription = if (matches) "Пароль совпал" else "Пароль не совпал",
            modifier = Modifier.align(Alignment.Center).size(240.dp)
        )
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = generatedPassword,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Сгенерированный пароль") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation('*'),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Скрыть" else "Показать")
                }
            }
            OutlinedButton(
                onClick = { clipboard.setText(AnnotatedString(generatedPassword)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Скопировать пароль") }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
        }
    }
}
