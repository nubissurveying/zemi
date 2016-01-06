package com.nubisZemi;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings.Secure;

public class NubisAlarmReceiver extends BroadcastReceiver {
	
    public PendingIntent pi;
    private AlarmManager am;
    private static Handler handler;
    public NubisAlarm alarm;
    public NubisAlarm parentAlarm;

   /* public NubisAlarmReceiver(AlarmReceiverCallback callback) {
    	super();
        //this.callback=callback;
    }*/
   
    public void SetAlarm(Context context, AlarmManager am, int id, NubisAlarm alarm, Handler hand) {
    	handler = hand;
    	this.am = am;
    	this.alarm = alarm;
        Intent i = new Intent(context, NubisAlarmReceiver.class);
        i.putExtra("index", id);
        this.pi = PendingIntent.getBroadcast(context, id, i, 0); //was 0
     //   String test = NubisMain.showDate(alarm.getCalendar());
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        if (now.getTimeInMillis() > alarm.getCalendar().getTimeInMillis()){ //alarm in the past..
        	alarm.active = false;	
        }
        if (alarm.active){ //only add if an alarm is active!
		  am.set(AlarmManager.RTC_WAKEUP, alarm.getCalendar().getTimeInMillis(), pi);
        }
        //ELAPSED_REALTIME_WAKEUP t
     }

    public void setUpdateAlarm(Context context, AlarmManager am, int id){
    	this.am = am;
        Intent i = new Intent(context, NubisAlarmReceiver.class);
        this.pi = PendingIntent.getBroadcast(context, id, i, 0); //was 0
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        now.set(Calendar.HOUR_OF_DAY, 12); //midnight!
        now.set(Calendar.MINUTE, 1); //midnight!
      //  String test = NubisMain.showDate(now);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.getTimeInMillis() + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, pi);  //one minute!
       // am.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.getTimeInMillis() + 10, 9000, pi);  //one minute!

    }
    
	public void CancelAlarm() {
         //Intent intent = new Intent(context, NubisAlarmReceiver.class);
         //PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
         //AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	
		 //handle active=false here??
		
         this.am.cancel(this.pi);
     }
     
