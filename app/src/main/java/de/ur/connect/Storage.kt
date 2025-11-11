import android.app.Activity
import de.ur.connect.Backend
import android.os.Bundle
import androidx.activity.ComponentActivity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

class Storage() {
    fun saveTimeTable(context: Activity, timeTable: List<Backend.TimeTableEntry>) {
        val json = Json.encodeToString(timeTable)
        val file = File(context.filesDir, "timeTable.json")
        FileOutputStream(file).use {
            it.write(json.toByteArray())
        }
    }

    fun getTimeTable(context: Activity): List<Backend.TimeTableEntry>? {
        val file = File(context.filesDir, "timeTable.json")
        if (!file.exists()) return null

        return try {
            val json = file.readText()
            Json.decodeFromString<List<Backend.TimeTableEntry>>(json)
        } catch (e: Exception) {
            null
        }
    }
}