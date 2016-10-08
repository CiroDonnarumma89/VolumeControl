package com.donnarumma.ciro.volumecontrol.services;

import static com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName.*;

import java.io.IOException;
import java.lang.ref.WeakReference;

import com.donnarumma.ciro.volumecontrol.utils.ILoudnessMeter;
import com.donnarumma.ciro.volumecontrol.utils.LoudnessMeter;
import com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName;
import com.donnarumma.ciro.volumecontrol.utils.VolumeManager;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


/**
 * VolumeService è un servizio che ha la responsabilità di regolare il volume del dispositivo
 * in funzione del rumore ambientale.
 * Bisogna tenere in considerazione che questo servizio, ogni signola volta che viene avviato,
 * effettua la registrazione del rumore mediante il microfono, per un determinato periodo di tempo,
 * per poi regolare il volume in funzione al rumore acquisito.
 * Dunque, per avere una regolazione continua deve essere attivato periodicamente.
 */
public class VolumeService extends Service  implements OnSharedPreferenceChangeListener {
	
	/* TAG per il log */
	private final static String LOG_TAG = VolumeService.class.getSimpleName();
	
	/* Durata in millisecondi del periodo di registrazione dell'intensità di rumore.
	 * Se tale periodo diverta inferiore a 3000 ms la registrazione non avvene correttamente.
	 */
	private final static int LOUDNESS_REGISTRATION_PERIOD = 3000;	
	
	/* Binder usato per collegare il volume service alla main activity */
	private final IBinder binder = new VolumeServiceBinder();

	/* Shared preferences */
	private int minimumVolume;						//Livello di volume oltre il quale il volume service non può scendere
	private int maximumVolume;						//Livello di volume oltre il quale il volume service non può andare
	private boolean adjustingVolume;				//Vale true se si desidera che il volume service effettui la regolazione del volume
	
	/* Riferimento al listener interessato all'intensità di rumore */
	private WeakReference<VolumeServiceListener> listener;
	
	/* Thread che effettua la misurazione dell'intensità di rumore */
	private static LoudnessMeterThread loudnessMeterThread;
	
	
	@Override
	public void onCreate() {
		Log.d(LOG_TAG, "Creazione del servizio!");
		super.onCreate();
		
		loadSharedPreferences();
		Log.d(LOG_TAG, "Servizio creato!");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LOG_TAG, "onStartCommand!");
		if (loudnessMeterThread == null){
			loudnessMeterThread = new LoudnessMeterThread();
		}
		loudnessMeterThread.start();
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		getSharedPreferences(SharedPreferencesName.SHARED_PREFERENCES_NAME, MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
		Log.d(LOG_TAG, "onBind!");
		return binder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(LOG_TAG, "onUnBind!");
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		loudnessMeterThread = null;
		super.onDestroy();
		Log.d(LOG_TAG, "Servizio distrutto!");
	}

	private void loadSharedPreferences(){
		SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferencesName.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		minimumVolume = sharedPreferences.getInt(KEY_MINIMUM_VOLUME, DEFAULT_MINIMUM_VOLUME);
		maximumVolume = sharedPreferences.getInt(KEY_MAXIMUM_VOLUME, DEFAULT_MAXIMUM_VOLUME);
		adjustingVolume = sharedPreferences.getBoolean(KEY_ADJUSTING_VOLUME, DEFAULT_ADJUSTING_VOLUME);
		Log.d(LOG_TAG, "Shared preferences caricate!");
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		loadSharedPreferences();		
	}
	
	/** Questo metodo permette, di registrate un listener interessato a conoscere 
	 *	l'intensità di rumore ambientale 
	 * @param listener listener da registrare
	 */
	synchronized public void setVolumeServiceListener(VolumeServiceListener listener){
		if (listener != null){
			this.listener = new WeakReference<VolumeServiceListener>(listener);
			Log.d(LOG_TAG, "Listener registrato!");
		}else{
			Log.d(LOG_TAG, "E' stato passato un listener null!");
		}
		
	}
	/** Restituisce il listener registrato. Se non è stato registrato alcun listener,
	 * o se il listener registrato è stato deallocato, restituisce true.
	 */
	synchronized public VolumeServiceListener getVolumeServiceListener(){
		if (listener != null)
			return listener.get();
		else
			return null;
	}
	
	/**
	 *  Metodo invocato non appena è disponibile il valore dell'intensità di rumore ambientale.
	 *  Nel caso in cui la shared preferences "adjusting volume" vale true, effettua la regolazione del volume
	 * @param loudnessIntensity intensità di rumore ambientale. Tale valore appartiene all'intervallo [0, 1]
	 */
	private void onNewLoudnessIntesitySample(double loudnessIntensity){
		Log.d(LOG_TAG, "Loudness intensity: " + Double.toString(loudnessIntensity));
		if (adjustingVolume){
			VolumeManager volumeManager = new VolumeManager(this);
			double newVolume = minimumVolume + (maximumVolume - minimumVolume)*loudnessIntensity;
			Log.d(LOG_TAG, "Volume impostato: " + Math.round(newVolume));
			volumeManager.setRingtoneVolume((int)Math.round(newVolume));
		}
		
		VolumeServiceListener listener = getVolumeServiceListener();
		if (listener != null)
			listener.notifyLoudnessIntensity(loudnessIntensity);

		stopSelf();
	}
	
	private void onLoudnessMeterError(IOException e){
		Log.d(LOG_TAG, "Impossibile avviare l'acquisizione del rumore!", e);
		VolumeServiceListener listener = getVolumeServiceListener();
		if (listener != null){
			Log.d(LOG_TAG, "Errore notificato!");
			listener.notifyError();
			return;
		}
		Log.d(LOG_TAG, "Errore non notificato!");
		stopSelf();
	}
	
	private class LoudnessMeterThread implements Runnable {
		private ILoudnessMeter loudnessMeter;
		private boolean started = false;
		private Thread thread;
		
		public void start() {
			if (!started){
				Log.d(LOG_TAG, "Avvio dell'acquisizione del rumore!");
				loudnessMeter = new LoudnessMeter();
				try {
					started = true;
					loudnessMeter.start();
					thread = new Thread(this, this.getClass().getSimpleName());
					thread.start();
					Log.d(LOG_TAG, "Acquisizione del rumore avviata!");
				} catch (IOException e) {
					Log.d(LOG_TAG, "Avvio dell'acquisizione del rumore fallita!");
					onLoudnessMeterError(e);
				}
			}
		}
		
	
		@Override
		public void run() {
			loudnessMeter.getLoudnessIntensity();	//Per inizializzazione
			try {
				Thread.sleep(LOUDNESS_REGISTRATION_PERIOD);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			onNewLoudnessIntesitySample(loudnessMeter.getLoudnessIntensity());
			loudnessMeter.stop();
			loudnessMeter = null;
			thread = null;
			started = false;
		}
	}
	

	
	public class VolumeServiceBinder extends Binder {
		public VolumeService getService(){
			return VolumeService.this;
		}
	}
	
	public interface VolumeServiceListener {
		void notifyLoudnessIntensity(double loudnessIntensity);
		void notifyError();
	}



}
