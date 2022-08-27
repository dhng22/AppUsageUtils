package com.teasoft.appusageutils

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.teasoft.utils.AppUsageManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        Log.e("TAG", "start ")
        resultLauncher.launch(intent)

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val weekAppUsageManager = AppUsageManager.queryWeekUsageTime(this,1)

        GlobalScope.launch {
            weekAppUsageManager.collect {
                it.forEach {
                    Log.e("TAG", "${SimpleDateFormat("EEE", Locale("en","UK")).format(it.timeStamp)} ${it.name} for: ${it.totalUsedTime/60000} minutes")
                }
                Log.e("end of day", "----------------------------------------------------------------------------------------- ")
            }
        }
    }
}