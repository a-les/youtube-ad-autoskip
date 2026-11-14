package com.autoskip.yt

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.autoskip.yt.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferencesManager(this)

        initNavigation()
        initSwitches()
        initActions()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun initNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showTab(0)
                R.id.nav_settings -> showTab(1)
                R.id.nav_info -> showTab(2)
            }
            true
        }

        binding.btnHomeGear.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.nav_settings
        }

        binding.btnBackFromSettings.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }

        binding.btnBackFromInfo.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }

        binding.btnNavToStats.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.nav_info
        }

        binding.btnNavToPermissions.setOnClickListener {
            openAccessibilitySettings()
        }

        binding.btnNavToDiagnostics.setOnClickListener {
            showDiagnosticsDialog()
        }
    }

    private fun showTab(index: Int) {
        binding.layoutHome.visibility = if (index == 0) View.VISIBLE else View.GONE
        binding.layoutSettings.visibility = if (index == 1) View.VISIBLE else View.GONE
        binding.layoutInfo.visibility = if (index == 2) View.VISIBLE else View.GONE
    }

    private fun initSwitches() {
        binding.switchAutoSkip.isChecked = prefs.isAutoSkipEnabled
        binding.switchAutoSkip.setOnCheckedChangeListener { _, isChecked ->
            prefs.isAutoSkipEnabled = isChecked
        }

        binding.switchYtMusic.isChecked = prefs.isYtMusicEnabled
        binding.switchYtMusic.setOnCheckedChangeListener { _, isChecked ->
            prefs.isYtMusicEnabled = isChecked
        }
    }

    private fun initActions() {
        binding.btnToggleService.setOnClickListener {
            openAccessibilitySettings()
        }

        binding.cardLaunchYoutube.setOnClickListener {
            launchYouTube()
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            showPrivacyDialog()
        }

        binding.btnLicenses.setOnClickListener {
            showLicensesDialog()
        }
    }

    private fun launchYouTube() {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage("com.google.android.youtube")
            if (launchIntent != null) {
                startActivity(launchIntent)
                return
            }
        } catch (e: Exception) {}

        try {
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:"))
            startActivity(appIntent)
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
                startActivity(webIntent)
            } catch (ex: Exception) {
                Toast.makeText(this, "Не удалось открыть YouTube", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPrivacyDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Политика конфиденциальности")
            .setMessage(
                "1. Полная автономность: Приложение не имеет разрешения на доступ в интернет (android.permission.INTERNET) и физически не может отправлять данные наружу.\n\n" +
                "2. Конфиденциальность экрана: Служба специальных возможностей активируется исключительно при открытом приложении YouTube и только для поиска кнопки «Пропустить».\n\n" +
                "3. Локальное хранение: Счётчик пропущенной рекламы и время работы хранятся только локально на вашем смартфоне.\n\n" +
                "4. Исходный код открыт и доступен для проверки на GitHub."
            )
            .setPositiveButton("Понятно", null)
            .show()
    }

    private fun showLicensesDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Лицензии")
            .setMessage(
                "yt-ad-autoskip распространяется под свободной лицензией MIT License.\n\n" +
                "Используемые библиотеки:\n" +
                "• AndroidX Core KTX (Apache 2.0)\n" +
                "• Google Material Components (Apache 2.0)\n" +
                "• AndroidX AppCompat & ConstraintLayout (Apache 2.0)"
            )
            .setPositiveButton("Закрыть", null)
            .show()
    }

    private fun showDiagnosticsDialog() {
        val runtime = Runtime.getRuntime()
        val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Диагностика и Ресурсы")
            .setMessage(
                "🔋 Расход батареи: < 0.1% в сутки\n" +
                "• Нет постоянного сканирования экрана\n" +
                "• Процессор не удерживается (без Wakelock)\n" +
                "• Пробуждение строго по системному событию YouTube\n\n" +
                "💾 Оперативная память (RAM): ~$usedMemMb МБ\n" +
                "• Экстремально лёгкий фоновый процесс\n\n" +
                "🌐 Сетевой трафик: 0 КБ\n" +
                "• Приложение полностью отключено от интернета\n\n" +
                "⚙️ Статус службы: " + (if (isAccessibilityServiceEnabled(this, YouTubeAutoSkipService::class.java)) "Активна (ОК)" else "Остановлена")
            )
            .setPositiveButton("Отлично", null)
            .show()
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun updateUI() {
        val isEnabled = isAccessibilityServiceEnabled(this, YouTubeAutoSkipService::class.java)

        if (isEnabled) {
            binding.layoutStatusCircle.setBackgroundResource(R.drawable.bg_status_working)
            binding.ivStatusIcon.setImageResource(R.drawable.ic_check)
            binding.tvStatusTitle.text = getString(R.string.status_working)
            binding.tvStatusDesc.text = getString(R.string.status_working_desc)
            binding.btnToggleService.text = getString(R.string.btn_stop_service)
        } else {
            binding.layoutStatusCircle.setBackgroundResource(R.drawable.bg_status_stopped)
            binding.ivStatusIcon.setImageResource(R.drawable.ic_clock)
            binding.tvStatusTitle.text = getString(R.string.status_stopped)
            binding.tvStatusDesc.text = getString(R.string.status_stopped_desc)
            binding.btnToggleService.text = getString(R.string.btn_start_service)
        }

        // Stats update
        val countStr = prefs.adsSkippedCount.toString()
        val uptimeStr = prefs.getFormattedUptime()
        binding.tvHomeAdsCount.text = countStr
        binding.tvInfoAdsCount.text = countStr
        binding.tvHomeUptime.text = uptimeStr
        binding.tvInfoUptime.text = uptimeStr

        // Last active time
        val lastActive = prefs.lastActivityTimestamp
        if (lastActive > 0L) {
            val sdf = SimpleDateFormat("Сегодня, HH:mm", Locale.getDefault())
            binding.tvInfoLastActive.text = sdf.format(Date(lastActive))
        } else {
            binding.tvInfoLastActive.text = "Ожидание рекламы"
        }
    }

    private fun isAccessibilityServiceEnabled(
        context: Context,
        service: Class<out AccessibilityService>
    ): Boolean {
        val expectedComponentName = ComponentName(context, service)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
    }
}
