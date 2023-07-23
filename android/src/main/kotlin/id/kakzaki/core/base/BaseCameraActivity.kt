package id.kakzaki.core.base

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import id.kakzaki.core.common.EXTRA_ERROR_MESSAGE
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import id.kakzaki.liveness_cam.R

abstract class BaseCameraActivity : AppCompatActivity() {
    lateinit var cameraExecutor: ExecutorService
    lateinit var rootView: ConstraintLayout
    lateinit var previewView: PreviewView
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_camera)
        rootView = findViewById(R.id.rootView)
        previewView = findViewById(R.id.view_finder)
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )

        } else {
            init()
        }
    }

    private fun init() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        previewView.post {
            // Set up the camera and its use cases
            setUpCamera()
        }
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Build and bind the camera use cases
            cameraProvider?.let {
                startCamera(it, previewView)
            }
        }, ContextCompat.getMainExecutor(this))
    }


    /** Declare and bind preview, capture and analysis use cases */
    abstract fun startCamera(cameraProvider: ProcessCameraProvider, previewView: PreviewView)

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults:
        IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                init()
            } else {
                val errMsg = "Permissions to access camera not allowed by the user."
                Toast.makeText(
                    this,
                    errMsg,
                    Toast.LENGTH_SHORT
                ).show()

                setResult(
                    RESULT_CANCELED,
                    Intent().putExtra(EXTRA_ERROR_MESSAGE, errMsg)
                )
                finish()
            }
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}