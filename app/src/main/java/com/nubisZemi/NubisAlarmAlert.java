package com.nubisZemi;

import java.util.Timer;
import java.util.TimerTask;

import com.nubisZemi.R;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


public class NubisAlarmAlert extends Activity implements OnClickListener {

		private MediaPlayer mediaPlayer;
		private Vibrator vibrator;
		private boolean alarmActive;
        private NubisAlarm alarm;
	
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			try {
			super.onCreate(savedInstanceState);
			final Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

			setContentView(R.layout.activity_nubis_alarm_alert);

			
			Button sendBtn = (Button) findViewById(R.id.serverButton);
	        sendBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("stopAlarmButton")); 
			sendBtn.setOnClickListener(stopOnClickListener);

			
			
			Bundle bundle = this.getIntent().getExtras();
			if (bundle != null){
			  this.alarm = (NubisAlarm) bundle.getSerializable("alarm");
			}
			TextView alarmView = (TextView) findViewById(R.id.alarmView); 
			if (alarm != null && alarm.isCortisolAlarm()){ // cortisol
			  alarmView.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("cortisolAlarmMessage")); //
			}
			else {
			  alarmView.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("questionsAlarmMessage")); //"Time to answer your questions!"
			}
			

			
			
			this.setTitle("Nubis Zemi");

			TelephonyManager telephonyManager = (TelephonyManager) this
					.getSystemService(Context.TELEPHONY_SERVICE);

			PhoneStateListener phoneStateListener = new PhoneStateListener() {
				@Override
				public void onCallStateChanged(int state, String incomingNumber) {
					switch (state) {
					case TelephonyManager.CALL_STATE_RINGING:
						//Log.d(getClass().getSimpleName(), "Incoming call: "+ incomingNumber);
						try {
							mediaPlayer.pause();
						} catch (IllegalStateException e) {

						}
						break;
					case TelephonyManager.CALL_STATE_IDLE:
					//	Log.d(getClass().getSimpleName(), "Call State Idle");
						try {
							if (mediaPlayer != null){
							  mediaPlayer.start();
							}
						} catch (IllegalStateException e) {

						}
						break;
					}
					super.onCallStateChanged(state, incomingNumber);
				}
			};

			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE);

			// Toast.makeText(this, answerString, Toast.LENGTH_LONG).show();
              if (alarm != null){
			    startAlarm();
              }
			}
			catch(Exception e){
				e.printStackTrace();
		        ((NubisApplication)this.getApplicationContext()).log += e.getMessage();
			}
		}
		
		
		public void stopAlarm(){
			try {
				if (alarmActive){
					
						try {
							if (vibrator != null)
								vibrator.cancel();
						} catch (Exception e) {
						
						}
						try {
							mediaPlayer.stop();			
							alarmActive = false;
						} catch (Exception e) {
						
						}
						try {
							mediaPlayer.release();
						} catch (Exception e) {
				   }
	   			   alarmActive = false;
				}
			} catch (Exception e) {
				
			}
		}
		
		private void startMainActivity(){
			    Intent intent2 = new Intent(this, NubisMain.class);
	    	    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			    startActivity(intent2);
				

		}
		
		Button.OnClickListener stopOnClickListener = 
				new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					stopAlarm();		 
					startMainActivity();
				}
			};

			
			Button.OnClickListener startOnClickListener = 
					new Button.OnClickListener() {
					@Override
					public void onClick(View v) {
						startMainActivity();
					}
				};
			
			
			
			
		@Override
		protected void onResume() {
			super.onResume();
			alarmActive = true;
		}

		private void addNotification(){
			 // Create the base notification (the R.drawable is a reference fo a png file)
			  Notification notification = new Notification(R.drawable.icon, "Nubis Zemi", System.currentTimeMillis());

			  // The action you want to perform on click
			  Intent intent = new Intent(this, NubisMain.class);

			  // Holds the intent in waiting until it’s ready to be used
			  PendingIntent pi = PendingIntent.getActivity(this, 1, intent, 0);

			  //CHECK WHICH MESSAGE SHOULD BE SHOWN!
			  int type = 2; //questions
			  if (alarm != null && alarm.isCortisolAlarm()){ // cortisol
			    type = 1;
			  }
			 //THIS MIGHT BE FRIDAY: CHECK IF NEEDED! OTHERWISE CONVERT STATUS TO QUESTION INSTEAD OF CORTISOL
			  if (((NubisApplication)getApplicationContext()).settings.numberOfCortisol >= ((NubisApplication)getApplicationContext()).settings.cortisolTotalMeasures){
				  type = 2;
			  }
			  if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_CORTISOL_ALARM){ //nothing going on right now!
                  //evening... missed??
				  if (((NubisApplication)getApplicationContext()).lastMainAlarmIndex != -1){
					   if (((NubisApplication)getApplicationContext()).lastMainAlarmIndex - 4 > 0){
						   if (!(((NubisApplication)getApplicationContext()).alarms.get(((NubisApplication)getApplicationContext()).lastMainAlarmIndex - 4).alarm.answered == 2)){
  						     type = 2;
						   }
					   }
				   }
			  }
			  
			  // Set the latest event info
			  if (type == 1){ // cortisol
  			    notification.setLatestEventInfo(this, "Nubis Zemi", ((NubisApplication)getApplicationContext()).settings.texts.getText("cortisolAlarmMessage"), pi); //
			  }
			  else {
  			    notification.setLatestEventInfo(this, "Nubis Zemi", ((NubisApplication)getApplicationContext()).settings.texts.getText("questionsAlarmMessage"), pi); //"Time to answer your questions!"
			  }
			  // Get an instance of the notification manager
			  NotificationManager noteManager = (NotificationManager)
			      getSystemService(Context.NOTIFICATION_SERVICE);

			  // Post to the system bar
			  noteManager.notify(1, notification);
		}
		
		
		private void startAlarm() {

			if (true){ //alarm.getAlarmTonePath() != "") {
				mediaPlayer = new MediaPlayer();
				if (true){ //alarm.getVibrate()) {
					vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
					long[] pattern = { 1000, 200, 200, 200 };
					vibrator.vibrate(pattern, 0);
				}
				try {
				//	RingtoneManager mRing = new RingtoneManager(this);
				//  int mNumberOfRingtones = mRing.getCursor().getCount();
				//  Uri mRingToneUri = mRing.getRingtoneUri((int) (Math.random() * mNumberOfRingtones));
				  				    
  			        Uri notification_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					mediaPlayer.setVolume(1.0f, 1.0f);
					mediaPlayer.setDataSource(this, notification_uri); //dUri.parse(RingtoneManager.TYPE_NOTIFICATION)); //alarm.getAlarmTonePath()));
					mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
					mediaPlayer.setLooping(true);
					mediaPlayer.prepare();
					mediaPlayer.start();

				} catch (Exception e) {
					mediaPlayer.release();
					alarmActive = false;
				}
				//ADD NOTIFICATION
				addNotification();
				
				
				
				int delay = ((NubisApplication)getApplicationContext()).settings.getAlarmTime();
				
				//int delay = 10000; // delay for 5 sec. 
				Timer timer = new Timer(); 
				timer.schedule(new TimerTask() 
				    { 
				        public void run() 
				        { 
				        	if (alarmActive){
				              stopAlarm();  // display main screen
   							  startMainActivity();
				            }
				        } 
				    }, delay); 				
				
				
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.app.Activity#onBackPressed()
		 */
		@Override
		public void onBackPressed() {
			if (!alarmActive)
				super.onBackPressed();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.app.Activity#onPause()
		 */
		@Override
		protected void onPause() {
			super.onPause();
			NubisAlarmAlertWakeLock.releaseCpuLock(); //lockOff(this);
		}

		@Override
		protected void onDestroy() {
			stopAlarm();
			super.onDestroy();
		}

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

}
