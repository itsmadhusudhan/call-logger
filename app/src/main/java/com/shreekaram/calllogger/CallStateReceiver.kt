package com.shreekaram.calllogger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_VOICEMAIL_NUMBER)
            Log.d("CallStateReceiver", state.toString())
            Log.d("CallStateReceiver", incomingNumber.toString())
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // The phone is ringing
                    Log.d("CallStateReceiver", "Incoming call from $incomingNumber")
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // A call is dialing, active or on hold
                    Log.d("CallStateReceiver", "Call answered")
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // The phone is idle
                    Log.d("CallStateReceiver", "Call ended")

                }
            }
        }
    }
}