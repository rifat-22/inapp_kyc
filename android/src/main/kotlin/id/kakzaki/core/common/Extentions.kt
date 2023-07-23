package id.kakzaki.core.common

import android.view.View

fun Boolean?.toVisibilityOrGone(): Int {
    return if (this == true) {
        View.VISIBLE
    } else View.GONE
}
