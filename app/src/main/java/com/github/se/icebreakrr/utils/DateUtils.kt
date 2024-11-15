import java.util.Calendar
import java.util.Date

object DateUtils {
  fun isAgeValid(birthDate: Date): Boolean {
    val today = Calendar.getInstance()
    val birthCalendar = Calendar.getInstance().apply { time = birthDate }

    var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
      age--
    }
    return age >= 13
  }

  fun formatDate(date: Date): String {
    val calendar = Calendar.getInstance().apply { time = date }
    return String.format(
        "%02d/%02d/%d",
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.YEAR))
  }
}
