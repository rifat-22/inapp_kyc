package id.kakzaki.face_detection.analyzer

import com.google.mlkit.vision.face.Face

interface FaceDetectionListener {
    fun onFaceDetectionSuccess(faces: List<Face>)
    fun onFaceDetectionFailure(exception: Exception)
}