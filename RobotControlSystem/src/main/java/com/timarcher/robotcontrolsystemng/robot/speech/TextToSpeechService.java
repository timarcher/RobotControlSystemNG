package com.timarcher.robotcontrolsystemng.robot.speech;

import java.util.Locale;
import java.util.UUID;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

/**
 * Text To Speech Service
 * To change the voice on your phone, go to:
 * Settings->Language & Input->Text to Speech Output
 * Select a voice and then press the Listen to an Example option to hear it.
 * 
 * I found the stock android voices dont sound as good as 3rd party ones.
 * As of this demo, I have installed the Ivona voices. Go to the play store, search for Ivona and 
 * install their voices. They are currently free. You need to instal the engine, and then it will take you to
 * their website to choose voices to install. I installed Nicole (Australian English) and Gwyneth (Welsh English) 
 * 
 * To change voices to Ivona after install, go to:
 * Settings->Language & Input->Text to Speech Output
 * Select the Ivona engine, and then in its settings choose the voice that you would like to use.
 * 
 */
public class TextToSpeechService implements TextToSpeech.OnInitListener {
	
	private static final String LOGTAG = "TextToSpeechService";
	protected TextToSpeech _tts;
	protected boolean _isInitialized = false;
	
	/**
	 * Constructor
	 */
	public TextToSpeechService(Context context) {
        _tts = new TextToSpeech(context, this);
	}
	
	/**
	 * Overridden method to initialize the text to speech engine
	 * 
	 * @param status
	 */
    @Override
    public void onInit(int status) {
    	Log.i(LOGTAG, "onInit Called. Status:" + status);

		// Now that the TTS engine is ready, we enable the button
        if (status == TextToSpeech.SUCCESS) {
        	int result = _tts.setLanguage(Locale.US);
        	//int result = tts.setLanguage(Locale.UK);        	
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(LOGTAG, "This Language is not supported");
            } 
            else {
            	_isInitialized = true;
            }
            
        }
        else {
        	Log.e(LOGTAG, "TTS Initilization Failed");
        }
        
        Log.i(LOGTAG, "TTS onInit Called. Status: " + status);
    }
    
    /**
     * Shutdown TTS
     * 
     */
    public void shutdownTextToSpeech() {
        if (_tts != null) {
        	_tts.stop();
        	_tts.shutdown();
        }
    }
    
    /**
     * Set the pitch to use when speaking.
     * By default the value is 1.0 You can set lower values than 1.0 to decrease pitch 
     * level or greater values for increase pitch level.
     * 
     */
    public void setPitch(float pitch) {
        if (_tts != null) {
        	_tts.setPitch(pitch);
        }
    }

    /**
     * Set the speed to use when speaking.
     * The speed rate can be set using setSpeechRate(). This also will take default 
     * of 1.0 value. You can double the speed rate by setting 2.0 or make half the speed level by setting 0.5
     * 
     */
    public void setSpeechRate(float rate) {
        if (_tts != null) {
        	_tts.setSpeechRate(rate);
        }
    }  
    
    /**
     * Method to say the text provided.
     * @param text
     */
    public void sayIt (String text) {
        String utteranceId = UUID.randomUUID().toString();
        _tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
    
    /**
     * Method to get whether the TTS engine has been initialized or not.
     */
    public boolean isInitialized () {
        return _isInitialized;
    }
    
}
