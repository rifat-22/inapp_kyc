package id.kakzaki.core.common

import android.content.Context
import androidx.appcompat.app.AlertDialog

fun Context.alert(message: String, title: String? = "Info", actionTitle: String = "Ok", negativeTitle: String? = null,
                  dismissListener: () -> Unit = {}, isCancelable: Boolean = true, actionListener: () -> Unit = {}): AlertDialog =
    AlertDialog.Builder(this).apply {
        setCancelable(isCancelable)
        setTitle(title)
        setMessage(message)
        negativeTitle?.let {
            setNegativeButton(negativeTitle) { dialog , _ ->
                dialog.dismiss()
                dismissListener()
            }
        }

        setPositiveButton(actionTitle){ dialog, _ ->
            dialog.dismiss()
            actionListener()
        }
    }.show()

