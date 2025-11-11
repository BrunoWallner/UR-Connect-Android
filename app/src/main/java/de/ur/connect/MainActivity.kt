package de.ur.connect

import Storage
import TimeTablePage
import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import de.ur.connect.ui.theme.URConnectTheme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    var loggedIn by mutableStateOf(false)
    var tableUpdated by mutableStateOf(false)

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

        val table = Storage().getTimeTable(this)
        if (table != null) {
            viewModel.tableUpdated = true
            viewModel.timeTable = table
        }

        setContent {
            URConnectTheme {
                Pages(viewModel, credentials)
            }
        }
    }
}

@Composable
fun Pages(viewModel: MainViewModel, credentials: Credentials) {
    val context = LocalContext.current
    if (viewModel.loggedIn && !viewModel.tableUpdated) {
        LaunchedEffect(Unit) {
            val table = withContext(Dispatchers.IO) {
                viewModel.backend.getTimeTable()
            }
            viewModel.timeTable = table ?: listOf()
            Storage().saveTimeTable(context as Activity, viewModel.timeTable)
        }
    }
    if (!viewModel.loggedIn && !viewModel.tableUpdated) {
        LoginPage(onLoginSuccess = { viewModel.loggedIn = true }, viewModel.backend, credentials)
    } else {
        if (viewModel.loggedIn) {
            LaunchedEffect(Unit) {
                val table = withContext(Dispatchers.IO) {
                    viewModel.backend.getTimeTable()
                }
                viewModel.timeTable = table ?: listOf()
                Storage().saveTimeTable(context as Activity, viewModel.timeTable)
            }
        }
        TimeTablePage(viewModel)
    }
}



