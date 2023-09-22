package com.example.callrecoding

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var startRecordingButton: Button
    private lateinit var stopRecordingButton: Button
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false
    private lateinit var outputFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startRecordingButton = findViewById(R.id.startRecordingButton)
        stopRecordingButton = findViewById(R.id.stopRecordingButton)

        startRecordingButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }

        stopRecordingButton.setOnClickListener {
            stopRecording()
        }

        // Request necessary permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                0
            )
        }
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        val filePath =
            Environment.getExternalStorageDirectory().absolutePath + "/CallRecordings"
        val directory = File(filePath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val fileName = "recording_${System.currentTimeMillis()}.3gp"
        outputFile = File(directory, fileName)
        mediaRecorder.setOutputFile(outputFile.absolutePath)

        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            isRecording = true
            startRecordingButton.text = "Stop Recording"
            stopRecordingButton.isEnabled = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            mediaRecorder.stop()
            mediaRecorder.release()
            isRecording = false
            startRecordingButton.text = "Start Recording"
            stopRecordingButton.isEnabled = false

            // Add the file to the device's file manager
            if (outputFile.exists()) {
                // Display a notification
                showNotification()

                // Update the UI
                updateUI()
            }
        }
    }

    private fun showNotification() {
        val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
            .setContentTitle("Recording Saved")
            .setContentText("Recording saved successfully.")
            .setSmallIcon(R.drawable.ic_notifications)
            .setAutoCancel(true)
            .build()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder)
    }

    private fun updateUI() {
        runOnUiThread {
            Toast.makeText(this, "Recording saved successfully", Toast.LENGTH_SHORT).show()
        }
    }

}
