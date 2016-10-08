package com.donnarumma.ciro.volumecontrol.utils;


import com.donnarumma.ciro.volumecontrol.services.VolumeService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class VolumeServiceAlarmManager {
	private final static String LOG_TAG = VolumeServiceAlarmManager.class.getSimpleName();
	
	public static void registerAlarm(Context context, long triggerIntervalInMillis){
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intentToFire = new Intent(context, VolumeService.class);
		PendingIntent alarmIntent = PendingIntent.getService(context, 0, intentToFire,0);
		int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
		long timeOrLegthofWait = triggerIntervalInMillis;
		alarmManager.setRepeating(alarmType, timeOrLegthofWait, timeOrLegthofWait, alarmIntent);
		Log.d(LOG_TAG, "Alarm registrato con periodo " + timeOrLegthofWait + " ms!");
	}
	
	public static void clearAlarm(Context context){
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intentToFire = new Intent(context, VolumeService.class);
		PendingIntent alarmIntent = PendingIntent.getService(context, 0, intentToFire,0);
		alarmManager.cancel(alarmIntent);
		Log.d(LOG_TAG, "Alarm eliminato!");
	}
	
}
