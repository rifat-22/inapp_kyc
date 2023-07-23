package id.kakzaki.core.common

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import id.kakzaki.liveness_cam.R


fun Activity.showCustomToast(
    message: String
) {
    val inflater: LayoutInflater = layoutInflater
    val layout: View = inflater.inflate(
        R.layout.toast_custom,
        findViewById(R.id.toast_layout_root)
    )

    val textView: TextView = layout.findViewById<View>(R.id.tv_message) as TextView

    textView.text = message

    val toast = Toast(this)
    toast.setGravity(Gravity.BOTTOM, 0, 120)
    toast.duration = Toast.LENGTH_SHORT
    toast.view = layout
    toast.show()
}