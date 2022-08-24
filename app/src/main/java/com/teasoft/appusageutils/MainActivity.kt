package com.teasoft.appusageutils

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.teasoft.utils.AppUsageManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        resultLauncher.launch(intent)

    }

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val appUsageManager = AppUsageManager.queryAllAppUsageTime(this, false)

        for (appUsedTime in appUsageManager) {
            Log.e(
                "TAG",
                "${appUsedTime.totalUsedTime / 1000 / 60} minutes ${appUsedTime.packageName} from ${appUsedTime.timeStampStart}, isDaily:${appUsedTime.isDaily}"
            )
        }
    }
}