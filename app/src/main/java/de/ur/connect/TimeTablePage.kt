import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.ur.connect.Backend
import de.ur.connect.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TimeTablePage(viewModel: MainViewModel) {
    Column() {
        Spacer(modifier = Modifier.height(64.dp))
        // var timeTable by remember { mutableStateOf(listOf<Backend.TimeTableEntry>()) }

        LaunchedEffect(Unit) {
            val table = withContext(Dispatchers.IO) {
                viewModel.backend.getTimeTable()
            }
            viewModel.timeTable = table ?: listOf()
            viewModel.timeTable = viewModel.timeTable.sortedBy { it.time }
        }
        if (viewModel.timeTable.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
            return
        }
        var currentPage by remember { mutableStateOf(0) }

        val pageCount: () -> Int = { -> Math.min(currentPage+4, 256)}
        val pagerState = rememberPagerState(
            initialPage = 0,
            initialPageOffsetFraction = 0.0f,
            pageCount
        )
        val scope = rememberCoroutineScope()

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            currentPage = page
            DayPage(viewModel.timeTable, page.toLong())
        }
    }
}

@Composable
fun DayPage(timeTable: List<Backend.TimeTableEntry>, dayOffset: Long) {
    var text = "";
    val currentDate = getCurrentDate(dayOffset)
    val dayOfWeek = getDayOfWeek(currentDate)
    for (entry in timeTable) {
        if (isRelatedDate(entry.date, currentDate, entry.dayRecurrence)) {
            text += "${entry.time.replace(" ", "")} | ${entry.title}\n\n"
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(
            text = "$currentDate $dayOfWeek",
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
    }
}
