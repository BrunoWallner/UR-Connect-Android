import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch
import java.util.SortedMap
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTablePage(viewModel: MainViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Button(onClick = {
                        scope.launch {
                            viewModel.tableUpdated = false
                        }
                    }) {
                        Text("Refresh time table")
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Black,
            topBar = {
                TopAppBar(
                    title = { Text("Time table") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black // <-- makes top app bar black
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            Column() {
                Spacer(Modifier.height(padding.calculateTopPadding()))
                TimeTablePager(viewModel)
            }
        }
    }
}

@Composable
private fun TimeTablePager(viewModel: MainViewModel) {
    var currentPage by remember { mutableStateOf(0) }
    val pageCount: () -> Int = { -> min(currentPage + 10, 256) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0.0f,
        pageCount
    )

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column() {
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
                pageSpacing = 0.dp,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.fillMaxSize()
            ) { page ->
                currentPage = page

                // Wrap in Box to center the page horizontally
                Box(
                    modifier = Modifier
                        .fillMaxSize().padding(20.dp),  // takes full pager width
                    contentAlignment = Alignment.Center, // centers DayPage inside the page slot

                ) {
                    DayPage(viewModel.timeTable, page.toLong())
                }
            }
        }

        // bottom navigation buttons
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(scrollState)
            .widthIn(max = 500.dp)  // maximum width of page
            .padding(horizontal = 0.dp)
    ) {
        Text(
            text = "$currentDate $dayOfWeek",
            color = Color.White,
            modifier = Modifier.padding(0.dp)
        )

        // Table content
        for ((time, entries) in table) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(Modifier.height(30.dp))
                Text(
                    text = time,
                    color = Color.White,
                    modifier = Modifier.padding(0.dp)
                )
                Row() {
                    Spacer(Modifier.width(20.dp))
                    Column {
                        for (entry in entries) {
                            Text(
                                text = entry,
                                color = Color.White,
                                modifier = Modifier.padding(0.dp)
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(60.dp)) // makes it possible to scroll further
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
