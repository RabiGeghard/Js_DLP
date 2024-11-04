package com.tiyamikeqiu.js_dlp


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import android.util.Base64
import java.io.File
import java.io.FileOutputStream


class ContinuousCaptureActivity : AppCompatActivity()  {
    private val CAMERA_PERMISSION_CODE = 100
    private val STORAGE_PERMISSION_CODE = 101
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var beepManager: BeepManager


    private  var lastText: String? = null
    private var top_text_view: TextView? = null
    private var totalFrames = -1
    private lateinit var fileName: String

    private var isBeep: Boolean? = false


    private var frameMap: HashMap<Int, String>? = null

    private var wakeLock: PowerManager.WakeLock? = null





    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@ContinuousCaptureActivity, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this@ContinuousCaptureActivity, arrayOf(permission), requestCode)
        } else {
//            Toast.makeText(this@ContinuousCaptureActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            Environment.isExternalStorageManager()
        } else {
            //Below android 11

            false
        }
    }

    private val storageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or above
            if (Environment.isExternalStorageManager()) {

//                Toast.makeText(this, "onActivityResult: Manage External Storage Permissions Granted", Toast.LENGTH_SHORT).show()
                // Manage External Storage Permissions Granted

            } else {
                Toast.makeText(this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Below android 11

        }
    }

    private fun requestForStoragePermissions() {
        // Android is 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent().apply {
                    action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                    data = Uri.fromParts("package", packageName, null)
                }
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent().apply {
                    action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                }
                storageActivityResultLauncher.launch(intent)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this@ContinuousCaptureActivity, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ContinuousCaptureActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this@ContinuousCaptureActivity, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ContinuousCaptureActivity, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDebugLine(frameNumber: Int): String {
        var min = 0
        val num = frameMap?.size
        val current = frameNumber
        val total = totalFrames

        for (i in 0 until totalFrames) {
            min = i
            if (!frameMap?.containsKey(i)!!) {
                break
            }
        }

        return "Minimal:$min, Current:$current, Scored:$num, Total:$total "
    }

    private fun updateStatus(message: String) {
        runOnUiThread {
            top_text_view!!.text = message
        }
    }


    private fun reconstructFile() {

        try {


        val combinedBase64 = StringBuilder().apply {
            for (i in 0 until totalFrames) {
                append(frameMap!![i])
            }
        }.toString()

        val fileBytes = try {
            Base64.decode(combinedBase64, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            throw Exception("Invalid base64 data")
        }

        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val directory = File(downloadDir, "Js_DLP")
        if (!directory.exists() && !directory.mkdirs()) {
            throw Exception("Failed to create directory")
        }


        if (fileName.isBlank() || fileName.contains("/")) {
            throw Exception("Invalid filename")
        }

        val file = File(directory, fileName)

        FileOutputStream(file).use { output ->
            output.write(fileBytes)
            output.flush()
        }

        frameMap!!.clear()

        } catch (e: Exception) {

            updateStatus("Error: ${e.message}")
        }

    }




    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }

            lastText = result.text


            val tmp_str = lastText

            val parts = tmp_str?.split(":")
            if (parts != null) {
                if (parts.size < 3) {
                    return
                }
                val frameNumber = parts[0].toInt()

                val tmp_total =parts[1].toInt()

                if ( totalFrames != tmp_total){
                    frameMap!!.clear()
                }

                totalFrames = parts[1].toInt()

                val debugString = getDebugLine(frameNumber)
                top_text_view?.text   = debugString

                if (frameMap?.containsKey(frameNumber) == true) {
                    return
                }


                if ( frameNumber == 0) {
                    if (parts.size < 4) {
                        return
                    }
                    fileName = parts[2]
                    frameMap?.set(frameNumber, parts[3])
                } else {
                    frameMap?.set(frameNumber, parts[2])
                }

                val debugString1 = getDebugLine(frameNumber)
                top_text_view?.text   = debugString1

                if (isBeep == true){
                    beepManager.playBeepSound()
                }

                if (totalFrames == frameMap?.size ) {
                    // All frames collected, reconstruct the file
                reconstructFile()
                    finish()
                }
            }


            //Added preview of scanned barcode
            val imageView = findViewById<ImageView>(R.id.barcodePreview)
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW))
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {
            // Not implemented
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp::WakeLockTag"
        ).apply {
            acquire(10*60*1000L /*10 minutes*/)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.continuous_scan)

        acquireWakeLock()
        if (checkStoragePermissions()) {
//            Toast.makeText(this@ContinuousCaptureActivity, "Permission already True", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@ContinuousCaptureActivity, "Permission already False", Toast.LENGTH_SHORT).show()
            requestForStoragePermissions()
        }

        checkPermission(Manifest.permission.CAMERA,
            CAMERA_PERMISSION_CODE)

        frameMap = java.util.HashMap()
        top_text_view = findViewById<View>(R.id.topTextView) as TextView

        barcodeView = findViewById(R.id.barcode_scanner)
        val formats = listOf(BarcodeFormat.QR_CODE)
        barcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView.initializeFromIntent(intent)
        barcodeView.decodeContinuous(callback)

        beepManager = BeepManager(this)


    }

    override fun onResume() {
        super.onResume()

        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()

        barcodeView.pause()
    }

    fun pause(view: View?) {
        barcodeView.pause()
    }

    fun resume(view: View?) {
        barcodeView.resume()
    }

    fun toggleBeep(view: View?) {
       isBeep = isBeep!!.not()
    }

    fun triggerScan(view: View?) {
        barcodeView.decodeSingle(callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

}