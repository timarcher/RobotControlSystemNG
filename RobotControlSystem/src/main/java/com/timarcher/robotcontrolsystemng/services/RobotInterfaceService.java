package com.timarcher.robotcontrolsystemng.services;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;
import com.timarcher.robotcontrolsystemng.robot.ioio.RobotIOIOInterface;
import com.timarcher.robotcontrolsystemng.robot.speech.TextToSpeechService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * The Main Robot Interface Service
 * This is the main service started when the Robot Control System launches.
 * It runs in the background and handles robot management functions including interacting with the
 * IOIO board, performing text to speech commands, and executing command sequences.
 * 
 */
public class RobotInterfaceService extends IOIOService {
	/** Logging tag. */
	protected static final String LOGTAG = "RobotInterfaceService";
	/** The robots text to speech service */
	protected TextToSpeechService tts;
	/** The custom IOIO interface class. */ 
	RobotIOIOInterface ioioInterface;
	/** Used for sending notifications to the android OS. */
	NotificationService notificationService;
	/** The service responsible for controlling the main drive motors. */
	MotorControlService motorControlService;	
	/** A reference to the binder for when clients connect to this service. */
	protected final IBinder robotInterfaceServiceBinder = new RobotInterfaceServiceBinder();
	
	
	protected long loopCount = 0;
	protected boolean isThreadStarted = false;
	public long getLoopCount() {
		return loopCount;
	}
	
	
	/**
	 * Executed when the service is started.
	 * 
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		if (!isThreadStarted) {
			initializeRobotServices();

			//
			//Initialize a thread to perform our service activities
			//
			Thread testThread = new Thread(new Runnable() {
		        public void run() {
		        	try {
    					Thread.sleep(5000);

		    			tts.sayIt("Robot Control System Initialized");			

		    			/*
		    			try {
		    				//G0 forward 6 feet at 50% speed
		    				//30 cm = 12 in
		    				motorControlService.moveDistance(120, 50);

		    				do {
		    					Thread.sleep(1000);
		    				} while (motorControlService.isRobotMoving());		    				
		    			} catch (Exception e) {				
		    			}
		    			*/
	    				/*
		    			for (int i = 0; i < 1; i++) {
			    			try {
				    			tts.sayIt("Turning 180 degrees");

			    				//turn 180 degrees at 50% speed
			    				motorControlService.turn(360, 50);
			    				do {
			    					Thread.sleep(1000);
			    				} while (motorControlService.isRobotMoving());		    				

			    				Thread.sleep(1500);

			    				tts.sayIt("Turning 180 degrees");			
			    				//turn 180 degrees at 50% speed
			    				motorControlService.turn(360, -50);
			    				do {
			    					Thread.sleep(1000);
			    				} while (motorControlService.isRobotMoving());		    				

			    				Thread.sleep(1500);
			    			} catch (Exception e) {				
			    			}
		    			}
		    			

		    			try {
			    			tts.sayIt("Going forward");			
		    				//Go forward 6 feet at 50% speed
		    				//30 cm = 12 in
		    				motorControlService.moveDistance(120, 50);

		    				do {
		    					Thread.sleep(1000);
		    				} while (motorControlService.isRobotMoving());		    				
		    			} catch (Exception e) {				
		    			}

		    			try {
			    			tts.sayIt("Going backward");			
		    				//Go forward 6 feet at 50% speed
		    				//30 cm = 12 in
		    				motorControlService.moveDistance(120, -50);

		    				do {
		    					Thread.sleep(1000);
		    				} while (motorControlService.isRobotMoving());		    				
		    			} catch (Exception e) {				
		    			}
		    			*/
		    			/*
		        		for (;;) {
		        			Thread.sleep(2000);
		        			loopCount ++;
		        			
		        			if (loopCount % 5 == 0) {
		        				notificationService.showNotification("Robot Control System", 
		        						"Thread Loop Count "+loopCount + "\n" + 
		        						"IOIO Connected: " + ioioInterface.isIOIOConnected() + "\n" + 
		        						"IOIO Loop Count: " + ioioInterface.getLoopCount());
		        				
		        				tts.sayIt("Status Updated");
		        			}
		        			//if (loopCount % 10 == 0) {
		        			//	notificationManager.cancel(1);
		        			//}
		        		}
		        		*/
		        	} catch (Exception e) {
		        		//Need to figure out how to handle
		        		Log.e("app", "Error calling service", e);		        		
		        	}
		        }
		    });
			testThread.setDaemon(true);
			testThread.start();			

			isThreadStarted = true;
			notificationService.showNotification("Robot Control System", "Starting up. Put other status stuff here.");
		}		
		
		return Service.START_STICKY;
	}

	/**
	 * Called when a client binds to the service.
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return robotInterfaceServiceBinder;
	}

	/**
	 * Inner class which allows bound clients to get an instance of
	 * this service.
	 * 
	 */
	public class RobotInterfaceServiceBinder extends Binder {
		public RobotInterfaceService getService() {
			return RobotInterfaceService.this;
		}
	}
	
	/**
	 * Overridden method from the IOIO Service. This is called
	 * when the service starts to initialize the Robot IOIO Interface.
	 * 
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		Log.d(LOGTAG, "createIOIOLooper called");
		//Duplicate call to initialize here to ensure the IOIO interface is
		//not null
		initializeRobotServices();
		return ioioInterface;
	}	
	
	/**
	 * Method to initialize all of the required/dependent services and objects 
	 * utilized by this robot service.
	 * 
	 */
	protected void initializeRobotServices() {
	    //
	    //Initialize text to speech
	    //	        
		Log.i(LOGTAG, "Initializing text to speech.");
		if (tts == null) {
			tts = new TextToSpeechService(this);
		}

		//
		//Notification manager used for putting notifications in the status bar. 
		//
		if (notificationService == null) {
			notificationService = new NotificationService(this);
		}
		
		//
		//Initialize the IOIO
		//
		if (ioioInterface == null) {
			ioioInterface = new RobotIOIOInterface();
		}

		//
		//Setup the main motor driver service
		//
		if (motorControlService == null) {
			motorControlService = new MotorControlService(ioioInterface);
		}
		
	}
}
