package com.nubisZemi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.nubisZemi.R;

import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NubisMain extends Activity {

	private TextView volumeInternet;
	private TextView messageTab;
	private TextView batteryPercent;
	private TextView infoMain;
	
	private Button mainBtn;
	
	private Boolean resendTimer = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((NubisApplication)getApplicationContext()).mainScreenActive = true;
	
     	final Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		setContentView(R.layout.activity_nubis_main);

		volumeInternet = (TextView) findViewById(R.id.volumeInternetView);
		messageTab = (TextView) findViewById(R.id.messageView);
		batteryPercent = (TextView) findViewById(R.id.batteryView);
		infoMain = (TextView) findViewById(R.id.infoMain); 

		
		IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryLevelReceiver, batteryLevelFilter);

		IntentFilter volumeLevelFilter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
		registerReceiver(volumeLevelReceiver, volumeLevelFilter);
		//INITIAL CHECK!
		AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
		((NubisApplication)getApplicationContext()).volume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
		
		IntentFilter networkAvailableFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(networkAvailableReceiver, networkAvailableFilter);
		showVolumeAndInternet();
		
		if (!((NubisApplication)getApplicationContext()).appRunning){  //FUNCTIONS THAT NEED TO RUN ONLY ONCE!!!
		
			SharedPreferences preferencesReader = getSharedPreferences("NubisSettings", Context.MODE_PRIVATE);
			// Read the shared preference value
			((NubisApplication)getApplicationContext()).communication.loadDelayedAnswers(preferencesReader);
			((NubisApplication)getApplicationContext()).loadSettings(preferencesReader, this);

			//set other things (server, messages, questions etc etc
			((NubisApplication)getApplicationContext()).appRunning = true;
		}
	
		setAlarms(((NubisApplication)getApplicationContext()).settings);
		
		
		//test buttons
		
		mainBtn = (Button) findViewById(R.id.mainButton);
		mainBtn.setOnClickListener(mainBtnOnClickListener);  
		mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("stressedOutButton"));
		
		/*
		questionBtn = (Button) findViewById(R.id.showQuestionsButton2);
		questionBtn.setOnClickListener(mQuestionsOnClickListener);  
		questionBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("questionButton"));*/
		//end test buttons
		
		
		
		
		setScreenBasedOnCurrentStatus();

		//check for new apk
		String newAPKUrl = ((NubisApplication)getApplicationContext()).settings.loadNewAPK; 
		if (newAPKUrl != null){
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(newAPKUrl)));
			((NubisApplication)getApplicationContext()).settings.loadNewAPK = null;
		}
		
		 ((NubisApplication)getApplicationContext()).communication.setHandler(		
		 new Handler() {
			 public void handleMessage(Message msg) {
				 setScreenBasedOnCurrentStatus();
			 } 
		 } );	
		

		
	}
	
	
	BroadcastReceiver volumeLevelReceiver = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context context, Intent intent){
	    	getVolumeLevel(context, intent);
	    }
	};		
	
	BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context context, Intent intent){
	    	getBatteryPercentage(context, intent);
	    }
	};		

	BroadcastReceiver networkAvailableReceiver = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context context, Intent intent){
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
   		    ((NubisApplication)getApplicationContext()).hasInternet = (cm.getActiveNetworkInfo() != null);
   		    showVolumeAndInternet();
      		setScreenBasedOnCurrentStatus();
	    }
	};		

	
	
	
	public void getVolumeLevel(Context context, Intent intent){
		try {
		  ((NubisApplication)getApplicationContext()).volume = (Integer)intent.getExtras().get("android.media.EXTRA_VOLUME_STREAM_VALUE");
		  showVolumeAndInternet();
		}
		catch(Exception e){
			
		}
	}
	public void showVolumeAndInternet(){
		try {
			volumeInternet.setBackgroundColor(getResources().getColor(R.color.NubisRed));
			volumeInternet.setVisibility(View.INVISIBLE);
			volumeInternet.setText("");
			if (((NubisApplication)getApplicationContext()).volume <= 3){
				volumeInternet.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("volumeMessage"));
			}
			if (!((NubisApplication)getApplicationContext()).hasInternet){
				if (volumeInternet.getText() != ""){
					volumeInternet.setText(volumeInternet.getText() + " / ");	
				}
			    volumeInternet.setText(volumeInternet.getText() + ((NubisApplication)getApplicationContext()).settings.texts.getText("noNetworkMessage"));
			}
			if (volumeInternet.getText() != ""){
				volumeInternet.setVisibility(View.VISIBLE);
			}
			
		}
		catch(Exception e){
				
		}
	}
	
	public void setAlarms(Settings settings){
		   // final Context ts = this;

		((NubisApplication)getApplicationContext()).alarmMgr = null;
	    ((NubisApplication)getApplicationContext()).alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		
		if (((NubisApplication)getApplicationContext()).alarms.size() == 0) { //only once!
			int index = 0;
			for (int i = 0; i < settings.getAlarmCount(); i++)
			{
			    NubisAlarm alarm = settings.getAlarm(i);

			    //here! set more alarms based on this one: window open, window close etc..
			    //CORTISOL
			    if (alarm.getAlarmType() == NubisAlarm.S_MORNING_WINDOW_1_CORTISOL_ALARM_1){
			    	//open window
			    	addNubisAlarm(index++, new NubisAlarm(alarm.getCopyCalendar(alarm.windowOpen), index, -1, NubisAlarm.S_MORNING_WINDOW_1_CORTISOL, 0, 0, false, alarm.active));
                    //main alarm			    	
				    alarm.parentid = index;
			    	addNubisAlarm(index++, alarm);
			    	//extra alarm
			    	addNubisAlarm(index++, new NubisAlarm(alarm.getCopyCalendar(alarm.window2ndReminder), index - 2, -1, NubisAlarm.S_MORNING_WINDOW_1_CORTISOL_ALARM_2, 0, 0, true, alarm.active));
			    	//close window
			    	addNubisAlarm(index++, new NubisAlarm(alarm.getCopyCalendar(alarm.windowClose), index - 3, -1, NubisAlarm.S_MORNING_WINDOW_1_CORTISOL_CLOSED, 0, 0, false, alarm.active));
			    }
                //CORTISON EVENING
			    else if (alarm.getAlarmType() == NubisAlarm.S_EVENING_WINDOW_3_CORTISOL_ALARM){
			    	//open window
			    	addNubisAlarm(index++, new NubisAlarm(alarm.getCopyCalendar(alarm.windowOpen), index, -1, NubisAlarm.S_EVENING_WINDOW_3_CORTISOL, 0, 0, false, alarm.active));
                    //main alarm			    	
				    alarm.parentid = index;
				    addNubisAlarm(index++, alarm);
			    	//close window
			    	addNubisAlarm(index++, new NubisAlarm(alarm.getCopyCalendar(alarm.windowClose), index - 2, -1, NubisAlarm.S_EVENING_WINDOW_3_CORTISOL_CLOSED, 0, 0, false, alarm.active));
			    }	
			    
			    
			    
			    //JUST QUESTIONS
			    else if (alarm.getAlarmType() == NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS_ALARM_1){
			    	//open window
			    	addNubisAlarm(index++, new NubisAlarm(alarm.getCopyCalendar(alarm.windowOpen), index, -1, NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS, 0, 0, false, alarm.active));
                    //main alarm			  
				    alarm.parentid = index;
				    addNubisAlarm(index++, alarm);
			    	//extra alarm
			    	addNubisAlarm(index++, new NubisAlarm(alarm.getCopyCalendar(alarm.window2ndReminder), index - 2, -1, NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS_ALARM_2, 0, 0, true, alarm.active));
			    	//close window
			    	addNubisAlarm(index++, new NubisAlarm(alarm.getCopyCalendar(alarm.windowClose), index - 3, -1, NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS_CLOSED, 0, 0, false, alarm.active));
			    }	    
			    //JUST QUESTIONS EVENING
			    else if (alarm.getAlarmType() == NubisAlarm.S_EVENING_WINDOW_3_QUESTIONS_ALARM){
			    	//open window
			    	addNubisAlarm(index++, new NubisAlarm(alarm.getCopyCalendar(alarm.windowOpen), index, -1, NubisAlarm.S_EVENING_WINDOW_3_QUESTIONS, 0, 0, false, alarm.active));
                    //main alarm			
				    alarm.parentid = index;
				    addNubisAlarm(index++, alarm);
			    	//close window
			    	addNubisAlarm(index++, new NubisAlarm(alarm.getCopyCalendar(alarm.windowClose), index - 2, -1, NubisAlarm.S_EVENING_WINDOW_3_QUESTIONS_CLOSED, 0, 0, false, alarm.active));
			    }	
			    else { //just add
				    alarm.parentid = index;
			    	addNubisAlarm(index++, alarm);
			    	
			    }
			}
			NubisAlarmReceiver test1 = new NubisAlarmReceiver();
			test1.setUpdateAlarm(this, ((NubisApplication)getApplicationContext()).alarmMgr, index++);

		}
	}
	
	public void addNubisAlarm(int i, NubisAlarm alarm){
	    NubisAlarmReceiver test1 = new NubisAlarmReceiver();
		test1.SetAlarm(this, ((NubisApplication)getApplicationContext()).alarmMgr, i, alarm, new Handler() {
					 public void handleMessage(Message msg) {
						 ((NubisApplication)getApplicationContext()).log += "\nhandlemessage";
                       } } );
		((NubisApplication)getApplicationContext()).alarms.add(test1);
	}
	

	/*
	public void addToTasks(){
		
		 // Create the base notification (the R.drawable is a reference fo a png file)
		  Notification notification = new Notification(R.drawable.icon, "Nubis Touchstone", System.currentTimeMillis());

		  // The action you want to perform on click
		  Intent intent = new Intent(this, NubisMain.class);

		  // Holds the intent in waiting until it’s ready to be used
		  PendingIntent pi = PendingIntent.getActivity(this, 1, intent, 0);

		  // Set the latest event info
		  notification.setLatestEventInfo(this, "Nubis Touchstone", "Time to take your cortisol!", pi);

		  // Get an instance of the notification manager
		  NotificationManager noteManager = (NotificationManager)
		      getSystemService(Context.NOTIFICATION_SERVICE);

		  // Post to the system bar
		  noteManager.notify(1, notification);
		
		
	}*/
	
    @Override
    public void onBackPressed() {
    	
    }
	
	@Override
	public void onResume() {
	    super.onResume();
	    ((NubisApplication)getApplicationContext()).mainScreenActive = true;
	    setScreenBasedOnCurrentStatus(); //necessary???
	    showVolumeAndInternet();
	    //getBatteryPercentage();
	}
	
	Button.OnClickListener mainBtnOnClickListener = 
			new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				mainBtnOnClickListenerIntent();		 
			}
		};
	
   
   public String getNextAlarm(){
	   
	   class DateCompare implements Comparator<Calendar> {
	       public int compare(Calendar one, Calendar two){
		       return one.compareTo(two);
	      }
	   }
	   
	   ArrayList<Calendar> dates = new ArrayList<Calendar>();
       //order in date/time, not in order of list
	   int start = 0;
	   if (((NubisApplication)getApplicationContext()).lastMainAlarmIndex != -1){
		   start = ((NubisApplication)getApplicationContext()).lastMainAlarmIndex - 1;
	   }
	   
	   for (int i = start; i < ((NubisApplication)getApplicationContext()).alarms.size(); i++){
			NubisAlarm alarm = ((NubisApplication)getApplicationContext()).alarms.get(i).alarm;
			if (alarm.isMainAlarm()){
				if (alarm.active){ //only main and active ones!
					if (alarm.isNewSessionMainAlarm() || alarm.getAlarmType() > ((NubisApplication)getApplicationContext()).status){ //only 'higher' statusses
						if (alarm.alert){
							dates.add(alarm.getCalendar());
						}
					}
				}
			}
	   }
       if (dates.size() == 0){
           return "";
       }
//       else if (dates.size() == 1){
//    	   return ((NubisApplication)getApplicationContext()).settings.texts.getText("nextAlarm[0]", showHour(dates.get(0)));
//    	   return "Next alarm is at " + showHour(dates.get(0)) + " today";
//       }
       else {
    	   //sort!!
    	   sortArrayByDate(dates);
    	   
    	   DateCompare compare = new DateCompare();
	       Collections.sort(dates, compare);
	       Calendar now = Calendar.getInstance();
	       now.setTimeInMillis(System.currentTimeMillis());
	      // now.add(Calendar.DAY_OF_YEAR, 1000);
	      // dates.get(0).add(Calendar.DAY_OF_YEAR, 1000);
	       
	       int days = 0;
	       while (!showDateOnly(now).equals(showDateOnly(dates.get(0)))) {
	    	    days += 1;
   	    		now.add(Calendar.DAY_OF_MONTH, 1); // increment one day at a time
   	    		if (days > 7){ 
   	    			break;
   	    		}
      	   }
	       
	  //     days= (int) (dates.get(0).getTimeInMillis() - now.getTimeInMillis()) / (24 * 60 * 60 * 1000);
	       if (days == 0){
	    	   return ((NubisApplication)getApplicationContext()).settings.texts.getText("nextAlarmMessage[0]", showHour(dates.get(0)));
//	    	   return "Next alarm is at " + showHour(dates.get(0)) + " today";
	       }
	       else if (days == 1){
	    	   return ((NubisApplication)getApplicationContext()).settings.texts.getText("nextAlarmMessage[1]", showHour(dates.get(0)));
//	    	   return "Next alarm is tomorrow at " + showHour(dates.get(0)) + "";
	       }
	       else if (days > 7){
	    	   return ((NubisApplication)getApplicationContext()).settings.texts.getText("nextAlarmMessage[2]");
//	    	   return "Next alarm is more than a week from now";
	       }
	       else {
	    	   return ((NubisApplication)getApplicationContext()).settings.texts.getText("nextAlarmMessage[3]", days, showHour(dates.get(0)));
//    	       return "Next alarm is " + days + " days from now at " + showHour(dates.get(0)) + "";
	       }
       }
   }
		
   public NubisAlarm getPreviousMainAlarm(){
	   if (((NubisApplication)getApplicationContext()).lastMainAlarmIndex != -1){
		   if (((NubisApplication)getApplicationContext()).lastMainAlarmIndex - 4 > 0){
			   return ((NubisApplication)getApplicationContext()).alarms.get(((NubisApplication)getApplicationContext()).lastMainAlarmIndex - 4).alarm;
		   }
	   }
	   return null;
   }

   public int getPreviousMainAlarmResult(){
	   if (getPreviousMainAlarm() != null){
		   return getPreviousMainAlarm().answered;
	   }
	   return -1;
   }
   
   public int getQuestionVersionOfStatus(int status){
	   if (status > 0 && status < NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS){ //convert
		   return status + 100;
	   }
	   return status;	   
   }

   
   public void setScreenBasedOnCurrentStatus(){
/*	 if (((NubisApplication)this.getApplicationContext()).hasInternet && ((NubisApplication)getApplicationContext()).communication != null && ((NubisApplication)getApplicationContext()).communication.getUnsentCount() > 0){
		//there are unsent answers!
	   mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("resendAnswersButton"));
	 }
	 else {*/
	   mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("stressedOutButton"));
  	 //}
	 
	 //mainBtn.setVisibility(View.INVISIBLE);		 
	 mainBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.nubis_buttons));
	 mainBtn.setEnabled(true);
	 
	 
	 //THIS MIGHT BE FRIDAY: CHECK IF NEEDED! OTHERWISE CONVERT STATUS TO QUESTION INSTEAD OF CORTISOL
	 if (((NubisApplication)getApplicationContext()).settings.numberOfCortisol >= ((NubisApplication)getApplicationContext()).settings.cortisolTotalMeasures){
		 ((NubisApplication)getApplicationContext()).status = getQuestionVersionOfStatus(((NubisApplication)getApplicationContext()).status);
	 }
	 
	 
	 if (!(((NubisApplication)getApplicationContext()).hasInternet) && ((NubisApplication)getApplicationContext()).settings.allowOfflineDataEntry == false){
		 mainBtn.setBackgroundColor(getResources().getColor(R.color.NubisRed));
		 mainBtn.setEnabled(false);
	 }
	 
	 int timeMessageNumber = -1;
	 
     if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_NOTHING){ //nothing going on right now!
    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_NOTHING"));   //WELCOME
	 }
	 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_CORTISOL){ //nothing going on right now!
		 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_1_CORTISOL", ((NubisApplication)getApplicationContext()).settings.texts.getGreeting()));  //Good morning!\n\nYou can already take your cortisol if you want.
    	 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("cortisolButton"));
	 }
	 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_CORTISOL_ALARM_1){ //nothing going on right now!
		 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_1_CORTISOL_ALARM_1", ((NubisApplication)getApplicationContext()).settings.texts.getGreeting()));  //Good morning!\n\nTime to take your cortisol!
    	 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("cortisolButton"));
	 }
	 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_CORTISOL_ALARM_2){ //nothing going on right now!
		 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_1_CORTISOL_ALARM_2"));  //Hey Sleepyhead!\nOnly a few minutes left to take your cortisol! Don’t forget the picture of the labeled vial when you are done!");
    	 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("cortisolButton"));
	 }
	 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_CORTISOL_CLOSED){ //nothing going on right now!
  	     infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_1_CORTISOL_CLOSED[0]", ((NubisApplication)getApplicationContext()).lastMainAlarm.reminder30minute));  //Thanks!\n\nSee you in " + ((NubisApplication)getApplicationContext()).lastMainAlarm.round2Reminder + " minutes!
		 if (((NubisApplication)getApplicationContext()).lastMainAlarm != null){
			 if (((NubisApplication)getApplicationContext()).lastMainAlarm.answered < 1){
   			   infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_1_CORTISOL_CLOSED[1]")); //You missed your wake-up cortisol measurement today!"); 
  			   timeMessageNumber = 0; //We will try again tomorrow.");
			 }
		 }
	 }
	 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_CORTISOL_ALARM_1){ //nothing going on right now!
    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_2_CORTISOL_ALARM_1"));  //Time to take your cortisol again! Don’t forget to upload the picture of your labeled vial when you are done!");
    	 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("cortisolButton"));
	 }
	 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_CORTISOL_ALARM_2){ //nothing going on right now!
    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_2_CORTISOL_ALARM_2"));  //Time to take your cortisol again! Don’t forget to upload the picture of your labeled vial when you are done!");
    	 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("cortisolButton"));
	 }
	 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_CORTISOL_CLOSED){ //nothing going on right now!
    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_2_CORTISOL_CLOSED[0]"));  //Thank you!
		 timeMessageNumber = 1;
		 //\n\nWe will get back to you tonight!");
		 if (((NubisApplication)getApplicationContext()).lastMainAlarm != null){
			 if (((NubisApplication)getApplicationContext()).lastMainAlarm.answered < 2){
		    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_2_CORTISOL_CLOSED[1]", ((NubisApplication)getApplicationContext()).lastMainAlarm.reminder30minute));  //You missed your " + ((NubisApplication)getApplicationContext()).lastMainAlarm.round2Reminder + "-minute cortisol this morning!");
				 timeMessageNumber = 1; //\n\nWe will get back to you tonight!");
			 }
/* this will never happen: they came here because they took the wake-up
			 
			 if (((NubisApplication)getApplicationContext()).lastMainAlarm.answered < 1){
  				 infoMain.setText("You missed your wake-up cortisol measurement today!");
  				 timeMessageNumber = 0;
  				 //\n\nWe will try again tomorrow.");
			 }*/
		 }
	 }
	 
	 
	 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_CORTISOL){ //nothing going on right now!
    	 if (getPreviousMainAlarmResult() == 2){ //only if completed in the morning
			 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_EVENING_WINDOW_3_CORTISOL", ((NubisApplication)getApplicationContext()).settings.texts.getGreeting()));  //Good evening!\n\nYou can already take your cortisol if you want.");
			 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("cortisolButton"));
		 }
		 else {
			 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_EVENING_WINDOW_3_QUESTIONS", ((NubisApplication)getApplicationContext()).settings.texts.getGreeting())); //"Good evening!\n\nYou can already answer your evening questions if you want.");
			 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("questionButton"));
		 }
	 }	
	 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_CORTISOL_ALARM){ //nothing going on right now!
		 if (getPreviousMainAlarmResult() == 2){ //only if completed in the morning
			 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_EVENING_WINDOW_3_CORTISOL_ALARM", ((NubisApplication)getApplicationContext()).settings.texts.getGreeting()));  //Good evening!\n\nTime to take your cortisol! Don’t forget to upload a picture of the vial after you are done!.");
			 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("cortisolButton"));
		 }
		 else {
			 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_EVENING_WINDOW_3_QUESTIONS_ALARM", ((NubisApplication)getApplicationContext()).settings.texts.getGreeting())); //"Good evening!\n\nTime to answer your evening questions.");
			 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("questionButton"));
		 }
	 }	
	 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_CORTISOL_CLOSED){ //nothing going on right now!
    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_EVENING_WINDOW_3_CORTISOL_CLOSED[0]")); //Thank you!");
		 timeMessageNumber = 2; //We will get back to you in the morning!");
		 if (((NubisApplication)getApplicationContext()).lastMainAlarm != null){
			/* if (((NubisApplication)getApplicationContext()).lastMainAlarm.answered < 1){
  				 infoMain.setText("You missed your wake-up cortisol measurement today!\n\nWe will try again in the morning.");
			 }
			 else if (((NubisApplication)getApplicationContext()).lastMainAlarm.answered < 2){
				 infoMain.setText("You missed your " + ((NubisApplication)getApplicationContext()).lastMainAlarm.round2Reminder + "-minute cortisol this morning!\n\nWe will get back to you tomorrow!");
			 }
			 else*/
			 if (((NubisApplication)getApplicationContext()).lastMainAlarm.answered2 < 1){
		    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_EVENING_WINDOW_3_CORTISOL_CLOSED[1]")); //You missed your nighttime cortisol measurement.");
				 timeMessageNumber = 3; //\n\nSee you tomorrow!");
			 }
		 }
	 }
 	
	 
