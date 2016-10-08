package com.donnarumma.ciro.volumecontrol.utils;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * Questa classe permette di acquisire il valore attuale del volume dell'avviso di chiamata, del volume della sveglia e del volume della musica.
 * 
 */
public class VolumeManager {
	
	private static final String LOG_TAG = VolumeManager.class.getSimpleName();
	
	private AudioManager audioManager;			//Audio manager per l'accesso, sia in lettura che in scrittura, ai livelli di volume del sistema
	private int maxRingtoneVolume;				//Livello massimo di volume impostabile per l'avviso di chiamata
	private int maxAlarmVolume;					//Livello massimo di volume impostabile per la sveglia
	private int maxMusicVolume;					//Livello massimo di volume impostabile per la musica
	
	
	/**
	 * Instanzia un nuovo volume manager
	 *
	 * @param context Il contesto da usare. Solitamente la tua Application o Activity
	 */
	public VolumeManager(Context context){
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		maxRingtoneVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
		maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		Log.d(LOG_TAG, "Volume manager costruito!");
	}
	
	
	/**
	 * Ritorna il valore corrente del volume di avvisio di chiamata.
	 *
	 * @return Volume dell'avviso di chiamata.
	 */
	public int getRingtoneVolume(){
		return audioManager.getStreamVolume(AudioManager.STREAM_RING);	
	}

	
	/**
	 * Ritorna il valore corrente del volume della sveglia.
	 *
	 * @return Volume della sveglia
	 */
	public int getAlarmVolume(){
		return audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
	}
	
	
	/**
	 * Ritorna il valore corrente del volume della musica.
	 *
	 * @return Volume della musica
	 */
	public int getMusicVolume(){
		return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}
	
	/**
	 * Ritorna il valore massimo del volume di avvisio di chiamata.
	 *
	 * @return Volume massimo dell'avviso di chiamata.
	 */
	public int getMaxRingtoneVolume(){
		return audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);	
	}

	
	/**
	 * Ritorna il valore massimo del volume della sveglia.
	 *
	 * @return Volume massimo della sveglia
	 */
	public int getMaxAlarmVolume(){
		return audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
	}
	
	
	/**
	 * Ritorna il valore massimo del volume della musica.
	 *
	 * @return Volume massimo della musica
	 */
	public int getMaxMusicVolume(){
		return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}
	
	/**
	 * Imposta il volume dell'avviso di chiamata.
	 *
	 * @param volume Il nuovo livello di volume
	 * @throws IllegalArgumentException Nel caso in cui <b>volume</b> sia minore di 0 o maggiore del suo valore massimo
	 */
	public void setRingtoneVolume(int volume) throws IllegalArgumentException {
		if (volume < 0 || volume > maxRingtoneVolume){
			Log.e(LOG_TAG, "Il volume dell'avviso di chiamata deve appartenere all'intervallo [0, " + maxRingtoneVolume + "]");
			throw new IllegalArgumentException("Volume must be between 0 and " + maxRingtoneVolume);
		}
		audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
		Log.d(LOG_TAG, "Volume dell'avviso di chiamata impostato al livello " + volume);
	}

	/**
	 * Imposta il volume della sveglia.
	 *
	 * @param volume Il nuovo livello di volume
	 * @throws IllegalArgumentException Nel caso in cui <b>volume</b> sia minore di 0 o maggiore del suo valore massimo
	 */
	public void setAlarmVolume(int volume) throws IllegalArgumentException {
		if (volume < 0 || volume > maxAlarmVolume){
			Log.e(LOG_TAG, "Il volume della sveglia deve appartenere all'intervallo [0, " + maxAlarmVolume + "]");
			throw new IllegalArgumentException("Volume must be between 0 and " + maxAlarmVolume);
		}
		audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);
		Log.d(LOG_TAG, "Volume della sveglia impostato al livello " + volume);
	}
	
	/**
	 * Imposta il volume della musica
	 *
	 * @param volume Il nuovo livello di volume
	 * @throws IllegalArgumentException Nel caso in cui <b>volume</b> sia minore di 0 o maggiore del suo valore massimo
	 */
	public void setMusicVolume(int volume) throws IllegalArgumentException {
		if (volume < 0 || volume > maxMusicVolume){
			Log.e(LOG_TAG, "Il volume della musica deve appartenere all'intervallo [0, " + maxMusicVolume + "]");
			throw new IllegalArgumentException("Volume must be between 0 and " + maxMusicVolume);
		}
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
		Log.d(LOG_TAG, "Volume della musica impostato al livello " + volume);
	}

}
