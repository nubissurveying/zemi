package com.nubisZemi;

import java.util.Calendar;

import com.nubisZemi.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.support.v4.app.NavUtils;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NubisLogs extends Activity {

	private TextView infoText;
    private int testMode = 0;
	
    Button closeButton;
    Button updateButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	//	final Window window = getWindow();
	//	window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	//	window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		setContentView(R.layout.activity_nubis_logs);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null){
		  this.testMode = bundle.getInt("test", 0);
		}
		
        closeButton = (Button) findViewById(R.id.closeSettingsButton);
        closeButton.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("closeButton")); 

        closeButton.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	        	 dispatchCloseIntent();
	         }
	    });
        
        updateButton = (Button) findViewById(R.id.updateSettingsButton);
        updateButton.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("updateButton")); 

        updateButton.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	        	 dispatchUpdateIntent();
	         }
	    });

        
        setInfoText("");
        
        
	}
	
	

	
	
	private void setInfoText(String extralog){
		try {
			String android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID); 
			
			infoText = (TextView) findViewById(R.id.infoView);
			infoText.setMovementMethod(new ScrollingMovementMethod());
			infoText.setText("Android id: " + android_id);
			infoText.setText(infoText.getText() + "\nStatus: " + ((NubisApplication)getApplicationContext()).status);
			infoText.setText(infoText.getText() + "\nLast alarm: " + ((NubisApplication)getApplicationContext()).lastMainAlarmIndex);
			infoText.setText(infoText.getText() + "\nVersion: " + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
			
			TelephonyManager tMgr = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
			infoText.setText(infoText.getText() + "\nPhone #: " + tMgr.getLine1Number());
			infoText.setText(infoText.getText() + "\n# stored alarms: " + ((NubisApplication)getApplicationContext()).communication.delayedAnswers.getDelayedAnswerCount());
			infoText.setText(infoText.getText() + " (" + ((NubisApplication)getApplicationContext()).communication.getUnsentCount() + ")");
			infoText.setText(infoText.getText() + "\n# cortisol: " + ((NubisApplication)getApplicationContext()).settings.numberOfCortisol);
			
			infoText.setText(infoText.getText() + ((NubisApplication)getApplicationContext()).settings.toLog());
			infoText.setText(infoText.getText() + "\nCURRENT ALARMS:");
			for (int i = 0; i < ((NubisApplication)getApplicationContext()).alarms.size(); i++){
				NubisAlarm alarm = ((NubisApplication)getApplicationContext()).alarms.get(i).alarm;
				infoText.setText(infoText.getText() + "\n" + i + ":" + NubisMain.showDate(alarm.getCalendar()) + alarm.showIsMain() + " (" + Boolean.toString(alarm.active) + ") t: " + alarm.getAlarmType() + " s: " + Boolean.toString(alarm.alert) + " p:" + alarm.parentid     );
			}
			infoText.setText(infoText.getText() + "\n--------------\n");
			Calendar now = Calendar.getInstance();
			infoText.setText(infoText.getText() + "\n" + NubisMain.showDate(now)); //now.get(Calendar.YEAR) + "-" + now.get(Calendar.MONTH) + "-" + now.get(Calendar.DAY_OF_MONTH) + " " + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE)) ;
			
			infoText.setText(infoText.getText() + "\n" + extralog);
			infoText.setText(infoText.getText() + "\nLog: " + ((NubisApplication)getApplicationContext()).log);
		}
		catch(Exception e){
			
		}
	}

	private void disableButtons(){
	    closeButton.setEnabled(false);
	    updateButton.setEnabled(false);
	}

	private void enableButtons(){
	    closeButton.setEnabled(true);
	    updateButton.setEnabled(true);
	}
	
	private void dispatchUpdateIntent(){
		try {
			disableButtons();
			((NubisApplication)getApplicationContext()).communication.readSettingsDone = false;

			
			//send delayed first!
			((NubisApplication)getApplicationContext()).communication.sendDelayedAnswer(getBaseContext());
			//now update!
			((NubisApplication)getApplicationContext()).communication.updateFromServer(getBaseContext(), new Handler() {
				 public void handleMessage(Message msg) {
					 ((NubisApplication)getApplicationContext()).log += "\nUpdate completed";
					 setInfoText("");
					 Toast.makeText(getApplicationContext(), "Update done", Toast.LENGTH_LONG).show();
					 enableButtons();
                  } } );			
									
            //WAIT UNTIL LOADED!
        /*    long startTime = System.currentTimeMillis();
			while(((NubisApplication)getApplicationContext()).communication.readSettingsDone == false){
				Thread.sleep(500);
				 //if ((System.currentTimeMillis()-startTime)>5000){ 
					// break; 
				 //} //timeout!
	               // waiting until finished protected String[] doInBackground(Void... params)          
	        } 
          */  
            
     	}
		catch(Exception e){
			e.printStackTrace();
		}

	}
	
	private void dispatchCloseIntent(){
	//	((NubisApplication)getApplicationContext()).log = ""; //empty log
    	 NavUtils.navigateUpFromSameTask(this);
	}
	
	@Override
	public void onBackPressed() {
	
	}

}