//QUESTIONS ONLY!	 
	 
		 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS){
	    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_1_QUESTIONS", ((NubisApplication)getApplicationContext()).settings.texts.getGreeting()));  //Good morning!\n\nYou can already answer your questions if you want.");
	    	 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("questionButton"));
		 }
		 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS_ALARM_1){
	    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_1_QUESTIONS_ALARM_1", ((NubisApplication)getApplicationContext()).settings.texts.getGreeting()));  //"Good morning!\n\nTime to answer your questions!");
	    	 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("questionButton"));
		 }
		 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS_ALARM_2){
	    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_1_QUESTIONS_ALARM_2"));
	    	 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("questionButton"));
		 }
		 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS_CLOSED){
	    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_1_QUESTIONS_CLOSED[0]", ((NubisApplication)getApplicationContext()).lastMainAlarm.reminder30minute)); //Thanks! See you in " + ((NubisApplication)getApplicationContext()).lastMainAlarm.round2Reminder + " minutes!");
			 if (((NubisApplication)getApplicationContext()).lastMainAlarm != null){
				 if (((NubisApplication)getApplicationContext()).lastMainAlarm.answered < 1){
			    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_1_QUESTIONS_CLOSED[1]")); //You missed your wakeup questions today!");
					 timeMessageNumber = 4; //We will try again later!");
				 }
			 }
		 }

		 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_QUESTIONS_ALARM_1){ 
	    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_2_QUESTIONS_ALARM_1")); //Time to answer our questions again!");
	    	 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("questionButton"));
		 }	 
		 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_QUESTIONS_ALARM_2){ 
	    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_2_QUESTIONS_ALARM_2")); //Time to answer our questions again!");
	    	 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("questionButton"));
		 }	 
		 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_QUESTIONS_CLOSED){ 
	    	 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_2_QUESTIONS_CLOSED[0]")); //Thank you!");
   			 timeMessageNumber = 5;
   			 //We will get back to you in the evening!");
   			 if (((NubisApplication)getApplicationContext()).lastMainAlarm != null){
   				 if (((NubisApplication)getApplicationContext()).lastMainAlarm.answered < 1){
   					infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_2_QUESTIONS_CLOSED[1]")); //You missed your wake-up questions today!");
   	  			    timeMessageNumber = 6; //\n\nWe will try again tonight.");
   				 }
   				 else if (((NubisApplication)getApplicationContext()).lastMainAlarm.answered < 2){
   					infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_MORNING_WINDOW_2_QUESTIONS_CLOSED[2]", ((NubisApplication)getApplicationContext()).lastMainAlarm.reminder30minute)); //You missed your " + ((NubisApplication)getApplicationContext()).lastMainAlarm.round2Reminder + "-minute questions this morning!");
   					timeMessageNumber = 5; //\n\nWe will get back to you in the evening!");
   				 }
   			 }
   	     }
		 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_QUESTIONS){ //nothing going on right now!
			 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_EVENING_WINDOW_3_QUESTIONS", ((NubisApplication)getApplicationContext()).settings.texts.getGreeting())); //"Good evening!\n\nYou can already answer your evening questions if you want.");
			 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("questionButton"));
		 }	
		 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_QUESTIONS_ALARM){ //nothing going on right now!
			 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_EVENING_WINDOW_3_QUESTIONS_ALARM", ((NubisApplication)getApplicationContext()).settings.texts.getGreeting())); //"Good evening!\n\nTime to answer your evening questions.");
			 mainBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("questionButton"));
		 }	
		 else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_QUESTIONS_CLOSED){ //nothing going on right now!
			 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_EVENING_WINDOW_3_QUESTIONS_CLOSED[0]")); //Thank you!");
			 timeMessageNumber = 2; //We will get back to you in the morning!");
			 if (((NubisApplication)getApplicationContext()).lastMainAlarm != null){
				/* if (((NubisApplication)getApplicationContext()).lastMainAlarm.answered < 1){
	  				 infoMain.setText("You missed your wake-up questions today!\n\nWe will try again tomorrow.");
				 }
				 else if (((NubisApplication)getApplicationContext()).lastMainAlarm.answered < 2){
					 infoMain.setText("You missed your " + ((NubisApplication)getApplicationContext()).lastMainAlarm.round2Reminder + "-minute questions this morning!\n\nWe will try again tomorrow!");
				 }
				 else*/
				 if (((NubisApplication)getApplicationContext()).lastMainAlarm.answered2 < 0){
					 infoMain.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("S_EVENING_WINDOW_3_QUESTIONS_CLOSED[1]")); //You missed your nighttime questions.");
					 timeMessageNumber = 3; //\n\nSee you tomorrow!");
				 }
			 }
		 }


     if (!(mainBtn.getText().equals(((NubisApplication)getApplicationContext()).settings.texts.getText("cortisolButton")) ||
    		 mainBtn.getText().equals(((NubisApplication)getApplicationContext()).settings.texts.getText("questionButton")))){
//     if (!(cortisolBtn.getVisibility() == View.VISIBLE || questionBtn.getVisibility() == View.VISIBLE)){ //show when next alarm is when there are no buttons on the screen.
    	 if (getNextAlarm() != ""){ //there is still something to do!
        	 if (timeMessageNumber != -1 && timeMessageNumber <= 6){
        		 infoMain.setText(infoMain.getText() + "\n\n" + ((NubisApplication)getApplicationContext()).settings.texts.getText("timeReferenceMessage[" + timeMessageNumber + "]"));
        	 }
    		 infoMain.setText(infoMain.getText() + "\n\n" + getNextAlarm());
    	 }
    	 else {
       	   infoMain.setText(infoMain.getText() + "\n\n" + ((NubisApplication)getApplicationContext()).settings.texts.getText("noAlarmsScheduledMessage"));
    	 }
     }
     else {

     }
     
     messageTab.setText(((NubisApplication)getApplicationContext()).settings.message);
     
	 //make sure this is redrawn!
	 //getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
	 ((NubisApplication)getApplicationContext()).log += "\nset screen status: " + ((NubisApplication)getApplicationContext()).status;
