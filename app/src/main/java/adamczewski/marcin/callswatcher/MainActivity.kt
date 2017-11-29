package adamczewski.marcin.callswatcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.stop_btn).setOnClickListener {
            stopCallsMonitorService()
            finish()
        }

        if (savedInstanceState == null && checkForPermissions()) {
            startCallsMonitorService()
        }
    }

    private fun checkForPermissions(): Boolean {
        return if (hasPermissions()) {
            true
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE), 1)
            false
        }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun startCallsMonitorService() {
        startServiceWithAction(CallsMonitorService.ACTION_START)
    }

    private fun stopCallsMonitorService() {
        startServiceWithAction(CallsMonitorService.ACTION_STOP)
    }

    private fun startServiceWithAction(action: String) {
        val intent = Intent(this, CallsMonitorService::class.java).apply {
            this.action = action
        }
        startService(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (arePermissionsGranted(grantResults)) {
            startCallsMonitorService()
        }
    }

    private fun arePermissionsGranted(grantResults: IntArray): Boolean {
        return if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) false else grantResults.isNotEmpty()
    }
}