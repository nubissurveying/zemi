package com.nubisZemi;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import com.nubisZemi.R;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NubisOpenEnded extends Activity {

	private int testMode = 0;
	private String questionText = "";
	EditText openEnded;
	private Button saveButton; 
	private ImageButton pictureButton;
	private ImageButton recordButton;

	
	private Bitmap mImageBitmap;
    private String mFileName;
	
	private static final int ACTION_TAKE_PHOTO_S = 2;
	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";

	private MediaRecorder mRecorder = null;
    private boolean recording = false;
    private boolean soundrecorded = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nubis_open_ended);
		try {
			
			//mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		    //mFileName += "/test.3gp";

		    mFileName = Environment.getExternalStorageDirectory().getPath() + "/" + "sound" + ".3gp";
            //mFileName = "/sdcard/sound/" + "sound.3gp";
		    
			Bundle bundle = this.getIntent().getExtras();
			if (bundle != null){
		      Intent intent=getIntent();
			  this.testMode = intent.getIntExtra("test", 0);
			  this.questionText = intent.getStringExtra("questionText");

			  TextView infoMainQuestion = (TextView) findViewById(R.id.infoMainQuestion);
			  infoMainQuestion.setText(questionText);
	
			}
	
			openEnded = (EditText) findViewById(R.id.openEndedText);
			
	    	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	    	imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
	
	        saveButton = (Button) findViewById(R.id.saveOpenEndedButton);
	    	saveButton.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("saveResponsesButton"));
	        saveButton.setOnClickListener(new View.OnClickListener() {
		         public void onClick(View v) {
		        	 dispatchSaveIntent(v);
		         }
		    });
	        mImageBitmap = null;
	        pictureButton = (ImageButton) findViewById(R.id.openEndedTakePictureButton);
			pictureButton.setImageResource(R.drawable.camera);
	        pictureButton.setOnClickListener(new View.OnClickListener() {
		         public void onClick(View v) {
		        	 dispatchPictureIntent(v);

		         }
		    });

	        recordButton = (ImageButton) findViewById(R.id.openEndedRecordSoundPictureButton);
	        recordButton.setImageResource(R.drawable.microphone);

	        recordButton.setOnClickListener(new View.OnClickListener() {
		         public void onClick(View v) {
		        	 dispatchRecordIntent(v);
		         }
		    });

	        
	        
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
		
	}
	
	public void dispatchPictureIntent(View v){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO_S);
	}
	
	public void startRecording(){
		if (!recording){
			recordButton.setImageResource(R.drawable.microphone_recording);
	        mRecorder = new MediaRecorder();
	        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	        mRecorder.setOutputFile(mFileName);
	        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	
	        try {
	            mRecorder.prepare();
	        } catch (IOException e) {
	     //       Log.e(LOG_TAG, "prepare() failed");
	        }
	        mRecorder.start();	   
	        recording = true;
		}
	}

	public void stopRecording(){
		if (recording){
			mRecorder.stop();
		    mRecorder.release();
		    mRecorder = null;
	        recording = false;
	        soundrecorded = true;
	        recordButton.setImageResource(R.drawable.microphone_check);

		}
	}
	
	public void dispatchRecordIntent(View v){
	    if (recording){  //stop recording
	    	this.stopRecording();
	    }
	    else { //start recording
	    	this.startRecording();
	        Toast.makeText(this.getBaseContext(), ((NubisApplication)getApplicationContext()).settings.texts.getText("startRecordingMessage"), Toast.LENGTH_LONG).show();
	       // recordButton.setImageResource(R.drawable.icon);
	        
	        
	    }
	}
	
	
    public void dispatchSaveIntent(View v) {
    	try {
    		this.stopRecording(); //make sure it is not recording anymore..
    		
    		pictureButton.setEnabled(false);
    		recordButton.setEnabled(false);

    		
    		

    		if (testMode == 0){ //not test mode! save!
    			saveButton.setEnabled(false);
		        Calendar now = Calendar.getInstance();
		        now.setTimeInMillis(System.currentTimeMillis());
		        
		        int ceid = -1;
			    int alarmtype = -1;
			    int subalarmtype = -1;
				if (((NubisApplication)getApplicationContext()).lastMainAlarm != null){
			  	  ceid = ((NubisApplication)getApplicationContext()).lastMainAlarm.ceid;
			      alarmtype = ((NubisApplication)getApplicationContext()).lastMainAlarm.getAlarmType();
			    }
  		        if (((NubisApplication)getApplicationContext()).lastSubAlarm != null){
				  subalarmtype = ((NubisApplication)getApplicationContext()).lastSubAlarm.getAlarmType();
				}
                //add openended answer
				NubisDelayedAnswer delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_GET_READ);
				delayedanswer.addGetParameter("id", Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
				delayedanswer.addGetParameter("version", this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
				delayedanswer.addGetParameter("rtid", ((NubisApplication)getApplicationContext()).settings.getRtid());
				delayedanswer.addGetParameter("ceid", ceid);
				delayedanswer.addGetParameter("alarmtype", alarmtype);
 			    delayedanswer.addGetParameter("subalarmtype", subalarmtype);
				delayedanswer.addGetParameter("phonets", NubisMain.showDateTime(now));
				delayedanswer.addGetParameter("p", "openended");
				delayedanswer.addGetParameter("answered", openEnded.getText().toString());

		        ((NubisApplication)getApplicationContext()).communication.addNubisDelayedAnswer(delayedanswer);
	    		
		        String fileName;
		        ByteArrayOutputStream out;
		        OutputStream outputStreamFile;
		        
		        if (mImageBitmap != null){
			        //add picture
			        
			        delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_POST_FILE);
			        delayedanswer.addGetParameter("id", Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
			        delayedanswer.addGetParameter("version", this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
			 	    delayedanswer.addGetParameter("rtid", ((NubisApplication)getApplicationContext()).settings.getRtid());
			        delayedanswer.addGetParameter("ceid", ceid);
			        delayedanswer.addGetParameter("alarmtype", alarmtype);
			        delayedanswer.addGetParameter("subalarmtype", subalarmtype);
			        delayedanswer.addGetParameter("phonets", NubisMain.showDateTime(now));
			        delayedanswer.addGetParameter("p", "openendedpicture");
					  
				    fileName = Environment.getExternalStorageDirectory().getPath() + "/" + NubisMain.showDateTimeFile(now) + "_" + Integer.toString(ceid) + ".jpg";
				    out = new ByteArrayOutputStream();
				    mImageBitmap.compress(Bitmap.CompressFormat.JPEG, ((NubisApplication)getApplicationContext()).settings.pictureCompression, out);
				    outputStreamFile = new FileOutputStream(fileName); 
				    out.writeTo(outputStreamFile);
				    
				    
				    delayedanswer.addFileName(fileName);
				    delayedanswer.setByteArrayOutputStream();
				  
				    ((NubisApplication)getApplicationContext()).communication.addNubisDelayedAnswer(delayedanswer);
		        }
		        if (soundrecorded){
			        //add sound
			        delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_POST_FILE);
			        delayedanswer.addGetParameter("id", Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
			        delayedanswer.addGetParameter("version", this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
			 	    delayedanswer.addGetParameter("rtid", ((NubisApplication)getApplicationContext()).settings.getRtid());
			        delayedanswer.addGetParameter("ceid", ceid);
			        delayedanswer.addGetParameter("alarmtype", alarmtype);
			        delayedanswer.addGetParameter("subalarmtype", subalarmtype);
			        delayedanswer.addGetParameter("phonets", NubisMain.showDateTime(now));
			        delayedanswer.addGetParameter("p", "openendedsound");
					  
				    //fileName = this.mFileName;
			  
				    //out = new ByteArrayOutputStream();
				    //mImageBitmap.compress(Bitmap.CompressFormat.PNG, 10, out);
				   // mImageBitmap.compress(Bitmap.CompressFormat.JPEG, ((NubisApplication)getApplicationContext()).settings.pictureCompression, out);
				    //outputStreamFile = new FileOutputStream(fileName); 
				    //out.writeTo(outputStreamFile);
				    delayedanswer.addFileName(mFileName);
				    delayedanswer.setByteArrayOutputStream();
				  
				    ((NubisApplication)getApplicationContext()).communication.addNubisDelayedAnswer(delayedanswer);
		        }
			    //done
		        ((NubisApplication)getApplicationContext()).communication.sendOrStoreLocal(this);

		        NavUtils.navigateUpFromSameTask(this);   			
    		}
    		else {
  	    	    final Intent intent2 = new Intent(this, NubisSettings.class);
	    	    startActivity(intent2); 
    		}

    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }	
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nubis_open_ended, menu);
		return true;
	}
*/
    private void handleSmallCameraPhoto(Intent intent) {
		Bundle extras = intent.getExtras();
		mImageBitmap = (Bitmap) extras.get("data");
/*		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(View.VISIBLE);
		
		View b = findViewById(R.id.closeButton);
		b.setEnabled(true);*/
		
	//	mVideoView.setVisibility(View.INVISIBLE);
		pictureButton.setImageResource(R.drawable.camera_check);
	}
    
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			handleSmallCameraPhoto(data);
		}
    }
    
    @Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
//		mImageView.setImageBitmap(mImageBitmap);
//		mImageView.setVisibility(savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? ImageView.VISIBLE : ImageView.INVISIBLE );
	}
    
}
