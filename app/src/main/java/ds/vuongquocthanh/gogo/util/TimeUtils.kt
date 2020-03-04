package ds.vuongquocthanh.gogo.util

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun getStringDateYMD(calendar: Calendar): String {
        val format = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        date.time = calendar.timeInMillis
        return format.format(date)
    }

    fun getStringDateDMY(calendar: Calendar): String {
        val format = SimpleDateFormat("dd-MM-yyyy")
        val date = Date()
        date.time = calendar.timeInMillis
        return format.format(date)
    }

    fun getStringDateYMDHMS(calendar: Calendar): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = Date()
        date.time = calendar.timeInMillis
        return format.format(date)
    }

    fun getStringDateDMHM(calendar: Calendar): String {
        val format = SimpleDateFormat("dd/MM HH:mm")
        val date = Date()
        date.time = calendar.timeInMillis
        return format.format(date)
    }

    fun formatTime(time: Long): String {
        val date = Date()
        date.time = time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return dateFormat.format(date)
    }

    fun formatTimeDetail(time: Long): String {
        val date = Date()
        date.time = time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return dateFormat.format(date)
    }

    fun longTimeFromString(time: String): Long{
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = format.parse(time)
        return date.time
    }

    fun longTimeFromHourString(time: String): Long{
        val t = time.split(":")
        return t[0].toLong()*60*60*1000 + t[1].toInt()*60*1000 + t[2].toInt()
    }
}