//	 setRelease();
	 NubisAlarmAlertWakeLock.releaseCpuLock();
	 
	 
	 try {
     if (mainBtn.getText() == ((NubisApplication)getApplicationContext()).settings.texts.getText("stressedOutButton")){
			 //ARE THERE UNSENT MESSAGES???
		 if (((NubisApplication)this.getApplicationContext()).hasInternet && ((NubisApplication)getApplicationContext()).communication != null && ((NubisApplication)getApplicationContext()).communication.getUnsentCount() > 0){
				//there are unsent answers!
			  //Toast.makeText(this,"connecting", Toast.LENGTH_SHORT).show();
	          if (resendTimer){
	        	  resendTimer = false;
		  		  Timer timer = new Timer(); 
			   	  timer.schedule(new TimerTask() 
					    { 
					        public void run() 
					        { 
					        	resendDatatoServer();
					        	//hHandler.sendEmptyMessage(0);
					        } 
					    }, 10000); //5 second delay 		
				  //((NubisApplication)getApplicationContext()).communication.checkInternet(this);
	          }
		   }	
		 }
	 }
	 catch (Exception e){
		 
	 }
  
   }
	/*	
   public void setRelease(){
    // AlarmAlertWakeLock.releaseCpuLock();
   }
		*/
		
    public void resendDatatoServer(){
    	if (((NubisApplication)this.getApplicationContext()).hasInternet && ((NubisApplication)getApplicationContext()).communication != null && ((NubisApplication)getApplicationContext()).communication.getUnsentCount() > 0){
    	  ((NubisApplication)getApplicationContext()).communication.checkInternet(this);
    	}
    	resendTimer = true;
    }
   
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nubis_main, menu);
		return true;
	}
	
	
	private void mainBtnOnClickListenerIntent() {
      mainBtn.setEnabled(false);
      
	  if (((NubisApplication)this.getApplicationContext()).hasInternet && ((NubisApplication)getApplicationContext()).communication != null && ((NubisApplication)getApplicationContext()).communication.getUnsentCount() > 0){
			//there are unsent answers!
		  //Toast.makeText(this,"connecting", Toast.LENGTH_SHORT).show();
          Timer timer = new Timer(); 
	   	  timer.schedule(new TimerTask() 
			    { 
			        public void run() 
			        { 
			        	hHandler.sendEmptyMessage(0);
			        } 
			    }, 5000); //5 second delay 		
		  ((NubisApplication)getApplicationContext()).communication.checkInternet(this);

	  
	  }	
	  else {	
		
	      if (mainBtn.getText().equals(((NubisApplication)getApplicationContext()).settings.texts.getText("cortisolButton"))){ //cortisol
	          Intent intent2 = new Intent(this, NubisPicture.class);
	          startActivity(intent2); 
	      }
	      else if (mainBtn.getText().equals(((NubisApplication)getApplicationContext()).settings.texts.getText("questionButton"))){ //questions
	    	  Intent intent2 = new Intent(this, NubisQuestions.class);
	    	  startActivity(intent2); 
	      }
	      else { //open ended
	    	  Intent intent2 = new Intent(this, NubisOpenEnded.class);
	    	  intent2.putExtra("test", 0);
	    	  intent2.putExtra("questionText", ((NubisApplication)getApplicationContext()).settings.texts.getText("stressedOutQuestion"));
	          startActivity(intent2);     	  
	      }
	  }
		
		

	}
	
	
	private Handler hHandler = new Handler()
	  {
	    @Override
	    public void handleMessage(Message msg)
	    {
	    	setScreenBasedOnCurrentStatus();
	    }
	  };

	public void startSettingsScreen(){
	    final Intent intent2 = new Intent(this, NubisSettings.class);
	    startActivity(intent2); 
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent2;
	    switch (item.getItemId()) {
	        case R.id.action_logs:
  	  	        intent2 = new Intent(this, NubisLogs.class);
			    startActivity(intent2); 
	            return true;
	            
	        case R.id.action_settings:
	        	if (((NubisApplication)getApplicationContext()).settings.hasPasswordProtection()){
		        	AlertDialog.Builder alert = new AlertDialog.Builder(this);
	
		        	alert.setTitle("Please enter password");
		        	//alert.setMessage("Message");
	
		        	// Set an EditText view to get user input 
		        	final EditText input = new EditText(this);
		        	input.setSingleLine(true);
		        	input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		        	alert.setView(input);
	
		        	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			        	public void onClick(DialogInterface dialog, int whichButton) {
			        	  Editable value = input.getText();
			        	  if (((NubisApplication)getApplicationContext()).settings.checkPassword(value.toString())) {
			        		  startSettingsScreen();
			        	  }
			        	  else {
			  		        Toast.makeText(getBaseContext(), "Invalid password", Toast.LENGTH_LONG).show();
			        	  }
			        	}
			        });
	
		        	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        	  public void onClick(DialogInterface dialog, int whichButton) {
		        	    // Canceled.
		        	  }
		        	});
	
		        	alert.show();
		            return true;
	        	}
	        	else {
	        		startSettingsScreen();
		            return true;
	        	}
	     }
		return true;
	}
	
	public static String showDate(Calendar calendar){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return formatter.format(calendar.getTimeInMillis());
	}
	
	public static String showDateTimeFile(Calendar calendar){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		return formatter.format(calendar.getTimeInMillis());
	}
	
	public static String showDateTime(Calendar calendar){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(calendar.getTimeInMillis());
	}
	
	public static String showDateOnly(Calendar calendar){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(calendar.getTimeInMillis());
	}
	
	public static String showHour(Calendar calendar){
		SimpleDateFormat formatter = new SimpleDateFormat("h:mma");
		return formatter.format(calendar.getTimeInMillis());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		NubisAlarmAlertWakeLock.releaseCpuLock(); //lockOff(this);
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		//kill all receivers!
		unregisterReceiver(batteryLevelReceiver);
		unregisterReceiver(volumeLevelReceiver);
		unregisterReceiver(networkAvailableReceiver);
		
	}
	
	public static void shuffleArray(int[] ar) {
	    Random rnd = new Random();
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      int a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	  }
	
	public static void sortArrayByDate(ArrayList<Calendar> dates){
//		Comparator comparator = Collections...reverseOrder();
//	    Collections.sort(dates,comparator);
		Collections.sort(dates);

	}

	
	private void getBatteryPercentage(Context context, Intent intent) {
//		  BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
	//	         public void onReceive(Context context, Intent intent) {
		//             context.unregisterReceiver(this);
		             int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		             int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		             int level = -1;
		             if (currentLevel >= 0 && scale > 0) {
		                 level = (currentLevel * 100) / scale;
		             }
		             
		             int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		             boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
		                                 status == BatteryManager.BATTERY_STATUS_FULL;
		         
		            // int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		            // boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		            // boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;		             
		             
		             String chargingMessage = "";
		             if (isCharging){
		            	 chargingMessage = " " + ((NubisApplication)getApplicationContext()).settings.texts.getText("batteryChargingMessage");
		             }		           
		             if (level > 15){
		               batteryPercent.setBackgroundColor(getResources().getColor(R.color.NubisBluePressed));
		               batteryPercent.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("batteryLevelRemainingMessage", level) + chargingMessage);
		             }
		             else {
		               if (level < 10){ //red if below 15
		            	   batteryPercent.setBackgroundColor(getResources().getColor(R.color.NubisRed));
		               }
		               else {
			               batteryPercent.setBackgroundColor(getResources().getColor(R.color.NubisOrange));
		               }
  	            	   batteryPercent.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("batteryLevelLowMessage"));
		             }
		         }
		//     }; 
		//     IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		//     registerReceiver(batteryLevelReceiver, batteryLevelFilter);
		 // }
	
	
  }


