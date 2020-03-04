package ds.vuongquocthanh.gogo.base

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.Fragment
import com.wang.avi.AVLoadingIndicatorView
import dmax.dialog.SpotsDialog
import ds.vuongquocthanh.gogo.R
import ds.vuongquocthanh.gogo.util.TinyDB

class BaseFragment : Fragment() {
    lateinit var tinyDB: TinyDB
    private var mContext: Context? = null
    private lateinit var mActivity: Activity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        tinyDB = TinyDB(context)
        if (context is Activity) mActivity = context
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        mContext=null
    }

    fun progressDialog(context: Context?, message: String?): AlertDialog {
        return SpotsDialog.Builder().setContext(context)
            .setMessage(message)
            .build()
    }

    fun progressAviDialog(context : Context) : Dialog{
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val viewDialog = inflater.inflate(R.layout.layout_progress_loading,null)
        dialogBuilder.setView(viewDialog)
        val dialog = dialogBuilder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val avi = viewDialog.findViewById<AVLoadingIndicatorView>(R.id.avi)
        avi.show()
        return dialog
    }

}