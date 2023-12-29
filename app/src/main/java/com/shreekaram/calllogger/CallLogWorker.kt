package com.shreekaram.calllogger

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.CallLog
import android.provider.MediaStore
import android.text.format.DateUtils
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.OutputStream
import kotlin.streams.asSequence

class CallLogWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        //                            empty list if Call record
        val callRecords = mutableListOf<CallRecord>()
//                            val dateFrom = "1703751885344"
//                           access call logs
        val cursor =  applicationContext.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            CallLog.Calls.DATE + " DESC"
        )
//                            val cursor = contentResolver.query(
//                                CallLog.Calls.CONTENT_URI,
//                                null,
//                                "${CallLog.Calls.DATE} >= ?",
//                                arrayOf(dateFrom),
//                                CallLog.Calls.DATE + " DESC"
//                            )

        cursor?.use {
            val number = it.getColumnIndex(CallLog.Calls.NUMBER)
            val type = it.getColumnIndex(CallLog.Calls.TYPE)
            val date = it.getColumnIndex(CallLog.Calls.DATE)
            val duration = it.getColumnIndex(CallLog.Calls.DURATION)
            val name = it.getColumnIndex(CallLog.Calls.CACHED_NAME)


            while (it.moveToNext()) {
                val phoneNumber = it.getString(number)
                val callType = it.getString(type)

                var callDate = it.getString(date)
                //convert date to human readable format
//                                    callDate = DateUtils.formatDateTime(
//                                        applicationContext,
//                                        callDate.toLong(),
//                                        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_TIME
//                                    )

                val callDuration = it.getString(duration)
                val callerName = it.getString(name)?:"Unknown"
                callRecords.add(
                    CallRecord(
                        callerName,
                        phoneNumber,
                        callType,
                        callDate,
                        callDuration
                    )
                )
            }

        }
        cursor?.close()
        println(callRecords)

        // store the call records into file
        // human readable date format with file name
       val time= DateUtils.formatDateTime(
            applicationContext,
            System.currentTimeMillis(),
            DateUtils.FORMAT_SHOW_TIME
        )


        val fileName = "callRecords_$time.txt"
        println("Printing file name $fileName")
        val content=callRecords.toString()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = applicationContext.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            uri?.let {
                val outputStream: OutputStream? = resolver.openOutputStream(it)
                outputStream?.bufferedWriter()?.use { it.write(content) }
//                                    close stream
                outputStream?.close()
            }


        } else {
            val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDirectory, fileName)
            file.writeText(content)
        }

        val notification= NotificationCompat.Builder(applicationContext,"call_logger")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Call Log file $fileName")
            .setContentText("Saved to Downloads. Good day to you!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(applicationContext)){
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }

//            generate random int for notification id
            val notificationId=(0..100).random()

            notify(notificationId,notification)
        }

        return Result.success()
    }
}