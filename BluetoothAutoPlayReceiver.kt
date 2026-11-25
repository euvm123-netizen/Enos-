package com.enos.music

import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

class BluetoothAutoPlayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED)
        val connected = state == BluetoothProfile.STATE_CONNECTED || intent.action == android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED
        if (!connected) return
        val preferences = context.getSharedPreferences("enos_settings", Context.MODE_PRIVATE)
        if (!preferences.getBoolean("auto_play_bluetooth", true)) return
        val serviceIntent = Intent(context, MusicPlaybackService::class.java).setAction(MusicPlaybackService.ACTION_AUTO_PLAY)
        try {
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (_: Exception) {
            // Android/Samsung may block automatic background launch. User can open Enos and start playback.
        }
    }
}
