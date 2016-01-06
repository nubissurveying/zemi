package com.nubisZemi;

import android.os.Bundle;
import android.app.Activity;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import com.nubisZemi.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class NubisPicture extends Activity {

//	private int alarmIndex = -1;
	
//	private static final int ACTION_TAKE_PHOTO_B = 1;
	private static final int ACTION_TAKE_PHOTO_S = 2;
//	private static final int ACTION_TAKE_VIDEO = 3;

	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private ImageView mImageView;
	private Bitmap mImageBitmap;

	private Button sendBtn;
	private Button picBtn;
	
	/*private static final String VIDEO_STORAGE_KEY = "viewvideo";
	private static final String VIDEOVIEW_VISIBILITY_STORAGE_KEY = "videoviewvisibility";
	private VideoView mVideoView;
	private Uri mVideoUri;*/

	//private String mCurrentPhotoPath;

	//private static final String JPEG_FILE_PREFIX = "IMG_";
	//private static final String JPEG_FILE_SUFFIX = ".jpg";

	
	//private Bitmap bitmap; 
//	private int serverResponseCode = 0;
    private int testMode = 0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    ((NubisApplication)getApplicationContext()).mainScreenActive = false;
		Bundle bundle = this.getIntent().getExtras();
		//if (bundle != null){
		//  this.alarmIndex = bundle.getInt("alarmindex", -1);
	//	}
	  
		if (bundle != null){
		  this.testMode = bundle.getInt("test", 0);
		}

		
	    
	//	final Window window = getWindow();
	//	window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	//	window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		setContentView(R.layout.activity_nubis_picture);
		mImageView = (ImageView) findViewById(R.id.imageView1);
		mImageBitmap = null;

		TextView textview = (TextView) findViewById(R.id.infoMainPicture);
		textview.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("pictureHeaderMessage"));

		
		
		picBtn = (Button) findViewById(R.id.takePictureButton);
		picBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("takePictureButton"));

		setBtnListenerOrDisable( 
				picBtn, 
				mTakePicOnClickListener,
				MediaStore.ACTION_IMAGE_CAPTURE
		);

		sendBtn = (Button) findViewById(R.id.closeButton);

		
		sendBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("sendButton"));
		
		sendBtn.setEnabled(false);
		sendBtn.setOnClickListener(mPicUploadOnClickListener); 
		
