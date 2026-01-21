package com.voicenotes.motorcycle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.Activity

class FinishActivityReceiver(private val activity: Activity) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.voicenotes.motorcycle.FINISH_ACTIVITY") {
            activity.finish()
        }
    }
}
