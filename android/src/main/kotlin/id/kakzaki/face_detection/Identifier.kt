package id.kakzaki.face_detection

import android.content.Context
import android.content.Intent
import id.kakzaki.face_detection.analyzer.DetectionMode
import id.kakzaki.face_detection.model.LivenessResult
import id.kakzaki.core.common.EXTRA_RESULT
import id.kakzaki.face_detection.InitActivity

object Identifier {
    private var attempt = 0
    var detectionMode= listOf(
        DetectionMode.HOLD_STILL,
        DetectionMode.OPEN_MOUTH,
        DetectionMode.BLINK,
//        DetectionMode.SHAKE_HEAD,
        DetectionMode.SMILE
    )

    @JvmStatic
    fun setDetectionModeSequence(shuffle: Boolean, detectionMode: List<DetectionMode>){
        Identifier.detectionMode = detectionMode.run {
            if (shuffle) shuffled() else this
        }
    }

    @JvmStatic
    fun getLivenessIntent(context: Context): Intent {
        attempt++
        return Intent(context, InitActivity::class.java)
    }

    @JvmStatic
    fun getLivenessResult(intent: Intent?) =
        intent?.getParcelableExtra<LivenessResult>(EXTRA_RESULT)?.apply {
            attempt = Identifier.attempt
            if(isSuccess) Identifier.attempt = 0
        }

}