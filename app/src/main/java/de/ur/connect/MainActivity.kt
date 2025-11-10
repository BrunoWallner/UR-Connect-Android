package de.ur.connect

import LoginPage
import TimeTablePage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.ur.connect.ui.theme.URConnectTheme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    var loggedIn by mutableStateOf(false)
    var timeTable: List<Backend.TimeTableEntry> by mutableStateOf(listOf())
    val backend = Backend()
}

class MainActivity : ComponentActivity() {
    val viewModel: MainViewModel by viewModels()
    val context = baseContext
    val credentials = Credentials(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            URConnectTheme {
                Pages(viewModel, credentials)
            }
        }
    }
}

@Composable
fun Pages(viewModel: MainViewModel, credentials: Credentials) {
    if (!viewModel.loggedIn) {
        LoginPage(onLoginSuccess = { viewModel.loggedIn = true }, viewModel.backend, credentials)
    } else {
        TimeTablePage(viewModel)
    }
}



