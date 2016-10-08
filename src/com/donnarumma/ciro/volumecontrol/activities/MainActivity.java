package com.donnarumma.ciro.volumecontrol.activities;

import java.lang.ref.WeakReference;

import com.donnarumma.ciro.volumecontrol.R;
import com.donnarumma.ciro.volumecontrol.services.VolumeService;
import com.donnarumma.ciro.volumecontrol.services.VolumeService.VolumeServiceListener;
import com.donnarumma.ciro.volumecontrol.utils.VolumeManager;
import com.donnarumma.ciro.volumecontrol.utils.VolumeServiceAlarmManager;

import static com.donnarumma.ciro.volumecontrol.utils.SharedPreferencesName.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


/**
 * Activity principale che permette di configurare l'applicazione.
 * Tale Activity permette di attivare e disattivare le singole funzionalità dell'applicazione, ovvero: 
 * controllo del volume in funzione del rumore ambientale; attivazione del flash in caso di chiamata in arrivo.
 * Per quanto riguarda la prima funzionalità è possibile impostare: i limiti entro il quale il volume può essere variato;
 * il periodo di attivazione del servizio di controllo del volume. 
 *
 * @author Ciro Donnarumma
 */
public class MainActivity extends Activity implements VolumeServiceListener, OnSeekBarChangeListener, OnCheckedChangeListener {
	
	/* Tag per il log */
	private static final String LOG_TAG = MainActivity.class.getSimpleName();
	
	/* Il volume service viene attivato periodicamente al fine di acquisire l'intensità di rumore ed effettuare la regolazione del volume.
	 * Queste costanti definiscono il periodo minimo e massimo di attivazione del volume service. Tale periodo viene espresso in secondi. 
	 */
	private static final int MINIMUM_TRIGGER_INTERVAL = 5;							
	private static final int MAXIMUX_TRIGGER_INTERVAL = 3600;
	
	/* Riferimenti alle View che compongono la GUI */
	private TextView progressLoudnessIntensity;
	private SeekBar seekMinimumVolume;
	private SeekBar seekMaximumVolume;
	private SeekBar seekTriggerInterval;
	private ToggleButton toggleFlahBlinking;
	private ToggleButton toggleVolumeControlStart;
	
	/* Shared preferences */
	private int minimumVolume;							//Livello di volume oltre il quale il volume service non può scendere
	private int maximumVolume;							//Livello di volume oltre il quale il volume service non può andare
	private int triggerInterval;						//Periodo temporale, espresso in secondi, fra due attivazioni successive del volume service
	private boolean flashBlinking;						//Vale true se si desidera che il flash lampeggi sulle chiamate in arrivo
	private boolean adjustingVolume;					//Vale true se si desidera che il volume service effettui la regolazione del volume
	
	/* Handler per la comunicazione con il main thread e relativi tipi di messaggi */
	private UpdateHandler updateHandler;
	private final static int UPDATE_LOUDNESS_WHAT = 1;	//Messaggio di notifica dell'intensità di rumore
	private final static int ERROR_WHAT = 2;			//Messaggio di notifica di un errore
	
	/* Oggetto toast utilizzato per mostrare piccole notifiche a scehrmo */
	private Toast toast;									
	
	/* Riferimento al volume service */
	private VolumeService volumeService;

	private boolean errorNotified = false;

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Caricamento shared preferences e preparazione delle views
		loadSharedPreferences();
		initializeViewReferences();
		initializeView();
		
		//Preparazione dell'oggetto toast
		toast = Toast.makeText(MainActivity.this, null, Toast.LENGTH_SHORT);
		
		//Creazione dell'Hadler per la comunicazione con il Main Thread
		updateHandler = new UpdateHandler(MainActivity.this);
		
		//Avvio del binding con il volume service
		bindService(new Intent(this, VolumeService.class), serviceConnection, BIND_AUTO_CREATE);
		
