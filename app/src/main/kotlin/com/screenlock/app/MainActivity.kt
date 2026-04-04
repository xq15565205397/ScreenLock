
package com.screenlock.app

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.screenlock.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var selectedMinutes = 30

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName

    companion object {
        private const val REQUEST_CODE_ENABLE_ADMIN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, DeviceAdminReceiver::class.java)

        setupNumberPicker()
        setupButtons()
        checkAdminPermission()
    }

    private fun setupNumberPicker() {
        binding.minutePicker.minValue = 1
        binding.minutePicker.maxValue = 180
        binding.minutePicker.value = selectedMinutes
        binding.minutePicker.setOnValueChangedListener { _, _, newVal -&gt;
            selectedMinutes = newVal
        }
    }

    private fun setupButtons() {
        binding.toggleButton.setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                if (checkAdminPermission()) {
                    startTimer()
                }
            }
        }

        binding.grantPermissionButton.setOnClickListener {
            requestAdminPermission()
        }
    }

    private fun checkAdminPermission(): Boolean {
        val isActive = devicePolicyManager.isAdminActive(componentName)
        binding.grantPermissionButton.visibility = if (isActive) {
            binding.grantPermissionButton.visibility = android.view.View.GONE
            true
        } else {
            binding.grantPermissionButton.visibility = android.view.View.VISIBLE
            false
        }
        return isActive
    }

    private fun requestAdminPermission() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "需要此权限才能锁定屏幕")
        }
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            checkAdminPermission()
        }
    }

    private fun startTimer() {
        val milliseconds = selectedMinutes * 60 * 1000L
        
        timer = object : CountDownTimer(milliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateRemainingTime(millisUntilFinished)
            }

            override fun onFinish() {
                lockScreen()
                resetTimerUI()
            }
        }.start()

        isTimerRunning = true
        binding.toggleButton.text = getString(R.string.stop_timer)
        binding.minutePicker.isEnabled = false
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
        resetTimerUI()
    }

    private fun resetTimerUI() {
        isTimerRunning = false
        binding.toggleButton.text = getString(R.string.start_timer)
        binding.remainingTimeText.text = "--:--"
        binding.minutePicker.isEnabled = true
    }

    private fun updateRemainingTime(millisUntilFinished: Long) {
        val minutes = (millisUntilFinished / 1000) / 60
        val seconds = (millisUntilFinished / 1000) % 60
        binding.remainingTimeText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun lockScreen() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()
        }
    }
}
