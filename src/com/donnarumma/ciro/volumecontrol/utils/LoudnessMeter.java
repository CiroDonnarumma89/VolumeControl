package com.donnarumma.ciro.volumecontrol.utils;

import java.io.IOException;
import android.media.MediaRecorder;
import android.util.Log;




/**
 * The Class LoudnessMeter.
 */
public class LoudnessMeter implements ILoudnessMeter {
	
	private final static String LOG_TAG = LoudnessMeter.class.getSimpleName();
	
	private MediaRecorder mediaRecorder;

	public LoudnessMeter() {
		mediaRecorder = null;
	}

	/*
	 * @see com.donnarumma.ciro.volumecontrol.utils.ILoudnessMeter#start()
	 */
	@Override
	public void start() throws IOException {
		if (mediaRecorder == null) {
			mediaRecorder = new MediaRecorder();
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mediaRecorder.setOutputFile("/dev/null");
			
			//Preparazione del media recorder
			try {
				mediaRecorder.prepare();
			} catch (IllegalStateException e) {
				Log.e(LOG_TAG, "Eccezione IllegalStateException lanciata durante la preparazione del media recorder!", e);
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(LOG_TAG, "Ecceziona IOException lanciata durante la preparazione del media recorder");
				e.printStackTrace();
			}
			
			//Avvio del media recorder
			try {
				mediaRecorder.start();
			} catch (IllegalStateException e){
				Log.e(LOG_TAG, "La misurazione del rumore ambientale non è stata avviata poichè non è possibile acquisire il microfono!");
				mediaRecorder = null;
				throw new IOException("Impossibile acquisire il microfono!");
			}			
		}else{
			Log.d(LOG_TAG, "Il metodo start non ha avuto effetto poichè la misurazione del rumore ambientale è già in corso!");
		}
	}
	

	/*
	 * @see com.donnarumma.ciro.volumecontrol.utils.ILoudnessMeter#stop()
	 */
	@Override
	public void stop() {
		if (mediaRecorder != null) {
			mediaRecorder.stop();
			mediaRecorder.reset();
			mediaRecorder.release();
			mediaRecorder = null;
			Log.d(LOG_TAG, "La misurazione del rumore ambientale è terminata!");
		} else {
			Log.d(LOG_TAG, "Il metodo stop non ha avuto effetto poichè la misurazione del rumore ambientale non era in corso!");
		}
	}
	

	/*
	 * @see com.donnarumma.ciro.volumecontrol.utils.ILoudnessMeter#getLoudnessIntensity()
	 */
	@Override
	public double getLoudnessIntensity() throws AssertionError {
		final double MAX_VALUE = 32767; 
		if (mediaRecorder != null)
			return mediaRecorder.getMaxAmplitude()/MAX_VALUE;
		else {
			Log.e(LOG_TAG, "La misurazione del rumore ambientale non è iin corso!");
			throw new AssertionError("Loudness meter non è stato avviato!");
		}
	}
	

	/*
	 * @see com.donnarumma.ciro.volumecontrol.utils.ILoudnessMeter#isStarted()
	 */
	@Override
	public boolean isStarted() {
		return mediaRecorder != null;
	}
	


}