		Log.d(LOG_TAG, "Activity creata!");
	}

	/**
	 * @see android.app.Activity#onDestroy()
	 */
	protected void onDestroy() {
		//Unbindig del volume service
		unbindService(serviceConnection);
		Log.d(LOG_TAG, "Activity distrutta!");
		super.onDestroy();
	}
	

	/**
	 * Inizializza i riferimenti agli oggetti view
	 */
	private void initializeViewReferences() {
		progressLoudnessIntensity = (TextView)findViewById(R.id.progress_view_loudness_intensity);
		seekMinimumVolume = (SeekBar)findViewById(R.id.seekBar_minimum_volume);
		seekMaximumVolume = (SeekBar)findViewById(R.id.seekBar_maximum_volume);
		seekTriggerInterval = (SeekBar)findViewById(R.id.seekBar_trigger_interval);
		toggleFlahBlinking = (ToggleButton)findViewById(R.id.toggle_flash);
		toggleVolumeControlStart = (ToggleButton)findViewById(R.id.toggle_volume_control_start);
	}
	
	
	/**
	 * Inizializza gli oggetti view
	 */
	private void initializeView() {
		//Configurazione delle seekbars
		VolumeManager volumeManager = new VolumeManager(MainActivity.this);
		seekMinimumVolume.setMax(volumeManager.getMaxRingtoneVolume());
		seekMaximumVolume.setMax(volumeManager.getMaxRingtoneVolume());
		seekTriggerInterval.setMax((MAXIMUX_TRIGGER_INTERVAL-MINIMUM_TRIGGER_INTERVAL)/MINIMUM_TRIGGER_INTERVAL);
		seekMinimumVolume.setProgress(minimumVolume);
		seekMaximumVolume.setProgress(maximumVolume);
		seekTriggerInterval.setProgress((triggerInterval-MINIMUM_TRIGGER_INTERVAL)/MINIMUM_TRIGGER_INTERVAL);
		
		//Configurazione dei toggle button
		toggleFlahBlinking.setChecked(flashBlinking);
		toggleVolumeControlStart.setChecked(adjustingVolume);
		
		//Configurazione della progress louness intensity
		setClipDrawableValue(progressLoudnessIntensity, 0);

		//Assegnazione del listner alle views
		seekMinimumVolume.setOnSeekBarChangeListener(this);
		seekMaximumVolume.setOnSeekBarChangeListener(this);
		seekTriggerInterval.setOnSeekBarChangeListener(this);
		toggleFlahBlinking.setOnCheckedChangeListener(this);
		toggleVolumeControlStart.setOnCheckedChangeListener(this);
		
	}
	

	/**
	 * Carica le shared preferences
	 */
	private void loadSharedPreferences() {
		//Caricamento delle shared preferences
		SharedPreferences mySharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		minimumVolume = mySharedPreferences.getInt(KEY_MINIMUM_VOLUME, DEFAULT_MINIMUM_VOLUME);
		maximumVolume = mySharedPreferences.getInt(KEY_MAXIMUM_VOLUME, DEFAULT_MAXIMUM_VOLUME);
		triggerInterval = mySharedPreferences.getInt(KEY_TRIGGER_INTERVAL, DEFAULT_TRIGGER_INTERVAL);
		flashBlinking = mySharedPreferences.getBoolean(KEY_FLASH_BLINKING, DEFAULT_FLASH_BLINKING);
		adjustingVolume = mySharedPreferences.getBoolean(KEY_ADJUSTING_VOLUME, DEFAULT_ADJUSTING_VOLUME);
		
		//Verifica dell'integrità delle shared preferences
		boolean modified = false;
		if (minimumVolume < 0){
			minimumVolume = DEFAULT_MINIMUM_VOLUME;
			modified = true;
		}
		if (maximumVolume < 0){
			maximumVolume = DEFAULT_MAXIMUM_VOLUME;
			modified = true;
		}
		if (triggerInterval < MINIMUM_TRIGGER_INTERVAL || triggerInterval > MAXIMUX_TRIGGER_INTERVAL){
			triggerInterval = (MAXIMUX_TRIGGER_INTERVAL-MINIMUM_TRIGGER_INTERVAL)/2;
			modified = true;
		}
		if (modified)
			saveSharedPreferences();
		
		Log.i(LOG_TAG, "SharedPreferences caricate!");
	}
		
	/**
	 * Salva le shared preferences
	 */
	private void saveSharedPreferences() {
		SharedPreferences mySharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putInt(KEY_MINIMUM_VOLUME, minimumVolume);
		editor.putInt(KEY_MAXIMUM_VOLUME, maximumVolume);
		editor.putInt(KEY_TRIGGER_INTERVAL, triggerInterval);
		editor.putBoolean(KEY_FLASH_BLINKING, flashBlinking);
		editor.putBoolean(KEY_ADJUSTING_VOLUME, adjustingVolume);
		editor.commit();
		Log.i(LOG_TAG, "SharedPreferences salvate!");
	}
		
	/**
	 * Imposta il livello di progresso di una text view avente un clip drawable come backgroung.
	 *
	 * @param textView la text view su cui impostare il nuovo livello
	 * @param value il valore in percentuale da impostare
	 * @throws AssertionError Errore lanciato se la text view passata non possiede un clip drawable come backgrround, oppure
	 * 						  se il parametro value non appartiene all'intervallo [0,100].
	 */
	private void setClipDrawableValue(final TextView textView, final int value) throws AssertionError {
		Drawable background = textView.getBackground();
		if (background instanceof ClipDrawable){
			if (value >= 0 && value <= 100){
				textView.setText("" + value + " %");
				((ClipDrawable)background).setLevel(value*100);
			}else{
				throw new AssertionError("Il valore deve essere compreo fra 0 e 100");
			}
		}else
			throw new AssertionError("La textView deve avere un clip draw come background");
	}
	
	/**
	 * Handler utilizzato per gestire la comunicazione con il Main Thread.
	 * E' in grado di gestire due tipi di messaggi:
	 * 1)messaggio di tipo UPDATE_LOUDNESS_WHAT, utilizzato per comunicare al Main Thread un nuovo valore per l'intensità di rumore
	 * 2)messaggio di tipo ERROR_WHAT, utilizzato per comunicare al Main Thread che il volume service non è stato in grado di acquisire il microfono.
	 * Nel caso in cui viene ricevuto un messaggio ERROR_WHAT, viene mostrata una Alert Dialog e l'applicazione viene terminata.
	 */
	private static class UpdateHandler extends Handler {
		
		private WeakReference<MainActivity> mainActivityRef;
		
		public UpdateHandler(MainActivity mainActivity) {
			this.mainActivityRef = new WeakReference<MainActivity>(mainActivity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			final MainActivity mainActivity = mainActivityRef.get();
			if (mainActivity != null)
				if (msg.what == UPDATE_LOUDNESS_WHAT){
					//E' stato ricevuto un messaggio di notifica dell'intensità di rumore
					mainActivity.setClipDrawableValue(mainActivity.progressLoudnessIntensity, msg.arg1);
				}else if (msg.what == ERROR_WHAT){
					//E' stato ricevuto un messaggio di notifica di un errore.
					if (!mainActivity.errorNotified){
						Resources resources = mainActivity.getResources();
						mainActivity.errorNotified = true;
						new AlertDialog.Builder(mainActivity)
						.setTitle(resources.getString(R.string.error))
						.setMessage(resources.getString(R.string.unable_acquire_microphone))
						.setCancelable(false)
						.setPositiveButton(resources.getString(R.string.positive_button), new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						}).show();
					}
				}
		}
	
	}
		
	/** 
	 * Callback invocata al verificarsi dell'evento Progress Changed scatenabile sulle seekbars.
	 * Garantisce che sia sempre verificata l'asserzione minimumVolume <= maximumVolume e mostra a video
	 * un toast che indica lo stato corrente della seekbar sulla quale è stato scatenato l'evento
	 * 
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar, int, boolean)
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (seekBar == seekMinimumVolume){
			minimumVolume = progress;
			if (minimumVolume > maximumVolume)
				seekMaximumVolume.setProgress(minimumVolume);
			toast.setText(getResources().getString(R.string.minimum_volume) + ": " + progress);
			
		}else if (seekBar == seekMaximumVolume){
			maximumVolume = progress;
			if (minimumVolume > maximumVolume)
				seekMinimumVolume.setProgress(maximumVolume);
			toast.setText(getResources().getString(R.string.maximum_volume) + ": " + progress);
			
		}else if (seekBar == seekTriggerInterval){
			triggerInterval = MINIMUM_TRIGGER_INTERVAL + progress*MINIMUM_TRIGGER_INTERVAL;
			toast.setText(getResources().getString(R.string.trigger_interval) + ": " + triggerInterval + " " + getResources().getString(R.string.seconds));		
		}
		
		if (fromUser)
			toast.show();
	}

	/**
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onStartTrackingTouch(android.widget.SeekBar)
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	/**
	 * Callback invocata al verificarsi dell'evento Stop Tracking Touch scatenabile sulle seekbars.
	 * Si limita a salvare le nuove impostazioni scelte dall'utente nelle shared preferences.
	 * Inoltre, nel caso in cui il controllo del volume è attivo e la modifica ha interessato
	 * il periodo di attivazione del volume service, configura il nuovo alarm.
	 * 
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onStopTrackingTouch(android.widget.SeekBar)
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		saveSharedPreferences();
		if (seekBar == seekTriggerInterval && adjustingVolume)
			VolumeServiceAlarmManager.registerAlarm(getApplicationContext(), triggerInterval*1000);
	}
	
	/**
	 * Callback invocata al verificarsi dell'evento Checked Changed scatenabile sulle seekbars.
	 * 
	 * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == toggleVolumeControlStart){
			if (isChecked)
				VolumeServiceAlarmManager.registerAlarm(getApplicationContext(), triggerInterval*1000);
			else
				VolumeServiceAlarmManager.clearAlarm(getApplicationContext());
			adjustingVolume = isChecked;
			
		}else if (buttonView == toggleFlahBlinking){
			flashBlinking = isChecked;
		}
		saveSharedPreferences();
	}

	/**
	 * Callback utilizzata dal volume service per notificare un nuovo valore per l'intensità di rumore
	 * @param loudnessIntensity Valore dell'intensità di rumore rilevata. Tale valore appartiene all'intervallo [0, 1]
	 * 
	 * @see com.donnarumma.ciro.volumecontrol.services.VolumeService.VolumeServiceListener#notifyError()
	 */
	@Override
	public void notifyLoudnessIntensity(double loudnessIntensity) {
		Log.d(LOG_TAG, "Intensità di rumore notificata: " + Math.round(loudnessIntensity));
		Message msg = updateHandler.obtainMessage(UPDATE_LOUDNESS_WHAT);
		msg.arg1 = ((int)Math.round(loudnessIntensity*100));
		updateHandler.sendMessage(msg);
	}

	/** 
	 * Callback utilizzata dal volume service per notificare un errore
	 * 
	 * @see com.donnarumma.ciro.volumecontrol.services.VolumeService.VolumeServiceListener#notifyError()
	 */
	@Override
	public void notifyError() {
		Log.d(LOG_TAG, "E' stato notificato un errore da parte del volume service");
		Message msg = updateHandler.obtainMessage(ERROR_WHAT);
		updateHandler.sendMessage(msg);
		
	}
	
	
	// Gestisce la connessione fra il volume service e l'activity
	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,IBinder service) {
			// Chiamata quando la connessione è stata attivata
			Log.d(LOG_TAG, "Servizio connesso!");
			volumeService = ((VolumeService.VolumeServiceBinder)service).getService();
			volumeService.setVolumeServiceListener(MainActivity.this);
			startService(new Intent(MainActivity.this, VolumeService.class));
		}
		
		public void onServiceDisconnected(ComponentName className) {
			// Chiamata quando il servizio si è disconnesso in modo inatteso
			volumeService = null;
		}
	};
	
	
	
}









	

