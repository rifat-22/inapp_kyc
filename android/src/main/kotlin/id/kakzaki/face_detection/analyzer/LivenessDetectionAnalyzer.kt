package id.kakzaki.face_detection.analyzer
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import id.kakzaki.face_detection.model.LivenessResult
import id.kakzaki.core.GraphicOverlay
import id.kakzaki.core.utils.BitmapUtils
import id.kakzaki.core.utils.BitmapUtils.saveBitmapToFile

import java.util.Random


import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LivenessDetectionAnalyzer(
    private val context: Context,
    private val listDetectionMode: List<DetectionMode>,
    private val detectionArea: Rect,
    private val graphicOverlay: GraphicOverlay,
    private val drawFaceGraphic: Boolean,
    private val listener: LivenessDetectionListener,
) : ImageAnalysis.Analyzer {
    private val classificationDetector: FaceDetector
    private val countourDetector: FaceDetector
    private var faceStatus: FaceStatus = FaceStatus.NOT_FOUND
    private var queueDetectionMode = mutableListOf<DetectionMode>()
    private var isMouthOpen: Boolean = false
    private var startHoldStillTimemilis: Long? = null
    private var startSmileTimemilis: Long? = null
    private val startTimeMilis = System.currentTimeMillis()
    private var startDetectionTime: Long? = null
    private var originalBitmap: Bitmap? = null
    private val detectionResults: MutableList<LivenessResult.DetectionResult> = mutableListOf()

    init {
        /**
         * For better performance, separate between classification and countour options,
         * see [https://developers.google.com/ml-kit/vision/face-detection/android#4.-process-the-image]
        **/
        val classificationOptions = FaceDetectorOptions.Builder()
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setMinFaceSize(0.3f)
            .build()

        val contourOptions = FaceDetectorOptions.Builder()
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setMinFaceSize(0.3f)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()

        classificationDetector = FaceDetection.getClient(classificationOptions)
        countourDetector = FaceDetection.getClient(contourOptions)
        queueDetectionMode = listDetectionMode.toMutableList()
    }


    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val rotationDegrees = image.imageInfo.rotationDegrees
        if (rotationDegrees == 0 || rotationDegrees == 180) {
            graphicOverlay.setImageSourceInfo(image.width, image.height, true)
        }
        else{
            graphicOverlay.setImageSourceInfo(image.height, image.width, true)
        }

        originalBitmap = BitmapUtils.getBitmap(image) ?: return

        val inputImage = InputImage.fromMediaImage(image.image!!,
            rotationDegrees)
        when (currentDetectionMode()) {
            DetectionMode.BLINK,
            DetectionMode.SMILE,
            DetectionMode.SHAKE_HEAD,
            DetectionMode.HOLD_STILL -> {
                classificationDetector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        handleFaces(faces)
                    }
                    .addOnCompleteListener {
                        image.close()
                    }
            }
            DetectionMode.OPEN_MOUTH -> {
                countourDetector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        handleFaces(faces)
                    }
                    .addOnCompleteListener {
                        image.close()
                    }
            }
            else -> {
                image.close()
            }
        }
    }

    private fun handleFaces(faces: List<Face>){
        graphicOverlay.clear()
        if (faces.isEmpty()) {
            setFaceStatus(FaceStatus.NOT_FOUND)
        }
        else
            for (face in faces){
                val faceGraphic = FaceGraphic(graphicOverlay,face)
                if(drawFaceGraphic){
                    graphicOverlay.add(faceGraphic)
                }
                if (checkFaceStatus(faceGraphic) == FaceStatus.READY) {
                    currentDetectionMode()?.let {
                        detectGesture(face, it)
                    }
                }
            }
    }

    private fun detectGesture(face: Face, detectionMode: DetectionMode) {
        when (detectionMode) {
            DetectionMode.BLINK -> detectBlink(face)
            DetectionMode.SHAKE_HEAD -> detectShakeHead(face)
            DetectionMode.OPEN_MOUTH -> detectMouthOpen(face)
            DetectionMode.SMILE -> detectSmile(face)
            DetectionMode.HOLD_STILL -> detectHoldStill(face)
        }
    }

    private fun detectHoldStill(face: Face){
        if (face.headEulerAngleY < 5 && face.headEulerAngleY > -5) {
            if (startHoldStillTimemilis == null)
                startHoldStillTimemilis = System.currentTimeMillis()
            else{
                startHoldStillTimemilis?.let {
                    if((System.currentTimeMillis() - it) > 500){
                        nextDetection()
                    }
                }
            }
        }
        else{
            startHoldStillTimemilis = null
        }
    }

    private fun detectSmile(face: Face){
        Log.d(TAG, "Smile Probability ${face.smilingProbability}")
        if ((face.smilingProbability ?: 0f) > 0.8f){

            nextDetection()
        }
    }

    private fun detectBlink(face: Face) {
        Log.d(TAG, "LeftEyeOpenProbability ${face.leftEyeOpenProbability} RightEyeOpenProbability ${face.rightEyeOpenProbability}")
        if (face.leftEyeOpenProbability != null && face.rightEyeOpenProbability != null) {
            if (face.leftEyeOpenProbability!! < 0.1f && face.rightEyeOpenProbability!! < 0.1f) {
                nextDetection()
            }
        }
    }

    private fun detectMouthOpen(face: Face) {
        val topLip = face.getContour(FaceContour.LOWER_LIP_TOP)
        val bottomLip = face.getContour(FaceContour.UPPER_LIP_BOTTOM)

        if (topLip != null && bottomLip != null) {
            val delta = topLip.points[4].y - bottomLip.points[4].y
            Log.d(TAG, "Delta Lip $delta FaceHeight ${face.boundingBox.height()}")
            if (delta > 25) {
                isMouthOpen = true
            }
            if (isMouthOpen && delta < 10) {
                isMouthOpen = false
                nextDetection()
            }
        }
    }

    private fun detectShakeHead(face: Face) {
        Log.d(TAG, "HeadEulerAngleY ${face.headEulerAngleY}")
        if (face.headEulerAngleY > 30 || face.headEulerAngleY < -30) {
            nextDetection()
        }
    }

    private fun nextDetection() {
        val size123 = queueDetectionMode.size
        println("Size of the list: $size123")
        currentDetectionMode()?.let {
            if (size123 == 1) {
                val random = Random()
                val randomNumber = random.nextInt()
                val fileUri = originalBitmap?.let { bitmap ->
                    saveBitmapToFile(
                        bitmap,
                        context.externalCacheDir!!.path,
                        "img_${it.name+randomNumber}.jpg"
                    )
                }
                detectionResults.clear()
                detectionResults.add(
                    LivenessResult.DetectionResult(
                        it,
                        fileUri,
                        startDetectionTime?.let { time -> System.currentTimeMillis() - time })
                )
            }
            null
        }
        
        queueDetectionMode.removeFirst()
        if (queueDetectionMode.isEmpty()) {
            GlobalScope.launch {
                delay(5000) // Delay for 10 seconds (10,000 milliseconds)
                listener.onLiveDetectionSuccess(
                    LivenessResult(
                        true,
                        "Sucess",
                        System.currentTimeMillis() - startTimeMilis,
                        detectionResults ))
                println("After delay")
            }
//            if (startSmileTimemilis == null)
//                startSmileTimemilis = System.currentTimeMillis() + 5000
//            else{
//                startSmileTimemilis?.let {
//                    if((System.currentTimeMillis() - it) > 500){
//                        println("whaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaat")
//                        
//                    }
//                }
//            }


        } else {
            startDetectionTime = System.currentTimeMillis()
            listener.onStartDetection(queueDetectionMode.first())
        }
    }

    private fun currentDetectionMode() = queueDetectionMode.firstOrNull()


    private fun checkFaceStatus(face: FaceGraphic): FaceStatus {
        val faceBox = face.getBoundingBox()
        val faceStatus: FaceStatus = when {
            detectionArea.contains(faceBox) -> FaceStatus.READY
            detectionArea.height() < faceBox.height() -> FaceStatus.TOO_CLOSE
            else -> FaceStatus.NOT_READY
        }
        setFaceStatus(faceStatus)
        return faceStatus
    }

    private fun setFaceStatus(faceStatus: FaceStatus) {
        if (this.faceStatus == faceStatus) return
        this.faceStatus = faceStatus
        listener.onFaceStatusChanged(faceStatus)
        when (faceStatus) {
            FaceStatus.READY -> {
                if(startDetectionTime == null) startDetectionTime = System.currentTimeMillis()
                listener.onStartDetection(queueDetectionMode.first())
            }
            FaceStatus.NOT_FOUND -> {
                startHoldStillTimemilis = null
                detectionResults.clear()
                refreshQueue()
            }
            else -> {
                startHoldStillTimemilis = null
            }
        }
    }

    private fun refreshQueue() {
        queueDetectionMode = listDetectionMode.toMutableList()
    }

    companion object {
        const val TAG = "LiveDetectionAnalyzer"
    }
}

enum class DetectionMode {
    BLINK, SHAKE_HEAD, OPEN_MOUTH, SMILE, HOLD_STILL
}

enum class FaceStatus {
    NOT_FOUND, NOT_READY, READY, TOO_FAR, TOO_CLOSE
}