	 public int getIndexFromAlarm(Context context, Calendar datetime){
		 try {
			for (int i = 0; i < ((NubisApplication)context.getApplicationContext()).alarms.size(); i++) { 
	  		  if (NubisMain.showDateTime(((NubisApplication)context.getApplicationContext()).alarms.get(i).alarm.getCalendar()).equals(NubisMain.showDateTime(datetime))) { //only once!
	            return i;
	  		  }
		 	}
		 }
		 catch(Exception e){
           e.printStackTrace();
           ((NubisApplication)context.getApplicationContext()).log += "getindexfromalarm" + e.getMessage();

		 }
		return -1;
	 }
	
	
     @Override
     public void onReceive(Context context, Intent intent) {
	//	Intent mathAlarmServiceIntent = new Intent(context, NubisAlarmReceiver.class);
	//	context.sendBroadcast(mathAlarmServiceIntent, null);
 	   try {
	       Calendar now = Calendar.getInstance();
	       now.setTimeInMillis(System.currentTimeMillis());

 		   ((NubisApplication)context.getApplicationContext()).log += "\n-------------------";
	       ((NubisApplication)context.getApplicationContext()).log += "\ntriggered!";
	       
	       int index = intent.getIntExtra("index", -1); //get from intent
	       if (index == -1){
	    	   index = getIndexFromAlarm(context, now); //if that doesn't work, maybe it is in the list?
	       }
	       
	       if (index == -1){
	    	   //UPDATE!!
	    	   ((NubisApplication)context.getApplicationContext()).log += "\nNO INTENT!";
		  	   ((NubisApplication)context.getApplicationContext()).log += "\n" + NubisMain.showDate(now);  	   

		  	   //SEND LOG
			   NubisDelayedAnswer delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_POST);
			   delayedanswer.addGetParameter("id", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
			   delayedanswer.addGetParameter("rtid", ((NubisApplication)context.getApplicationContext()).settings.getRtid());
			   delayedanswer.addGetParameter("p", "log");
		  	   
			   delayedanswer.setPostData("log=" + ((NubisApplication)context.getApplicationContext()).log);
			   
		       ((NubisApplication)context.getApplicationContext()).communication.addNubisDelayedAnswer(delayedanswer);
		       ((NubisApplication)context.getApplicationContext()).communication.sendOrStoreLocal(context);			   
			   		  
               //go to main screen to update message (change tomorrow into today for the message!
               final Intent newIntent = new Intent(context, NubisMain.class);
			   newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		       context.startActivity(newIntent);
	    	   
	       }
	       else {
		       //get the alarm from the alarms list
		       final NubisAlarm alarm = ((NubisApplication)context.getApplicationContext()).alarms.get(index).alarm;
	           //set last main alarm to parent alarm of this one..
		       ((NubisApplication)context.getApplicationContext()).lastMainAlarmIndex = alarm.parentid;
		       ((NubisApplication)context.getApplicationContext()).lastMainAlarm = ((NubisApplication)context.getApplicationContext()).alarms.get(alarm.parentid).alarm;	       

		       //set last sub alarm
		       ((NubisApplication)context.getApplicationContext()).lastSubAlarmIndex = index;
		       ((NubisApplication)context.getApplicationContext()).lastSubAlarm = alarm;	       
		       
		       
		       
		       //this will get the parent alarm
		       //Settings settings = ((NubisApplication)context.getApplicationContext()).settings;
		  	   //final Alarm parentAlarm = settings.getAlarm(alarm.parentid);
		  	   
		  	   //LOG!! 
		  	   ((NubisApplication)context.getApplicationContext()).log += "\n" + NubisMain.showDate(now) + "-----" + NubisMain.showDate(alarm.getCalendar());  	   
			   ((NubisApplication)context.getApplicationContext()).log += "\nmainscreen: " + Boolean.toString(((NubisApplication)context.getApplicationContext()).mainScreenActive);
			   ((NubisApplication)context.getApplicationContext()).log += " - alert: " + Boolean.toString(alarm.alert);
	    	   ((NubisApplication)context.getApplicationContext()).log += " - status: " + ((NubisApplication)context.getApplicationContext()).status + " to " + alarm.getAlarmType();  	   
	    	   ((NubisApplication)context.getApplicationContext()).log += "\n-------------------";
		  	   int alarmtriggered = 0;
		  	   if (NubisMain.showDate(alarm.getCalendar()).equals(NubisMain.showDate(now))){ //Is this the right alarm?
				    if (alarm.isNewSessionAlarm() || ((NubisApplication)context.getApplicationContext()).status < alarm.getAlarmType()){ //ONLY ALERT SOMETHING IF THE CURRENT STATUS IS LESS THAN WHAT WE ARE DOING NOW
					    //update status!!!
					    ((NubisApplication)context.getApplicationContext()).status = alarm.getAlarmType();  
			 		    //end update!!!   
					    
				 		if (((NubisApplication)context.getApplicationContext()).mainScreenActive){ //only if main screen active (don't do anything if user is working on ohter screen already!)
					 		NubisAlarmAlertWakeLock.acquireCpuWakeLock(context);
				 			alarmtriggered = 1;
				 			if (alarm.alert){ //Sound and vibration!
						     	final Intent newIntent = new Intent(context, NubisAlarmAlert.class);
						     	//Bundle bundle = intent.getExtras();
								//final Alarm alarm = (Alarm) bundle.getSerializable("alarm");
						     	
						     	newIntent.putExtra("alarm", alarm);
							    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						        context.startActivity(newIntent);
						    }
						    else { //JUST GO TO THE MAIN SCREEN!
						     	final Intent newIntent = new Intent(context, NubisMain.class);
							    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						        context.startActivity(newIntent);
						    }
				 		}
				    }
		  	   }
		  	   //send info back to main
		  	   handler.obtainMessage(alarmtriggered, "alarm").sendToTarget();
			   //cancel this alarm!
			   ((NubisApplication)context.getApplicationContext()).log += "\ndelete: " + index + ": " + NubisMain.showDate(alarm.getCalendar());
			   ((NubisApplication)context.getApplicationContext()).alarms.get(index).CancelAlarm();
	           alarm.active = false;
			   //((NubisApplication)context.getApplicationContext()).alarms.get(intent.getIntExtra("index", 0)).alarm.active = false;
	       }
	   }
       catch (Exception e) {
           e.printStackTrace();
           ((NubisApplication)context.getApplicationContext()).log += e.getMessage();
       }			
	
			
		
    }
     
