import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class Semester(val type: SemesterType, val year: Int)

enum class SemesterType { SUMMER, WINTER }

fun getSemester(date: LocalDate): Semester {
    return when (date.monthValue) {
        in 4..9 -> Semester(SemesterType.SUMMER, date.year) // SoSe: April–Sept
        else -> {
            val semesterYear = if (date.monthValue >= 10) date.year else date.year - 1
            Semester(SemesterType.WINTER, semesterYear)       // WiSe: Oct–Mar
        }
    }
}

fun getCurrentDate(dayOffset: Long = 0): String {
    val currentDate = LocalDate.now();
    val date = currentDate.plusDays(dayOffset)
    val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    return dateString
}

fun getDayOfWeek(date: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val parsedDate = LocalDate.parse(date, formatter)
    return parsedDate.dayOfWeek.toString()
}

fun isRelatedDate(
    cmpDateStr: String,
    currentDateStr: String,
    dayIntervalRecurrence: Int,
): Boolean {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val cmpDate = LocalDate.parse(cmpDateStr, formatter)
    val currentDate = LocalDate.parse(currentDateStr, formatter)

    if (currentDate == cmpDate) {return true}
    if (dayIntervalRecurrence == 0) {return false}

    // Only dates in the same semester
    if (getSemester(cmpDate) != getSemester(currentDate)) return false


    if (currentDate <= cmpDate) return false

    if (cmpDate.dayOfWeek != currentDate.dayOfWeek) return false

    val daysBetween = ChronoUnit.DAYS.between(cmpDate, currentDate)

    if (daysBetween % dayIntervalRecurrence != 0L) return false

    return true
}