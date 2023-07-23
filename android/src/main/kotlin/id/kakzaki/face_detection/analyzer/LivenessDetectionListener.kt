package id.kakzaki.face_detection.analyzer

import id.kakzaki.face_detection.model.LivenessResult

interface LivenessDetectionListener {
    fun onFaceStatusChanged(faceStatus: FaceStatus)
    fun onStartDetection(detectionMode: DetectionMode)
    fun onLiveDetectionSuccess(livenessResult: LivenessResult)
    fun onLiveDetectionFailure(exception: Exception)
}