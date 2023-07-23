package id.kakzaki.core.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


abstract class Result {
    abstract val isSuccess: Boolean
    abstract val errorMessage: String?
}

@Parcelize
data class ResultImpl(
    override val isSuccess: Boolean,
    override val errorMessage: String?
) : Result(), Parcelable