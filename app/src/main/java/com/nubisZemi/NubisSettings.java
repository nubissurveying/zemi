package com.nubisZemi;

import com.nubisZemi.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class NubisSettings extends Activity {
	
	ProgressDialog	barProgressDialog;
	 Handler updateBarHandler;

	 private String temp = "";
	 
	private static final int PROGRESS = 0x1;

    private ProgressBar mProgress;
    private int mProgressStatus = 0;

    private Handler mHandler = new Handler();
	
	TextView updateView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nubis_settings);
		
		updateView = (TextView) findViewById(R.id.updateTextView);
		updateView.setMovementMethod(new ScrollingMovementMethod());
		  updateBarHandler = new Handler();

        final Button closeButton = (Button) findViewById(R.id.settingsCloseButton);
        closeButton.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("closeButton")); 

        closeButton.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	        	 dispatchCloseIntent();
	         }
	    });

/*		
        final Button updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("updateButton")); 
        updateButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.nubis_buttons));
        updateButton.setEnabled(true);
        if (!((NubisApplication)getApplicationContext()).hasInternet){
        	updateButton.setEnabled(true);
        }
        updateButton.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	   		  //mProgress = (ProgressBar) findViewById(R.id.progressBar);

			  //mProgress.setVisibility(View.VISIBLE);	
	        	 dispatchUpdateIntent();
	        //	 updateView.setText(temp);
	         }
	    });*/
        
        final Button serverButton = (Button) findViewById(R.id.serverButton);
        
        serverButton.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	        	 dispatchServerIntent();
	         }
	    });

        
        final Button resetButton = (Button) findViewById(R.id.resetSettingsButton);
        
        resetButton.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	        	 dispatchResetIntent();
	         }
	    });

        final Button settingsSendLogButton = (Button) findViewById(R.id.settingsSendLogButton);
        
        settingsSendLogButton.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	        	 dispatchSendLogIntent();
	         }
	    });
        
        
        
        
        
        


        final Button testQuestionButton = (Button) findViewById(R.id.testQuestionButton);
        
        testQuestionButton.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
               dispatchTestQuestionsIntent();
	         }
	    });
        
        final Button testCortisolButton = (Button) findViewById(R.id.testCortisolButton);
        
        testCortisolButton.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	        	 dispatchTestCortisolIntent();
	         }
	    });

        
        final Button uploadButton = (Button) findViewById(R.id.uploadDataButton);
        uploadButton.setEnabled(true);
        if (!((NubisApplication)getApplicationContext()).hasInternet){
        	uploadButton.setEnabled(true);
        }
        uploadButton.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	        	 dispatchUploadIntent();
	         }
	    });
        
        final Button logsButton = (Button) findViewById(R.id.logsButton);
        logsButton.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View v) {
	        	 dispatchLogIntent();
	         }
	    });
        
        
		
		
	}
	
	private void dispatchTestQuestionsIntent(){
 	  Intent intent2 = new Intent(this, NubisQuestions.class);
	  intent2.putExtra("test", 1);
	  startActivity(intent2); 
	}
	
	private void dispatchTestCortisolIntent(){
	      Intent intent2 = new Intent(this, NubisPicture.class);
	      intent2.putExtra("test", 1);
	      startActivity(intent2); 
	}
	
	private void dispatchUploadIntent(){
		((NubisApplication)getApplicationContext()).communication.checkInternet(this);
	}
	
	private void dispatchLogIntent(){
	    final Intent intent2 = new Intent(this, NubisLogs.class);
	    intent2.putExtra("test", 1);
	    startActivity(intent2); 
		
	}
	
	private void dispatchResetIntent(){
		((NubisApplication)getApplicationContext()).settings.resetSettings();
		Toast.makeText(this.getBaseContext(),"Application reset", Toast.LENGTH_SHORT).show();
		
	}
	
	private void dispatchSendLogIntent(){
		
  	   //SEND LOG
	   NubisDelayedAnswer delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_POST);
	   delayedanswer.addGetParameter("id", Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
	   delayedanswer.addGetParameter("rtid", ((NubisApplication)this.getApplicationContext()).settings.getRtid());
	   delayedanswer.addGetParameter("p", "log");
  	   
	   delayedanswer.setPostData("log=" + ((NubisApplication)this.getApplicationContext()).log);
	   
       ((NubisApplication)this.getApplicationContext()).communication.addNubisDelayedAnswer(delayedanswer);
       ((NubisApplication)this.getApplicationContext()).communication.sendOrStoreLocal(this);			   
	   //END SEND LOG
		
		
		Toast.makeText(this.getBaseContext(),"Sending log", Toast.LENGTH_SHORT).show();
	}
	
	private void dispatchServerIntent(){
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	
    	alert.setTitle("Please enter server name");
    	//alert.setMessage("Message");

    	// Set an EditText view to get user input 
    	final EditText input = new EditText(this);
    	input.setSingleLine(true);
    	input.setText(((NubisApplication)getApplicationContext()).settings.serverURL);
    	//input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    	alert.setView(input);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        	  Editable value = input.getText();
        	  ((NubisApplication)getApplicationContext()).settings.serverURL = value.toString();
        	}
        });

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	  public void onClick(DialogInterface dialog, int whichButton) {
    	    // Canceled.
    	  }
    	});

    	alert.show();
		
	}
	
	
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nubbis_settings, menu);
		return true;
	}*/
	
	
	
	@Override
	public void onBackPressed() {
	
	}
	/*
	private void dispatchUpdateIntent(){
		try {
            //send delayed first!
			((NubisApplication)getApplicationContext()).communication.sendDelayedAnswer(getBaseContext());
			//now update!
            ((NubisApplication)getApplicationContext()).communication.updateFromServer(getBaseContext(), new Handler() {
				 public void handleMessage(Message msg) {
					 ((NubisApplication)getApplicationContext()).log += "\nUpdate completed";
                  } } );			
     	}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	*/
	private void dispatchCloseIntent(){
	//	((NubisApplication)getApplicationContext()).log = ""; //empty log
    	 NavUtils.navigateUpFromSameTask(this);
	}


}
