package aitam.csm.chandra.herguardians

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var audioFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        audioFile = File(externalCacheDir, "recorded_audio.wav")
        setContent {
            SafetyAlertScreen()
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
        )
        val notGrantedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGrantedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGrantedPermissions.toTypedArray(), 0)
        }
    }

    fun startRecording() {
        audioFile = File(externalCacheDir, "recorded_audio.wav")
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        sendAudioToBackend()
    }

    private fun sendAudioToBackend() {
        val requestFile = RequestBody.create(MediaType.parse("audio/wav"), audioFile)
        val body = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)

        RetrofitClient.instance.uploadAudio(body).enqueue(object : Callback<EmotionResponse> {
            override fun onResponse(call: Call<EmotionResponse>, response: Response<EmotionResponse>) {
                val emotion = response.body()?.emotion ?: "Unknown"
                Toast.makeText(this@MainActivity, "Detected Emotion: $emotion", Toast.LENGTH_LONG).show()

                if (emotion == "Fear" || emotion == "Angry") {
                    sendEmergencyAlert()
                }
            }

            override fun onFailure(call: Call<EmotionResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendEmergencyAlert() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        val locationTask: Task<Location> = fusedLocationClient.lastLocation
        locationTask.addOnSuccessListener { location: Location? ->
            val message = if (location != null) {
                "Emergency! My location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                "Emergency! Unable to fetch location."
            }

            val requestBody = RequestBody.create(MediaType.parse("text/plain"), message)
            RetrofitClient.instance.sendEmergencyMessage(requestBody).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Toast.makeText(this@MainActivity, "Emergency message sent!", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Failed to send emergency message: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}

@Composable
fun SafetyAlertScreen() {
    val context = LocalContext.current
    val mainActivity = context as MainActivity
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            mainActivity.startRecording()
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        val detectedWord=""
        Text("Detected Word: $detectedWord")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
            Text("Start Listening")
        }
    }
}

