package ds.vuongquocthanh.gogo.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast


fun Context.toast(messenger: CharSequence, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messenger, length).show()
}

fun ViewGroup.inflate(layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}