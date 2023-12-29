package com.shreekaram.calllogger

import android.app.Person
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.format.DateUtils
import android.util.Log
import java.io.File
import java.io.InputStream
import java.io.OutputStream


enum class WhatsAppCallType {
    INCOMING,
    OUTGOING,
}

enum class CallStatus {
    MISSED,
    RECEIVED,
    DIALED,
    PENDING,
}

data class WhatsAppCall(
    val id: Int,
    val callType: WhatsAppCallType,
    val callPerson: String = "Unknown",
    val phoneNumber: String?,
    val callTime: Long,
    val callDuration: Long,
    val callStatus: CallStatus = CallStatus.PENDING,
)

class AppNotificationService : NotificationListenerService() {
    private var onGoingCall: WhatsAppCall? = null
    private fun printNotification(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName


        val notification = sbn?.notification
        val extras = notification?.extras
        val title = extras?.getString("android.title")
        val text = extras?.getCharSequence("android.text")
        Log.d("AppNotificationService", title.toString())
        Log.d("AppNotificationService", text.toString())
        Log.d("AppNotificationService", packageName.toString())
        Log.d("AppNotificationService", sbn?.isOngoing.toString())
//        extras?.keySet()?.forEach {
//            Log.d("AppNotificationService", it)
//           extras.get(it)?.let { it1 -> Log.d("AppNotificationService", it1.toString()) }
//        }
        println("Extras: ")
        println(extras)
        val person = extras?.get("android.callPerson")
        println(person)
        if (person != null) {
            println("Printing person: ")
            println((person as Person).name)
            println((person).uri)
        }
        val supplier = extras?.get("android.people.list")
        println("Printing supplier: ")
        println(supplier)
        if (supplier != null) {
            println((supplier as ArrayList<Person>)[0].name)
            println((supplier)[0].uri)
        }

        val appInfo = extras?.get("android.appInfo")
        println("Printing appInfo: ")
        println(appInfo)
        println(appInfo is ArrayList<*>)
        if (appInfo != null && appInfo is ArrayList<*>) {
            println(((appInfo as ArrayList<Person>)[0]).name)
            println(appInfo[0].uri)
        }
        Log.d("AppNotificationService", extras?.get("android.callPerson").toString())


    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName
        Log.d("AppNotificationService", sbn?.id.toString())
        if (packageName == "com.whatsapp") {
            val notification = sbn.notification
            val extras = notification?.extras

            if (extras?.getInt("android.callType") == null) {
                return
            }


            val callType =
                if (extras.getInt("android.callType") == 1) WhatsAppCallType.INCOMING else WhatsAppCallType.OUTGOING
            val callPerson = extras.get("android.callPerson")
            val supplier = extras.get("android.people.list")
            var phoneNumber = ""
            var personName = ""
//            Log.d(
//                "AppNotificationService",
//                DateUtils.formatDateTime(
//                    this,
//                    sbn.postTime,
//                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
//                )
//            )
//            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
//            println(audioManager.mode)
//            val mode = audioManager.mode
//            if (AudioManager.MODE_IN_CALL == mode) {
//                // device is in a telephony call
//            } else if (AudioManager.MODE_IN_COMMUNICATION == mode) {
//                Log.d("AudioMode", "IN_COMMUNICATION")
//                // device is in communication mode, i.e. in a VoIP or video call
//            } else if (AudioManager.MODE_RINGTONE == mode) {
//                // device is in ringing mode, some incoming is being signalled
//                Log.d("AudioMode", "Ringing")
//
//            } else {
//                Log.d("AudioMode", "Normal")
//                // device is in normal mode, no incoming and no audio being played
//            }
            println(extras)

            if (callPerson != null) {
//                Log.d("AppNotificationService", "Call person is not null")
                val person = callPerson as Person
                personName = person.name.toString()
            }

            if (supplier != null) {
//                Log.d("AppNotificationService", "supplier is not null")
                val person = (supplier as ArrayList<Person>)[0]
                phoneNumber = person.uri.toString()
            }

            notification.actions.forEach {
                Log.d("AppNotificationService", it.title.toString())
//                Log.d("AppNotificationService", it.actionIntent.toString())
            }

            Log.d(
                "AppNotificationService",
                "WhatsApp call $personName on $phoneNumber type $callType"
            )

            if (onGoingCall != null) {
                onGoingCall = onGoingCall?.copy(
                    callType = callType,
                    callPerson = personName,
                    phoneNumber = phoneNumber,
                    callTime = System.currentTimeMillis(),
                )
            } else {
                onGoingCall = WhatsAppCall(
                    id = sbn.id,
                    callType = callType,
                    callPerson = personName,
                    phoneNumber = phoneNumber,
                    callTime = System.currentTimeMillis(),
                    callDuration = 0,
                )
            }
        }


//        Log.d("AppNotificationService", "Notification posted")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        onGoingCall?.let { whatsAppCall ->
            val callDuration = (System.currentTimeMillis() - whatsAppCall.callTime) / 1000


            onGoingCall = whatsAppCall.copy(
                callDuration = callDuration,
                callStatus = if (callDuration > 0) CallStatus.RECEIVED else CallStatus.MISSED
            )

            Log.d(
                "AppNotificationService",
                "WhatsApp call ${whatsAppCall.callPerson} on ${whatsAppCall.phoneNumber} type ${whatsAppCall.callType} duration ${callDuration / 1000} seconds"
            )
            val time = DateUtils.formatDateTime(
                applicationContext,
                System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME
            )

            val fileName = "whatsappCallRecords_$time.txt"
            println("Printing WhatsApp file name $fileName")
            val content = onGoingCall.toString()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = applicationContext.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

                uri?.let { uri1 ->
                    val outputStream: OutputStream? = resolver.openOutputStream(uri1,"wa")
                    outputStream?.bufferedWriter()?.use { it.write(content) }
                    //  close stream
                    outputStream?.close()
                }

                uri?.let { uri1 ->
                    val outputStream: OutputStream? = resolver.openOutputStream(uri1,"wa")
                    outputStream?.bufferedWriter()?.use { it.write(content) }
                    //  close stream
                    outputStream?.close()
                }
            } else {
                val downloadsDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDirectory, fileName)
                file.writeText(content)
            }

            onGoingCall = null
        }
        val notification = sbn?.notification
        val extras = notification?.extras
        println(extras)
        if (extras != null) {
            var string = "Bundle{"
            for (key in extras.keySet()!!) {
                string += " " + key + " => " + extras.get(key) + ";"
            }
            string += " }Bundle"

            Log.d("AppNotificationService", string)
        }
//        Log.d("AppNotificationService", sbn?.id.toString())
//        println(extras is Parcelable)
//        println(extras?.getInt("android.progress"))
//        Log.d("AppNotificationService", sbn?.packageName.toString())
        Log.d("AppNotificationService", "Notification removed")
    }
}


/**
 * 2023-12-29 21:39:34.207 14223-14223 AppNotificationService  com.shreekaram.calllogger
 * D  Bundle{ android.title => Mummy; android.reduced.images => true;
 * android.subText => null;
 * android.template => android.app.Notification$CallStyle;
 * android.showChronometer => false; android.text =>
 * Ringing…; android.progress => 0;
 * androidx.core.app.extra.COMPAT_TEMPLATE => androidx.core.app.NotificationCompat$BigTextStyle;
 * android.progressMax => 0; android.callIsVideo => false; android.showWhen => true;
 * in_conference_scene_mode => false; android.callType => 2;
 * android.bigText => Ringing…; android.infoText => null;
 * android.progressIndeterminate => false;
 * android.remoteInputHistory => null; in_full_screen_mode => false; }Bundle
 *
 */