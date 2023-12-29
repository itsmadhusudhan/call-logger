package com.shreekaram.calllogger

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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


//        check if there is permission for reading phone state
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            listenToPhoneStateChanges()
        }

        setContent {
            CallLoggerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                    Column {
                        Button(onClick = { requestPermissions() }) {
                            Text("Request Permissions")
                        }
                        Button(onClick = {
                            saveCallLog()
//                            empty list if Call record
//                            val callRecords = mutableListOf<CallRecord>()
////                            val dateFrom = "1703751885344"
////                           access call logs
//                            val cursor = contentResolver.query(
//                                CallLog.Calls.CONTENT_URI,
//                                null,
//                                null,
//                                null,
//                                CallLog.Calls.DATE + " DESC"
//                            )
//                            val cursor = contentResolver.query(
//                                CallLog.Calls.CONTENT_URI,
//                                null,
//                                "${CallLog.Calls.DATE} >= ?",
//                                arrayOf(dateFrom),
//                                CallLog.Calls.DATE + " DESC"
//                            )

//                            cursor?.use {
//                                val number = it.getColumnIndex(CallLog.Calls.NUMBER)
//                                val type = it.getColumnIndex(CallLog.Calls.TYPE)
//                                val date = it.getColumnIndex(CallLog.Calls.DATE)
//                                val duration = it.getColumnIndex(CallLog.Calls.DURATION)
//                                val name = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
//
//
//                                while (it.moveToNext()) {
//                                    val phoneNumber = it.getString(number)
//                                    val callType = it.getString(type)
//
//                                    var callDate = it.getString(date)
//                                    //convert date to human readable format
////                                    callDate = DateUtils.formatDateTime(
////                                        applicationContext,
////                                        callDate.toLong(),
////                                        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_TIME
////                                    )
//
//                                    val callDuration = it.getString(duration)
//                                    val callerName = it.getString(name)?:"Unknown"
//                                    callRecords.add(
//                                        CallRecord(
//                                            callerName,
//                                            phoneNumber,
//                                            callType,
//                                            callDate,
//                                            callDuration
//                                        )
//                                    )
//                                }
//
//                            }
//                            cursor?.close()
//
////                            store the call records into file
//                            val fileName = "callRecords_${System.currentTimeMillis()}.txt"
//                            val content=callRecords.toString()
//
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                                val resolver = applicationContext.contentResolver
//                                val contentValues = ContentValues().apply {
//                                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//                                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
//                                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
//                                }
//
//                                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
//                                uri?.let {
//                                    val outputStream: OutputStream? = resolver.openOutputStream(it)
//                                    outputStream?.bufferedWriter()?.use { it.write(content) }
////                                    close stream
//                                    outputStream?.close()
//                                }
//
//
//                            } else {
//                                val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                                val file = File(downloadsDirectory, fileName)
//                                file.writeText(content)
//                            }

                        }) {
                            Text("Access Call logs")
                        }
                        Button(onClick = {
                            val audioManager=getSystemService(Context.AUDIO_SERVICE) as AudioManager

                            val mode = audioManager.mode

                            if (AudioManager.MODE_IN_CALL == mode) {
                                // device is in a telephony call
                            } else if (AudioManager.MODE_IN_COMMUNICATION == mode) {
                                Log.d("AudioMode", "IN_COMMUNICATION")
                                // device is in communiation mode, i.e. in a VoIP or video call
                            } else if (AudioManager.MODE_RINGTONE == mode) {
                                // device is in ringing mode, some incoming is being signalled
                                Log.d("AudioMode", "Ringing")

                            } else {
                                Log.d("AudioMode", "Normal")
                                // device is in normal mode, no incoming and no audio being played
                            }
                        }) {
                                Text("Get Audio settings")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CallLoggerTheme {
        Greeting("Android")
    }
}