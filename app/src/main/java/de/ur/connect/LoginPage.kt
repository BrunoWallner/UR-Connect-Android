package de.ur.connect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import de.ur.connect.Backend
import de.ur.connect.Credentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginPage(
    onLoginSuccess: () -> Unit,
    backend: Backend,
    credentials: Credentials
) {
    LoginForm(onLoginSuccess, backend, credentials)
}

@Composable
fun LoginForm(
    onLoginSuccess: () -> Unit,
    backend: Backend,
    credentials: Credentials,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var loginStatus: String by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    var correctLogin: Credentials.Login? by remember {mutableStateOf(null)}
    var loggedInViaCredentials by remember {mutableStateOf(false)}

    if (correctLogin != null) {
        LaunchedEffect(Unit) {
            if (!loggedInViaCredentials) {
                credentials.save(correctLogin!!)
            }
            onLoginSuccess()
        }
    }

    val handleLogin: (String, String) -> Unit = { name, password ->
        loginStatus = ""
        isLoading = true
        coroutineScope.launch {
            val res = withContext(Dispatchers.IO) {
                backend.login(name, password)
            }
            isLoading = false
            when (res) {
                Backend.LoginResult.SUCCESS -> {
                    correctLogin = Credentials.Login(name, passwd = password)

                }
                Backend.LoginResult.ERROR -> loginStatus = "Unexpected error, please try again later"
                Backend.LoginResult.INVALID_CREDENTIALS -> loginStatus = "Invalid credentials"
            }
        }
    }

    // toggle credential manager popup
    LaunchedEffect(Unit) {
        val login = withContext(Dispatchers.IO) {
            credentials.get()
        }
        if (login != null) {
            loggedInViaCredentials = true
            handleLogin(login.name, login.passwd)
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(16.dp),
    ) {
        Spacer(Modifier.height(128.dp))

        Text(text = "Please login with your RZ-Account", color = Color.White)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { handleLogin(name, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
            Spacer(Modifier.height(16.dp))
            if (isLoading) {
                CircularProgressIndicator()
            }
        }

        Text(
            text = loginStatus,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
    }
}