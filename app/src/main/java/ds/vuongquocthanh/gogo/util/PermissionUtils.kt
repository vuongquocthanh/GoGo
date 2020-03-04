package ds.vuongquocthanh.gogo.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object PermissionUtils {

    fun hasPermission(context: Context, permission: String): Boolean{
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(context: Context, permissions: Array<String>, requestCode: Int){
        ActivityCompat.requestPermissions(context as Activity, permissions, requestCode)
    }
}