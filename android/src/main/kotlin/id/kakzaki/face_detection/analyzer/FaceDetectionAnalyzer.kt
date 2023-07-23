package id.kakzaki.face_detection.analyzer

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceDetectionAnalyzer(options: FaceDetectorOptions = FaceDetectorOptions.Builder().build(), private val listener: FaceDetectionListener): ImageAnalysis.Analyzer {
    private val faceDetector = FaceDetection.getClient(options)
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        image.image?.let {
            faceDetector.process(InputImage.fromMediaImage(it, image.imageInfo.rotationDegrees))
                .addOnSuccessListener { listFace ->
                    listener.onFaceDetectionSuccess(listFace)
                }
                .addOnFailureListener { exception ->
                    listener.onFaceDetectionFailure(exception)
                }
                .addOnCompleteListener {
                    image.close()
                }
        }?: image.close()
    }
}