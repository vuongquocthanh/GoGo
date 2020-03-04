package ds.vuongquocthanh.gogo.base

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wang.avi.AVLoadingIndicatorView
import dmax.dialog.SpotsDialog
import ds.vuongquocthanh.gogo.R
import ds.vuongquocthanh.gogo.util.TinyDB

class BaseActivity : AppCompatActivity() {
    lateinit var tinyDB: TinyDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tinyDB = TinyDB(this)
    }

    fun progressAviDialog(context: Context): Dialog {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val viewDialog = inflater.inflate(R.layout.layout_progress_loading, null)
        dialogBuilder.setView(viewDialog)
        val dialog = dialogBuilder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val avi = viewDialog.findViewById<AVLoadingIndicatorView>(R.id.avi)
        dialog.setCanceledOnTouchOutside(true)
        avi.show()
        return dialog
    }

    fun progressSpotDialog(messenger: String): AlertDialog {
        return SpotsDialog.Builder().setContext(this).setMessage(messenger).build()
    }


}