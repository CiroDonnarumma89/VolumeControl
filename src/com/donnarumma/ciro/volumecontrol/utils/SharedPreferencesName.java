package com.donnarumma.ciro.volumecontrol.utils;

/**
 * Questa interfaccia contiene le costanti utilizzate per accedere alle Shared Preferences.
 */
public interface SharedPreferencesName {
	
	//Nome delle preferenze
	final static String SHARED_PREFERENCES_NAME = "volume_control_preferences";

	//Chiavi identificative delle preferenze
	final static String KEY_MINIMUM_VOLUME = "minimum_volume";
	final static String KEY_MAXIMUM_VOLUME = "maximum_volume";
	final static String KEY_TRIGGER_INTERVAL = "trigger_interval";
	final static String KEY_ADJUSTING_VOLUME = "adjusting_volume";
	final static String KEY_FLASH_BLINKING = "flash_blinking";


	//Valori di default delle preferenze
	final static int DEFAULT_MINIMUM_VOLUME = 0;
	final static int DEFAULT_MAXIMUM_VOLUME = 7;
	final static int DEFAULT_TRIGGER_INTERVAL = 5;
	final static boolean DEFAULT_ADJUSTING_VOLUME = false;
	final static boolean DEFAULT_FLASH_BLINKING = false;
	
	
	//final static String KEY_SAMPLING_PERIOD = "sampling_period"; //TODO: Controllare
//	final static int DEFAULT_SAMPLING_PERIOD = 500; 
	
}
