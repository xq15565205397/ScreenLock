package com.screenlock.app

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.screenlock.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var selectedMinutes = 30

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private lateinit var sharedPreferences: android.content.SharedPreferences

    companion object {
        private const val REQUEST_CODE_ENABLE_ADMIN = 1001
        private const val PREFS_NAME = "LockConfig"
        private const val KEY_IS_TIMER_ACTIVE = "is_timer_active"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setupNumberPicker()
        setupButtons()
        checkAdminPermission()
        checkAccessibilityService()
    }

    private fun setupNumberPicker() {
        binding.minutePicker.minValue = 1
        binding.minutePicker.maxValue = 180
        binding.minutePicker.value = selectedMinutes
        binding.minutePicker.setOnValueChangedListener { _, _, newVal ->
            selectedMinutes = newVal
        }
    }

    private fun setupButtons() {
        binding.toggleButton.setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                if (checkAdminPermission() && checkAccessibilityService()) {
                    startTimer()
                }
            }
        }

        binding.grantPermissionButton.setOnClickListener {
            requestAdminPermission()
        }

        binding.releaseOwnerButton.setOnClickListener {
            releaseDeviceOwner()
        }
    }

    private fun checkAdminPermission(): Boolean {
        val isActive = devicePolicyManager.isAdminActive(componentName)
        binding.grantPermissionButton.visibility = if (isActive) {
            android.view.View.GONE
        } else {
            android.view.View.VISIBLE
        }
        return isActive
    }

    private fun checkAccessibilityService(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val isEnabled = enabledServices?.contains(packageName) == true
        if (!isEnabled) {
            showAccessibilityDialog()
        }
        return isEnabled
    }

    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this)
            .setTitle("启用无障碍服务")
            .setMessage("为了防止在定时期间卸载应用，请先在系统设置中启用无障碍服务。")
            .setPositiveButton("去设置") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun requestAdminPermission() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "需要此权限才能锁定屏幕")
        }
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
    }

    @Deprecated("Deprecated in Java")
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
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.remainingTimeText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                resetTimerUI()
            }
        }.start()

        isTimerRunning = true
        binding.toggleButton.text = getString(R.string.stop_timer)
        binding.minutePicker.isEnabled = false

        sharedPreferences.edit()
            .putBoolean(KEY_IS_TIMER_ACTIVE, true)
            .apply()

        LockService.startService(this, selectedMinutes)
        AntiUninstallNotificationService.startService(this)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null

        sharedPreferences.edit()
            .putBoolean(KEY_IS_TIMER_ACTIVE, false)
            .apply()

        LockService.stopService(this)
        AntiUninstallNotificationService.stopService(this)

        resetTimerUI()
    }

    private fun resetTimerUI() {
        isTimerRunning = false
        binding.toggleButton.text = getString(R.string.start_timer)
        binding.remainingTimeText.text = "--:--"
        binding.minutePicker.isEnabled = true

        sharedPreferences.edit()
            .putBoolean(KEY_IS_TIMER_ACTIVE, false)
            .apply()
    }

    private fun releaseDeviceOwner() {
        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
            stopTimer()
            devicePolicyManager.clearDeviceOwnerApp(packageName)
            Toast.makeText(this, R.string.owner_released, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.not_device_owner, Toast.LENGTH_SHORT).show()
        }
    }
}
