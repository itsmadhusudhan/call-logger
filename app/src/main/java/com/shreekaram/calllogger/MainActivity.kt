package com.shreekaram.calllogger

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.shreekaram.calllogger.navigation.RootNavigationGraph
import com.shreekaram.calllogger.ui.theme.CallLoggerTheme
import java.util.concurrent.TimeUnit

data class CallRecord(
    val name: String,
    val phoneNumber: String,
    val callType: String,
    val date: String,
    val duration: String
)

class MainActivity : ComponentActivity() {
    private val permissions = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
    )

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        }

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val name = "Call Logger"
        val descriptionText = "Channel to notify call logs"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("call_logger", name, importance)
        mChannel.description = descriptionText

        notificationManager.createNotificationChannel(
            mChannel
        )
    }

    private fun saveCallLog() {
        val constraints = Constraints.Builder()
            .build()

        val workRequest = OneTimeWorkRequestBuilder<CallLogWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }


    private fun listenToPhoneStateChanges() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.registerTelephonyCallback(
                applicationContext.mainExecutor,
                object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) {
                        Log.d("CallStateReceiver", state.toString())

                        when (state) {
                            TelephonyManager.CALL_STATE_IDLE -> {
                                Log.d("CallStateReceiver", "Call ended")
                            }

                            TelephonyManager.CALL_STATE_OFFHOOK -> {
                                Log.d("CallStateReceiver", "Call answered")
                            }

                            TelephonyManager.CALL_STATE_RINGING -> {
                                Log.d("CallStateReceiver", "Incoming call")
                            }
                        }
                    }
                })
        } else {
            telephonyManager.listen(object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                }
            }, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun scheduleCallLogWorker() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            WorkManager.getInstance(applicationContext).cancelAllWork()
            val constraints = Constraints.Builder()
                .build()

            val workRequest = PeriodicWorkRequestBuilder<CallLogWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "call_log_worker",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                workRequest
            )
        }
    }

    @SuppressLint(
        "UnusedMaterialScaffoldPaddingParameter"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()
        scheduleCallLogWorker()
        listenToPhoneStateChanges()

        setContent {
            CallLoggerTheme {
                val navHostController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    RootNavigationGraph(navHostController = navHostController)
                }
            }
        }
    }
}




