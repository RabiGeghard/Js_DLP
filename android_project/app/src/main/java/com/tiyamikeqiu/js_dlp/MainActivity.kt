package com.tiyamikeqiu.js_dlp




import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import android.provider.Settings
import android.net.Uri

class MainActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_CODE = 100
    private val STORAGE_PERMISSION_CODE = 101

    private lateinit var scanBtn: Button
    private lateinit var scannedValue: TextView

    // Function to check and request permission.
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
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

                Toast.makeText(this, "onActivityResult: Manage External Storage Permissions Granted", Toast.LENGTH_SHORT).show()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

// Defining Buttons
        val storage: Button? = findViewById(R.id.storage)
        val camera: Button? = findViewById(R.id.camera)

        // Set Buttons on Click Listeners
        storage?.setOnClickListener {

            if (checkStoragePermissions()) {
                Toast.makeText(this@MainActivity, "Permission already True", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Permission already False", Toast.LENGTH_SHORT).show()
                requestForStoragePermissions()
            }

        }
        camera?.setOnClickListener {
            checkPermission(Manifest.permission.CAMERA,
                CAMERA_PERMISSION_CODE)
        }

        scanBtn = findViewById(R.id.scanBtn)
        scannedValue = findViewById(R.id.scannedValue)

        registerUiListener()
    }

    private fun registerUiListener() {
        scanBtn.setOnClickListener {
            scannerLauncher.launch(
                ScanOptions().setPrompt("Scan Barcode")
                    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            )
        }
    }

    private val scannerLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(
        ScanContract()
    ) { result ->

        if (result.contents == null) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        } else {
            scannedValue.text = buildString {
                append("Scanned Value : ")
                append(result.contents)
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}