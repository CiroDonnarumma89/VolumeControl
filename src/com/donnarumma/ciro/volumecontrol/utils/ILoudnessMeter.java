package com.donnarumma.ciro.volumecontrol.utils;
import java.io.IOException;


/**
 * Definizione dell'interfaccia di un oggetto in grado di misurare il livello di rumore ambientale.
 */
public interface ILoudnessMeter {
	
	/**
	 * Avvia la misurazione del rumore ambientale nel caso in cui non sia già in corso.
	 * In caso contrario non fa nulla.
	 *
	 * @throws IOException Sollevata se non si riesce ad acquisire il microfono
	 */
	public void start() throws IOException;
	
	/**
	 * Ferma la misurazione del rumore ambientale se è in corso.
	 * In caso contrario non fa nulla.
	 */
	public void stop();
	
	/**
	 * Ritorna l'intensità di rumore ambientale.
	 * E' possibile invocare questo metodo solo se {@link #isStarted()} ritorna <b>true</b>.
	 *
	 * @return intensità di rumore ambientale
	 * @throws AssertionError Sollevato quando {@link #getLoudnessIntensity()} è invocato senza aver prima avviato la misurazione con {@link #start()}
	 */
	public double getLoudnessIntensity() throws AssertionError;
	
	/**
	 * Controlla se la misurazione del rumore è in corso.
	 *
	 * @return true, se la misarazione è in corso
	 */
	public boolean isStarted();
	
}
