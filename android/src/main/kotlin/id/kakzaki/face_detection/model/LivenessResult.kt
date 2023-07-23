package id.kakzaki.face_detection.model

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import id.kakzaki.face_detection.analyzer.DetectionMode
import id.kakzaki.core.common.Result
import id.kakzaki.core.utils.BitmapUtils
import kotlinx.parcelize.Parcelize


@Parcelize
data class LivenessResult(
    val isSuccess: Boolean,
    val errorMessage: String?,
    val totalTimeMilis: Long? = null,
    val detectionResult: List<DetectionResult>? = null,
    var attempt: Int = 0): Parcelable{

    @Parcelize
    data class DetectionResult(
        val detectionMode: DetectionMode,
        val image: Uri?,
        val timeMilis: Long?): Parcelable

    fun getBitmap(context: Context, detectionMode: DetectionMode): Bitmap? {
        return detectionResult?.find {
            it.detectionMode == detectionMode
        }?.let {
            it.image?.let { uri ->
                BitmapUtils.getBitmapFromContentUri(context.contentResolver, uri)
            }
        }
    }
}
