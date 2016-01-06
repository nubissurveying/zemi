package com.nubisZemi;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class NubisApplication extends Application{

	public Settings settings = new Settings();
	public int status = 0;
	public String log = "";
	public boolean mainScreenActive = true;
	public List<NubisAlarmReceiver> alarms = new ArrayList<NubisAlarmReceiver>();
	public AlarmManager alarmMgr = null;

	public boolean hasInternet = true;
	public int volume = 7;
	
	public int lastMainAlarmIndex = -1;
	public NubisAlarm lastMainAlarm = null;
	public int lastSubAlarmIndex = -1;
	public NubisAlarm lastSubAlarm = null;

	public NubisCommunication communication = new NubisCommunication();
	public boolean appRunning = false;
	
	private SharedPreferences preferencesReader;
	
	public void loadSettings(SharedPreferences preferencesReader, Context context){
	  	  try {
	  		    this.preferencesReader = preferencesReader;
		    	String serializedDataFromPreference = preferencesReader.getString("Settings", null);
				if (serializedDataFromPreference != null){ // Create a new object from the serialized data with the same state
					settings = Settings.create(serializedDataFromPreference);
				}
		  }
		  catch (Exception e) {
	            Toast.makeText(context, "Settings changed. Plz update app!", Toast.LENGTH_LONG).show();
		        e.printStackTrace();
		  }
	}
	
	public void saveSettings(){
		// Serialize the object into a string
		String serializedData = settings.serialize();
		SharedPreferences preferencesReader = this.preferencesReader; //  context.getSharedPreferences("NubisSettings", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferencesReader.edit();
		editor.putString("Settings", serializedData);
		editor.commit();
	}
	
	public void clear(){
		alarms.clear();
		status = 0;

		lastMainAlarmIndex = -1;
		lastMainAlarm = null;

		lastSubAlarmIndex = -1;
		lastSubAlarm = null;
		
		log = "";

	}
	
	
}
/* TODO
  * check at midnight for updates
V  * test combi phone/id
V  * messages (read from server and check)
V  * admin: view todays measures (and who missed what and when)
V  * new phone: request settings: add to 'wait list' table -> drop down under edit with choices
  * skip rules.. cortisol missed.. -> only ask questions: change alarmtype
------
V schedule settings read communication/index.php  set rtid once, then check on rtid  $rtid = ''  if rtid == '', set rtid  if rtid == row['rtid']  add line to schedule
--------
V add alarmtype to picture and questions so we can see where pic and questions belong in admin pages..
-------
V add 'today' on admin pages
-------
V change to answered and answered2
-------
settings: texts:
TEXTS
then gzip with ~ seperated..
--------------
V add message for: sound off.. no internet
----------------
message: take from settings.message .. eg: please call us at .....
----------------
V message picture: make sure vials are clearly visible
-------
V fix unlock
-------
V END OF DAY: status should be reset to '0'!
-------
choose other ringtone
-------
V save phone times when saving questions/picture
-------
V next alarm: check time is not in the past
-------
V window: no overlap with 30 minute one
-------
schedule on website: link to picture if cortisol taken
-------
picture on website: full screen when clicked

*/