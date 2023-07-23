package id.kakzaki.face_detection

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import id.kakzaki.core.common.EXTRA_RESULT
import id.kakzaki.face_detection.model.LivenessResult

class InitActivity : AppCompatActivity() {
    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if (isGranted)
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, LivenessDetectionActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                })
                finish()
            },2000)
        else {
            val result = LivenessResult(false,"Camera Permission Denied")
            setResult(RESULT_OK,Intent().putExtra(EXTRA_RESULT,result))
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPermission.launch(Manifest.permission.CAMERA)
    }
}