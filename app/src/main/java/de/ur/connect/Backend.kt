package de.ur.connect

class Backend {
    companion object {
        init {
            System.loadLibrary("ur_connect_jni") // name without 'lib' prefix and '.so'
        }
    }

    data class TimeTableEntry(
        val date: String,
        val time: String,
        val title: String,
        val location: String,
        val dayRecurrence: Int,
    )

    enum class LoginResult { SUCCESS, INVALID_CREDENTIALS, ERROR }

    external fun login(name: String, password: String): LoginResult

    private external fun getTimeTableSerialized(): String?
    fun getTimeTable(): List<TimeTableEntry>? {
        val serialized = this.getTimeTableSerialized() ?: return null

        val entries = mutableListOf<TimeTableEntry>()
        for (serializedEntry in serialized.split("\n")) {
            val elements = serializedEntry.split("§").map { it.trim() }.filter { !it.isEmpty() };
            if (elements.size != 5) {
                continue
            }
            val recurrence = elements[4].toInt()
            val entry = TimeTableEntry(
                date = elements[0],
                time = elements[1],
                title = elements[2],
                location = elements[3],
                dayRecurrence = recurrence
            )
            entries.add(entry);
        }

        return entries
    }
}
