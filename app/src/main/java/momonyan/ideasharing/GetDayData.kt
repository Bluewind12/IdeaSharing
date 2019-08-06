package momonyan.ideasharing

import java.text.SimpleDateFormat
import java.util.*

fun getToday(): String {
    val date = Date()
    val sdf = SimpleDateFormat("yyyy/MM/dd-HH:mm:ss", Locale.getDefault())
    return sdf.format(date)
}