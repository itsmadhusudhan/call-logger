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
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            Log.d("CallStateReceiver", state.toString())
            if (incomingNumber != null) {
                Log.d("CallStateReceiver", incomingNumber)
            }
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    print("This is extra state ringing")
                    print(intent.extras)

                    // The phone is ringing
                    Log.d("CallStateReceiver", "Incoming call from $incomingNumber")

                    if(incomingNumber != null) {
                        val i = Intent(
                            context,
                            IncomingCallActivity::class.java
                        )
                        i.putExtra("incomingNumber",incomingNumber);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        context.startActivity(i)
                    }
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    Log.d("CallStateReceiver", "outgoing call from $incomingNumber")
                    // A call is dialing, active or on hold
                    Log.d("CallStateReceiver", "Call answered")
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // The phone is idle
                    Log.d("CallStateReceiver", "Call ended: "+intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER))

                }
            }
        }
    }
}