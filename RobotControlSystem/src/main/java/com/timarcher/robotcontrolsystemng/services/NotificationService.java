package com.timarcher.robotcontrolsystemng.services;

import com.timarcher.robotcontrolsystemng.MainActivity;
import com.timarcher.robotcontrolsystemng.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

/**
 * A service used to assist with managing notifications
 * sent to the Android OS. It basically provides convenience methods around the
 * Notification Manager.
 * 
 */
public class NotificationService {
	/**The notification service. */
	protected NotificationManager notificationManager;
	/** The context for the notifications. Usually another activity or service. */
	protected Context context;
	
	/**
	 * Constructor.
	 * 
	 */
	public NotificationService(Context context) {
		this.context = context;
		notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
	}

	/**
	 * Put a notification in the status bar for the robot control system.
	 * 
	 * @param title
	 * @param description
	 */
	public void showNotification(String title, String description) {
		this.showNotification(title, description, 1);
	}
	
	/**
	 * Put a notification in the status bar for the robot control system.
	 * 
	 * @param title
	 * @param description
	 * @param notificationId
	 */
	public void showNotification(String title, String description, int notificationId) {		
		if (notificationManager != null) {
			/*OLd for android v11
		    Notification not = new Notification(R.drawable.ic_launcher, "RCS Running", System.currentTimeMillis());
		    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), Notification.FLAG_ONGOING_EVENT);        
		    not.flags = Notification.FLAG_ONGOING_EVENT;
		    not.setLatestEventInfo(this, title, description, contentIntent);
		    notificationManager.notify(1, not);
		    */
			
		    //New for android v16+
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), Notification.FLAG_ONGOING_EVENT);
		    Notification.Builder builder = new Notification.Builder(context);
		    Resources res = context.getResources();
		    builder.setContentIntent(contentIntent)
		                .setSmallIcon(R.mipmap.ic_launcher)
		                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
		                .setTicker(title + " - Initialized")
		                .setWhen(System.currentTimeMillis())
		                .setAutoCancel(false)
		                .setContentTitle(title)
		                .setContentText(description);
		    Notification not = builder.build();
		    
		    notificationManager.notify(notificationId, not);
		}
	}
	
	/**
	 * Cancels the notification at the specified notification ID.
	 * 
	 */
	public void cancelNotification (int notificationId) {
		notificationManager.cancel(notificationId);
	}
}
