package ds.vuongquocthanh.gogo.util

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import java.util.*


fun Context.toast(messenger: CharSequence, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messenger, length).show()
}

fun ViewGroup.inflate(layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun Context.getAddress(lat: Double, long: Double): String {
    val geocoder = Geocoder(this, Locale.getDefault())
    val geoList = geocoder.getFromLocation(lat, long, 1)
    val address = geoList[0].getAddressLine(0)

    return address
}