package com.donnarumma.ciro.volumecontrol.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;


/* Questa classe è realizzata con pattern Singleton poichè non bisogna perdere il riferimento all'oggetto Camera
 * una volta che è stato creato
 */
@SuppressWarnings("deprecation")
public class FlashBlinker{ 
	private static final String LOG_TAG = FlashBlinker.class.getSimpleName();
	private static final long DEFAULT_DELAY = 500;
	private static FlashBlinker flashBlinker;			//Singleton
	
	private Camera camera;
	private boolean running;
	private volatile long delay;
	private Thread thread;
	
	private FlashBlinker() {
		camera = null;
		running = false;
		delay = DEFAULT_DELAY;
		thread = null;
	}
	
	public static FlashBlinker getFlashBlinker () {
		if (flashBlinker == null)
			flashBlinker = new FlashBlinker();
		return flashBlinker;
	}
		
	public void start (Context context){
		if (thread == null){
			if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
				if (camera == null)
					camera = Camera.open();
				running = true;
				thread = new Thread(new Runnable() {
					@Override
					public void run() {
						blinking();					
					}
				});
				thread.start();
			}else{
				Log.d(LOG_TAG, "Il dispositivo non possiede il flash!");
			}
		}
	}
	
	public void stop() {
		running = false;
	}
	
	public void setDelay(long delay) {
		this.delay = delay;
	}
	
	public long getDelay() {
		return delay;
	}
	
	private void blinking(){
		Log.d(LOG_TAG, "Led Blinker avviato!");
		Parameters flashOn = camera.getParameters();
		Parameters flashOff = camera.getParameters();
		flashOn.setFlashMode(Parameters.FLASH_MODE_TORCH);
		flashOff.setFlashMode(Parameters.FLASH_MODE_AUTO);
		while (running){
			
			camera.setParameters(flashOn);
			camera.startPreview();
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			camera.setParameters(flashOff);
			camera.startPreview();
			
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		camera.stopPreview();
		camera.release();
		camera = null;
		thread = null;
		Log.d(LOG_TAG, "Led Blinker fermato!");
	}
	


}
