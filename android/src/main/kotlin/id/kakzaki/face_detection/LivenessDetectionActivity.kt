package id.kakzaki.face_detection
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import id.kakzaki.face_detection.analyzer.DetectionMode
import id.kakzaki.face_detection.analyzer.FaceStatus
import id.kakzaki.face_detection.analyzer.LivenessDetectionAnalyzer
import id.kakzaki.face_detection.analyzer.LivenessDetectionListener
import id.kakzaki.face_detection.model.LivenessResult
import id.kakzaki.core.GraphicOverlay
import id.kakzaki.core.base.BaseCameraActivity
import id.kakzaki.core.common.EXTRA_RESULT
import id.kakzaki.core.common.alert
import id.kakzaki.liveness_cam.R
import java.util.*
import kotlin.concurrent.fixedRateTimer

class LivenessDetectionActivity : BaseCameraActivity(), LivenessDetectionListener {

    private lateinit var textToSpeech: TextToSpeech
    lateinit var uiContainer: View
    lateinit var tvInstruction: TextView
    lateinit var tvTimer: TextView
    lateinit var tgbTextToSpeech: ToggleButton
    lateinit var ivBack: ImageView
    lateinit var ivFace: ImageView
    lateinit var graphicOverlay: GraphicOverlay

    private var isMute = false

    private var timer: Timer? = null
    private var countdownTime = COUNTDOWN_TIME
    private var currentDetection: DetectionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiContainer = layoutInflater.inflate(
            R.layout.activity_liveness_detection,
            rootView, true)
        tvInstruction = uiContainer.findViewById(R.id.tv_instruction)
        tvTimer = uiContainer.findViewById(R.id.tv_timer)
        ivFace = uiContainer.findViewById(R.id.iv_face)
        graphicOverlay = uiContainer.findViewById(R.id.graphic_overlay)
        ivBack = uiContainer.findViewById(R.id.iv_back)
        ivBack.setOnClickListener {
            finish()
        }
        tgbTextToSpeech = uiContainer.findViewById(R.id.tg_mute)
        tgbTextToSpeech.setOnCheckedChangeListener { _, isChecked ->
            isMute = isChecked
            if (isMute)
                textToSpeech.stop()
        }
        initTextToSpeech()
    }


    private fun startCountdownTimer(){
        timer = fixedRateTimer(initialDelay = 0, period = 1000){
            runOnUiThread {
                val time = countdownTime.toString()
                tvTimer.text = time
                countdownTime--
                if (countdownTime==0){
                    stopTimer()
                    alert("TimeOut", isCancelable = false){
                        val livenessResult = LivenessResult(false, "User Timeout")
                        setResult(RESULT_OK, Intent().putExtra(EXTRA_RESULT,livenessResult))
                        finish()
                    }
                }
            }
        }
    }


    private fun restartTime() {
         countdownTime = COUNTDOWN_TIME
    }

    private fun stopTimer(){
        timer?.cancel()
        timer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    override fun startCamera(cameraProvider: ProcessCameraProvider, previewView: PreviewView) {
        val offset = 50
        val analysisUseCase = ImageAnalysis.Builder().build().also {
            it.setAnalyzer(cameraExecutor,
                LivenessDetectionAnalyzer(
                    this,
                    Identifier.detectionMode,
                    Rect(ivFace.left - offset*2, ivFace.top - offset, ivFace.right + offset*2, ivFace.bottom + offset),
                    graphicOverlay,
                    false,
                    this))
        }

        val previewUseCase = Preview.Builder().build()

        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
        cameraProvider.unbindAll()
        try {
           cameraProvider.bindToLifecycle(
                this, cameraSelector, previewUseCase, analysisUseCase)
            // Attach the viewfinder's surface provider to preview use case
            previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
            startCountdownTimer()
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun initTextToSpeech(){
        textToSpeech = TextToSpeech(this){ status ->
            if(status != TextToSpeech.ERROR) {
                val locale = Locale.getDefault()
                textToSpeech.voices.firstOrNull {
                    it.locale == locale
                }?.also {
                    textToSpeech.voice = it
                }
            }
        }
    }

    private fun speak(text: String){
        if (!isMute)
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "${text.hashCode()}  ${System.currentTimeMillis()}" )
    }

    override fun onPause() {
        super.onPause()
        textToSpeech.stop()
    }

    override fun onFaceStatusChanged(faceStatus: FaceStatus) {
        textToSpeech.stop()
        tvInstruction.text = when(faceStatus){
            FaceStatus.NOT_FOUND -> getString(R.string.lbl_put_face_to_the_frame)
            FaceStatus.TOO_FAR -> getString(R.string.lbl_too_far)
            FaceStatus.TOO_CLOSE -> getString(R.string.lbl_too_close)
            else -> getString(R.string.lbl_put_face_to_the_frame)
        }
    }

    override fun onStartDetection(detectionMode: DetectionMode) {
        if(currentDetection != detectionMode) {
            currentDetection = detectionMode
            restartTime()
        }
        when(detectionMode){
            DetectionMode.HOLD_STILL ->{
                tvInstruction.text = getString(R.string.lbl_hold_still_instruction)
                speak(getString(R.string.lbl_hold_still_instruction))
            }

            DetectionMode.BLINK -> {
                tvInstruction.text = getString(R.string.liveness_please_blink)
                speak(getString(R.string.liveness_please_blink))
            }
            DetectionMode.OPEN_MOUTH -> {
                tvInstruction.text = getString(R.string.liveness_please_open_mouth)
                speak(getString(R.string.liveness_please_open_mouth))
            }
            DetectionMode.SHAKE_HEAD -> {
                tvInstruction.text = getString(R.string.liveness_please_shake_head)
                speak(getString(R.string.liveness_please_shake_head))
            }
            DetectionMode.SMILE -> {
                tvInstruction.text = getString(R.string.liveness_please_smile)
                speak(getString(R.string.liveness_please_smile))
            }
        }
    }

    override fun onLiveDetectionSuccess(livenessResult: LivenessResult) {
        val intent = Intent().putExtra(EXTRA_RESULT,livenessResult)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onLiveDetectionFailure(exception: Exception) {
        val result = LivenessResult(false, exception.message)
        setResult(RESULT_CANCELED, Intent().putExtra(EXTRA_RESULT, result))
        finish()
    }

    companion object {
        private const val TAG = "Liveness Detection"
        private const val COUNTDOWN_TIME = 60
    }
}