package com.example.cameraxblackjpegbug

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val _permissionCamera: Int = 19723
    private lateinit var capturingProcess: CapturingProcess

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val permissionsMissing = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED

        if (permissionsMissing) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CAMERA
                ),
                _permissionCamera
            )
            return
        }

        // Create Capturing Process
        capturingProcess = CapturingProcess(applicationContext, this)

        // Register capture action to button
        fab.setOnClickListener {
            capturingProcess.requestPicture()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            _permissionCamera -> {
                var permissionsGranted = true
                for (grantResult in grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        permissionsGranted = false
                    }
                }
                if (permissionsGranted) {// Create Capturing Process
                    capturingProcess = CapturingProcess(applicationContext, this)

                    // Register capture action to button
                    fab.setOnClickListener {
                        capturingProcess.requestPicture()
                    }
                    return
                }
                // Disable camera feature
                finish()
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