/*		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}*/
	}
	
	
	private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(takePictureIntent, actionCode);
	}

	
	private void dispatchPictureUploadIntent() {
	  try {
	        if (testMode == 0){ //not test mode!
	          sendBtn.setEnabled(false);
	          picBtn.setEnabled(false);
	        	
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
		      
		      NubisDelayedAnswer delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_POST_FILE);
			  delayedanswer.addGetParameter("id", Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
	 		  delayedanswer.addGetParameter("version", this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
			  delayedanswer.addGetParameter("ceid", ceid);
			  delayedanswer.addGetParameter("alarmtype", alarmtype);
			  delayedanswer.addGetParameter("subalarmtype", subalarmtype);
			  delayedanswer.addGetParameter("phonets", NubisMain.showDateTime(now));
			  delayedanswer.addGetParameter("p", "picture");
		
			  
			  delayedanswer.addGetParameter("rtid", ((NubisApplication)getApplicationContext()).settings.getRtid());
			  String fileName = Environment.getExternalStorageDirectory().getPath() + "/" + NubisMain.showDateTimeFile(now) + "_" + Integer.toString(ceid) + ".jpg";
		  
			  ByteArrayOutputStream out = new ByteArrayOutputStream();
			  //mImageBitmap.compress(Bitmap.CompressFormat.PNG, 10, out);
			  mImageBitmap.compress(Bitmap.CompressFormat.JPEG, ((NubisApplication)getApplicationContext()).settings.pictureCompression, out);
			  OutputStream outputStreamFile = new FileOutputStream(fileName); 
			  out.writeTo(outputStreamFile);
			  delayedanswer.addFileName(fileName);
			  delayedanswer.setByteArrayOutputStream();
			  
			  ((NubisApplication)getApplicationContext()).communication.addNubisDelayedAnswer(delayedanswer);
			  ((NubisApplication)getApplicationContext()).communication.sendOrStoreLocal(this);
			  
			  ((NubisApplication)getApplicationContext()).settings.numberOfCortisol++; 
			  
	        }
	    
		    //start questions!
	        Intent intent2 = new Intent(this, NubisQuestions.class);
//		    intent2.putExtra("talarmindex", alarmIndex);
		    intent2.putExtra("test", testMode);
	
	        startActivity(intent2); 
	      
  	    } catch (Exception e) {
         ///  dialog.dismiss();  
           e.printStackTrace();
  	    }
  	  
	}
	
	@Override
	public void onBackPressed() {
	
	}
    
	private void handleSmallCameraPhoto(Intent intent) {
		Bundle extras = intent.getExtras();
		mImageBitmap = (Bitmap) extras.get("data");
		mImageView.setImageBitmap(mImageBitmap);
		//mVideoUri = null;
		mImageView.setVisibility(View.VISIBLE);
		
		View b = findViewById(R.id.closeButton);
		b.setEnabled(true);
		
	//	mVideoView.setVisibility(View.INVISIBLE);
	}
/*
	private void handleBigCameraPhoto() {

		if (mCurrentPhotoPath != null) {
			setPic();
			galleryAddPic();
			mCurrentPhotoPath = null;
		}

	}

	private void handleCameraVideo(Intent intent) {
		mVideoUri = intent.getData();
		mVideoView.setVideoURI(mVideoUri);
		mImageBitmap = null;
		mVideoView.setVisibility(View.VISIBLE);
		mImageView.setVisibility(View.INVISIBLE);
	}
*/
	Button.OnClickListener mTakePicOnClickListener = 
		new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
			picBtn.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("reTakePictureButton"));
			//dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
		}
	};

	Button.OnClickListener mPicUploadOnClickListener = 
		new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchPictureUploadIntent();
		}
	};

	/*Button.OnClickListener mTakeVidOnClickListener = 
		new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchTakeVideoIntent();
		}
	};*/



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			handleSmallCameraPhoto(data);
		}
		/*
		switch (requestCode) {
	/*	case ACTION_TAKE_PHOTO_B: {
			if (resultCode == RESULT_OK) {
				handleSmallCameraPhoto(data);
				//handleBigCameraPhoto();
			}
			break;
		} // ACTION_TAKE_PHOTO_B
*/
	/*	case ACTION_TAKE_PHOTO_S: {
			if (resultCode == RESULT_OK) {
				handleSmallCameraPhoto(data);
			}
			break;
		} // ACTION_TAKE_PHOTO_S

	/*	case ACTION_TAKE_VIDEO: {
			if (resultCode == RESULT_OK) {
				handleCameraVideo(data);
			}
			break;
		} // ACTION_TAKE_VIDEO*/
  	//  } // switch
	}

	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
//		outState.putParcelable(VIDEO_STORAGE_KEY, mVideoUri);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
//		outState.putBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY, (mVideoUri != null) );
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
	//	mVideoUri = savedInstanceState.getParcelable(VIDEO_STORAGE_KEY);
		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(
				savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
		);
	/*	mVideoView.setVideoURI(mVideoUri);
		mVideoView.setVisibility(
				savedInstanceState.getBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
		);*/
	}

	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	private void setBtnListenerOrDisable( 
			Button btn, 
			Button.OnClickListener onClickListener,
			String intentName
	) {
		if (isIntentAvailable(this, intentName)) {
			btn.setOnClickListener(onClickListener);        	
		} else {
			btn.setText( 
				getText(R.string.cannot).toString() + " " + btn.getText());
			btn.setClickable(false);
		}
	}	
	
	public void onResume() {
	    super.onResume();
	    ((NubisApplication)getApplicationContext()).mainScreenActive = false;
	}

	
	
}
