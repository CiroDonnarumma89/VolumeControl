package com.donnarumma.ciro.volumecontrol.broadcastreceivers;

import static com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName.DEFAULT_ADJUSTING_VOLUME;
import static com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName.DEFAULT_TRIGGER_INTERVAL;
import static com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName.KEY_ADJUSTING_VOLUME;
import static com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName.KEY_TRIGGER_INTERVAL;
import static com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName.SHARED_PREFERENCES_NAME;

import com.donnarumma.ciro.volumecontrol.utils.VolumeServiceAlarmManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

public class BootCompletedReceiver extends BroadcastReceiver{
	public final static String LOG_TAG = BootCompletedReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(LOG_TAG, "Boot completato!");
		SharedPreferences mySharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		boolean adjustingVolume = mySharedPreferences.getBoolean(KEY_ADJUSTING_VOLUME, DEFAULT_ADJUSTING_VOLUME);
		int triggerInterval = mySharedPreferences.getInt(KEY_TRIGGER_INTERVAL, DEFAULT_TRIGGER_INTERVAL);
		if (adjustingVolume)
			VolumeServiceAlarmManager.registerAlarm(context, triggerInterval*1000);
	}
}
