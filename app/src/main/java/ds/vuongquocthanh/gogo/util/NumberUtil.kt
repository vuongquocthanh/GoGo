package ds.vuongquocthanh.gogo.util

import java.text.DecimalFormat
import java.text.NumberFormat

object NumberUtil {

    fun formatStringToMoney(s: String) : String{
        val format: NumberFormat = DecimalFormat("#,###")
        var price :String= format.format(s.toDouble())
        price = String.format("%s đ", price)
        return price
    }
}