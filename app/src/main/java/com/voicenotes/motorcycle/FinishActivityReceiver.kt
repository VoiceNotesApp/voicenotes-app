package com.voicenotes.motorcycle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FinishActivityReceiver : BroadcastReceiver() {
    var mainActivity: MainActivity? = null
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.voicenotes.motorcycle.FINISH_ACTIVITY") {
            mainActivity?.finish()
        }
    }
}