/*
Calendar timestamp = Calendar.getInstance();

//Check whether the day of the week was earlier in the week:
if( myAlarmDayOfTheWeek > timestamp.get(Calendar.DAY_OF_WEEK) ) {
  //Set the day of the AlarmManager:
  time.add(Calendar.DAY_OF_YEAR, (myAlarmDayOfTheWeek - timestamp.get(Calendar.DAY_OF_WEEK)));
}
else {
  if( myAlarmDayOfTheWeek < timestamp.get(Calendar.DAY_OF_WEEK) ) {
      //Set the day of the AlarmManager:
      timestamp.add(Calendar.DAY_OF_YEAR, (7 - (timestamp.get(Calendar.DAY_OF_WEEK) - myAlarmDayOfTheWeek)));
  }
  else {  // myAlarmDayOfTheWeek == time.get(Calendar.DAY_OF_WEEK)
      //Check whether the time has already gone:
      if ( (myAlarmHour < timestamp.get(Calendar.HOUR_OF_DAY)) || ((myAlarmHour == timestamp.get(Calendar.HOUR_OF_DAY)) && (myAlarmMinute < timestamp.get(Calendar.MINUTE))) ) {
          //Set the day of the AlarmManager:
          timestamp.add(Calendar.DAY_OF_YEAR, 7);
      }
  }
}

//Set the time of the AlarmManager:
timestamp.set(Calendar.HOUR_OF_DAY, myAlarmHour);
timestamp.set(Calendar.MINUTE, myAlarmMinute);
timestamp.set(Calendar.SECOND, 0);*/