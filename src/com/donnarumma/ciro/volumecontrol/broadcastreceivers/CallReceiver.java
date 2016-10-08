package com.donnarumma.ciro.volumecontrol.broadcastreceivers;

import static com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName.DEFAULT_ADJUSTING_VOLUME;

import static com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName.DEFAULT_TRIGGER_INTERVAL;
import static com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName.KEY_ADJUSTING_VOLUME;
import static com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName.KEY_TRIGGER_INTERVAL;
import static com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName.SHARED_PREFERENCES_NAME;

import java.util.Date;

import com.donnarumma.ciro.volumecontrol.utils.FlashBlinker;
import com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName;
import com.donnarumma.ciro.volumecontrol.utils.VolumeServiceAlarmManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;



public class CallReceiver extends PhonecallReceiver {
	
	private final static String LOG_TAG = CallReceiver.class.getSimpleName();

	@Override
	protected void onIncomingCallReceived(Context ctx, String number, Date start) {
		Log.d(LOG_TAG, "Chiamata in arrivo!");
		VolumeServiceAlarmManager.clearAlarm(ctx.getApplicationContext());
		SharedPreferences sharedPreferences = ctx.getSharedPreferences(SharedPreferencesName.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		boolean flashBlinking = sharedPreferences.getBoolean(SharedPreferencesName.KEY_FLASH_BLINKING, SharedPreferencesName.DEFAULT_FLASH_BLINKING);
		if (flashBlinking)
			FlashBlinker.getFlashBlinker().start(ctx.getApplicationContext());
	}
	
	@Override
	protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
		Log.d(LOG_TAG, "La chiamata ha avuto risposta!");
		FlashBlinker.getFlashBlinker().stop();
	}

	@Override
	protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
		Log.d(LOG_TAG, "Chiamata terminata!");
		restoreAlarm(ctx);
		
	}

	@Override
	protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onMissedCall(Context ctx, String number, Date start) {
		Log.d(LOG_TAG, "Chiamata senza risposta!");
		FlashBlinker.getFlashBlinker().stop();
		restoreAlarm(ctx);
	}

	private void restoreAlarm(Context ctx) {
		SharedPreferences mySharedPreferences = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		int triggerInterval = mySharedPreferences.getInt(KEY_TRIGGER_INTERVAL, DEFAULT_TRIGGER_INTERVAL);
		boolean adjustingVolume = mySharedPreferences.getBoolean(KEY_ADJUSTING_VOLUME, DEFAULT_ADJUSTING_VOLUME);
		if (adjustingVolume)
			VolumeServiceAlarmManager.registerAlarm(ctx.getApplicationContext(), triggerInterval*1000);
	}

}