	////@Override
	public void onReceive2(Context context, Intent intent) {
		try {
		// TODO Auto-generated method stub
		NubisAlarmAlertWakeLock.acquireCpuWakeLock(context);
		
    	Calendar now = Calendar.getInstance();
    	now.setTimeInMillis(System.currentTimeMillis());
	   
	   Settings settings = ((NubisApplication)context.getApplicationContext()).settings;
	   //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd H:i");
	//	return formatter.format(calendar.getTime());
	   Calendar alarm = settings.getAlarm(intent.getIntExtra("index", 0)).getCalendar();
	   
	   ((NubisApplication)context.getApplicationContext()).log += "\nalert!:\n " + 
			   NubisMain.showDate(now) +
	      "-----" + 
	      NubisMain.showDate(alarm);
	      
	   int alarmtriggered = 0;
	   if (NubisMain.showDate(alarm).equals(NubisMain.showDate(now))){
	   //if(settings.getAlarm(intent.getIntExtra("index", 0)).getCalendar().getTimeInMillis() + 500 > now.getTimeInMillis()){  //don't trigger events from the past!!

		   
		   
		   ((NubisApplication)context.getApplicationContext()).log += " --> triggered!";
		   //update status!!!
		   ((NubisApplication)context.getApplicationContext()).status = settings.getAlarm(intent.getIntExtra("index", 0)).getAlarmType();  
		   //end update!!!   
		
		  
		   
		   Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		   v.vibrate(300);
		   try {
		       Uri notification_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		       Ringtone r = RingtoneManager.getRingtone(context, notification_uri);
		       r.play();
		   } catch (Exception e) {}
		    //disable keyguard..
		
		   
		   KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

//		   if (!AlarmAlertWakeLock.pm.isScreenOn()) {
		          KeyguardLock kl = km.newKeyguardLock("TAG");
		          kl.disableKeyguard();
	//	       }
		   
		   /*PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		   KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		   if (!pm.isScreenOn()) {
	          KeyguardLock kl = km.newKeyguardLock("TAG");
	          kl.disableKeyguard();
	       }
		*/
/*		   
		   PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		   KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
	*/	   
		   /*if (!pm.isScreenOn()) {
		        KeyguardLock kl = km.newKeyguardLock("TAG");
		        kl.disableKeyguard();
		   }*/
		   /*
		   if (!pm.isScreenOn()) {
		        KeyguardLock kl = km.newKeyguardLock("TAG");
		        kl.disableKeyguard();
		   }
		   */
		   
	/*	   WakeLock wl = null;
		
		   if (!pm.isScreenOn()) {
		        KeyguardLock kl = km.newKeyguardLock("TAG");
		        kl.disableKeyguard();
		        wl = pm.newWakeLock(
		            PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE |
		            PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK , "TAG");
		        wl.acquire();
		   }*/
		/*
		    pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		    PowerManager.WakeLock sCpuWakeLock;
	        sCpuWakeLock = pm.newWakeLock(
	                PowerManager.PARTIAL_WAKE_LOCK |
	                PowerManager.ACQUIRE_CAUSES_WAKEUP |
	                PowerManager.ON_AFTER_RELEASE, "TAG");
	        sCpuWakeLock.acquire();
		  */
		    /*
		    Intent newIntent = new Intent();
		    newIntent.setClassName("com.nubisTouchstone", "com.nubisTouchstone.NubisMain");
		    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		    context.startActivity(newIntent);
		   */
		   
		
		    
		    alarmtriggered = 1;
		   //Toast.makeText(context, "Alarm worked", Toast.LENGTH_LONG).show();
		   //update screen!
		}
	   else {

	   }
	   
	   handler.obtainMessage(alarmtriggered, "alarm").sendToTarget();
	   
	   //cancel this alarm!
	   ((NubisApplication)context.getApplicationContext()).log += "\ndelete: " + intent.getIntExtra("index", 0) + ": " + NubisMain.showDate(alarm);
	   ((NubisApplication)context.getApplicationContext()).alarms.get(intent.getIntExtra("index", 0)).CancelAlarm();
	   ((NubisApplication)context.getApplicationContext()).alarms.get(intent.getIntExtra("index", 0)).alarm.active = false;

		}
        catch (Exception e) {
            e.printStackTrace();
        }			
		
  }
	
}
