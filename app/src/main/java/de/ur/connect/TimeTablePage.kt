import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import de.ur.connect.Backend
import de.ur.connect.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.SortedMap
import kotlin.math.max
import kotlin.math.min

@Composable
fun TimeTablePage(viewModel: MainViewModel) {
    var currentPage by remember { mutableStateOf(0) }
    val pageCount: () -> Int = { -> min(currentPage + 4, 256) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0.0f,
        pageCount
    )

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column() {
            Spacer(modifier = Modifier.height(64.dp))

            LaunchedEffect(Unit) {
                val table = withContext(Dispatchers.IO) {
                    viewModel.backend.getTimeTable()
                }
                viewModel.timeTable = table ?: listOf()
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

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                currentPage = page
                DayPage(viewModel.timeTable, page.toLong())
            }
        }

        // bottom bar / row (automatically aligned bottom center by Box)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                scope.launch {
                    pagerState.scrollToPage(max(currentPage - 1, 0))
                }
            }) {
                Text("<")
            }
            Button(onClick = {
                scope.launch {
                    pagerState.scrollToPage(0)
                }
            }) {
                Text("Go Home")
            }
            Button(onClick = {
                scope.launch {
                    pagerState.scrollToPage(currentPage + 1)
                }
            }) {
                Text(">")
            }
        }
    }
}


@Composable
fun DayPage(timeTable: List<Backend.TimeTableEntry>, dayOffset: Long) {
    val currentDate = getCurrentDate(dayOffset)
    val dayOfWeek = getDayOfWeek(currentDate)
    val table = buildTimeTable(timeTable, currentDate)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(
            text = "$currentDate $dayOfWeek",
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
        for ((time, entries) in table) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = time,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
                Column(

                ) {
                    for (entry in entries) {
                        Text(
                            text = entry,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

fun buildTimeTable(
    timeTable: List<Backend.TimeTableEntry>,
    currentDate: String
): SortedMap<String, List<String>> {
    val map: HashMap<String, List<String>> = HashMap(timeTable.size / 2)
    for (entry in timeTable) {
        if (!isRelatedDate(entry.date, currentDate, entry.dayRecurrence)) {
            continue
        }
        val toAdd = entry.title
        val new = map.getOrPut(entry.time) { listOf() } + toAdd
        map[entry.time] = new
    }

    return map.toSortedMap()
}
