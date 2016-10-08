package com.donnarumma.ciro.volumecontrol.utils;

import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


public class SoundMeter implements ILoudnessMeter {
	
	private final static String LOG_TAG = SoundMeter.class.getSimpleName();
	
	AudioRecord audioRecord;
	private int sampleRate;
	private int bufferSize;

	/**
	 * Instanzia un nuovo {@link #SoundMeter()}
	 */
	public SoundMeter() {
		audioRecord = null;
		sampleRate = 8000;
	}
	
	/*
	 * @see com.donnarumma.ciro.volumecontrol.utils.ILoudnessMeter#start()
	 */
	@Override
	public void start() throws IOException {
		if (audioRecord == null){
			bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
			switch (bufferSize) {
			case AudioRecord.ERROR:
				//I parametri di registrazione non supportati dall'hardware
				Log.e(LOG_TAG, "I parametri di registrazione non supportati dall'hardware!");
				throw new IOException("I parametri di registrazione non supportati dall'hardware!");
			case AudioRecord.ERROR_BAD_VALUE:
				//Impossibile interrogare l'hardware per le proprietà dell'input
				Log.e(LOG_TAG, "Impossibile interrogare l'hardware per le proprietà dell'input!");
				throw new IOException("Impossibile interrogare l'hardware per le proprietà dell'input!");
			default:
				try {
					audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
					audioRecord.startRecording();
					Log.d(LOG_TAG, "La misurazione del rumore ambientale è stata avviata!");
				} catch (Exception e){
					Log.e(LOG_TAG, "Impossibile avviare la misurazione del rumore ambientale");
					if (audioRecord != null){
						audioRecord.release();
						audioRecord = null;
					}
					e.printStackTrace();
				}
				break;
			}
		}
	}

	/*
	 * @see com.donnarumma.ciro.volumecontrol.utils.ILoudnessMeter#stop()
	 */
	@Override
	public void stop() {
		if (audioRecord != null){
			audioRecord.stop();
			audioRecord.release();
			audioRecord = null;
			Log.d(LOG_TAG, "La misurazione del rumore ambientale è terminata!");
		}else{
			Log.d(LOG_TAG, "Il metodo stop non ha avuto effetto poichè la misurazione del rumore ambientale non era in corso!");
		}
		
	}

	/*
	 * @see com.donnarumma.ciro.volumecontrol.utils.ILoudnessMeter#getLoudnessIntensity()
	 */
	@Override
	public double getLoudnessIntensity() throws AssertionError {
		try {
			final double MAX_VALUE = 32768; //PCM16 - The short has full range from [-32768, 32767]
			short[] buffer = new short[bufferSize];
			int bufferReadResult = 1;
			if (audioRecord != null) {
				bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
				double intensity = 0;
				for (int i = 0; i < bufferReadResult; i++) {
					//intensity += buffer[i];
					if (intensity<Math.abs(buffer[i]));
						intensity = Math.abs(buffer[i]);
				}
				//intensity = Math.abs((intensity / bufferReadResult))/MAX_VALUE;
				return intensity;
			}else{
				Log.e(LOG_TAG, "La misurazione del rumore ambientale non è iin corso!");
				throw new AssertionError("Loudness meter non è stato avviato!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/*
	 * @see com.donnarumma.ciro.volumecontrol.utils.ILoudnessMeter#isStarted()
	 */
	@Override
	public boolean isStarted() {
		return audioRecord != null;
	}

}
