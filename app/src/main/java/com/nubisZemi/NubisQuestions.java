package com.nubisZemi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.nubisZemi.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.support.v4.app.NavUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;


public class NubisQuestions extends Activity {

	
	private int testMode = 0;


	private Button saveButton;
	
	 public List<NubisDynamicQuestion> dynamicQuestions = new ArrayList<NubisDynamicQuestion>();
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    ((NubisApplication)getApplicationContext()).mainScreenActive = false;
	//	final Window window = getWindow();
	//	window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	//	window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	    
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null){
		  this.testMode = bundle.getInt("test", 0);
		}
		
		
        setContentView(R.layout.activity_nubis_questions);
        
	    //////START DYNAMIC
        //SET TEXTS		        thumb.mutate().setAlpha(250); 

        setTextViewMessage(R.id.headerView, ((NubisApplication)getApplicationContext()).settings.texts.getText("questionHeaderMessage"));

        if (((NubisApplication)getApplicationContext()).settings.randomizeQuestions){

            //ADD STRESSED.. ALWAYS FIRST
            dynamicQuestions.add(new NubisDynamicQuestion(NubisDynamicQuestion.Q_RADIOBUTTON_SCALE, 1));
            dynamicQuestions.get(0).createRadioButtonScale(this, R.id.baseLinearLayout, ((NubisApplication)getApplicationContext()).settings.texts.getText("question1Message"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMinView1Label"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMaxView1Label"));

        	
	        //RANDOMIZE TWO OTHERS
	        
	        int[] questionArray = { 2, 3, 4, 5, 6, 7 };
	        NubisMain.shuffleArray(questionArray);
	
	
	        int sel1 = questionArray[0];
	        int sel2 = questionArray[1];
	        
	        if (((NubisApplication)getApplicationContext()).lastMainAlarm != null && ((NubisApplication)getApplicationContext()).lastSubAlarm != null){ //real case
	        	
	          if (((NubisApplication)getApplicationContext()).lastSubAlarm.isMainAlarm()){ //first
	        	  if (((NubisApplication)getApplicationContext()).lastSubAlarm.is30MinuteAlarm()){ //30 minute
	            	  sel1 = ((NubisApplication)getApplicationContext()).lastMainAlarm.questionArray[2];
	            	  sel2 = ((NubisApplication)getApplicationContext()).lastMainAlarm.questionArray[3];
	        	  }
	        	  else { //first!
	            	  sel1 = ((NubisApplication)getApplicationContext()).lastMainAlarm.questionArray[0];
	            	  sel2 = ((NubisApplication)getApplicationContext()).lastMainAlarm.questionArray[1];
	        	  }
	          }
	          else { //last!
	        	  if (((NubisApplication)getApplicationContext()).lastMainAlarmIndex != -1){
	        		  if (((NubisApplication)getApplicationContext()).lastMainAlarmIndex - 4 > 0){
	        			  sel1 = ((NubisApplication)getApplicationContext()).alarms.get(((NubisApplication)getApplicationContext()).lastMainAlarmIndex - 4).alarm.questionArray[4];
	        			  sel2 = ((NubisApplication)getApplicationContext()).alarms.get(((NubisApplication)getApplicationContext()).lastMainAlarmIndex - 4).alarm.questionArray[5];
	        		  }
	         	   }
	          }
	        }
	        //END RANDOMIZATION
	        dynamicQuestions.add(new NubisDynamicQuestion(NubisDynamicQuestion.Q_RADIOBUTTON_SCALE, sel1));
	        dynamicQuestions.get(1).createRadioButtonScale(this, R.id.baseLinearLayout, ((NubisApplication)getApplicationContext()).settings.texts.getText("question" + Integer.toString(sel1) + "Message"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMinView" + Integer.toString(sel1) + "Label"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMaxView" + Integer.toString(sel1) + "Label"));
	        dynamicQuestions.add(new NubisDynamicQuestion(NubisDynamicQuestion.Q_RADIOBUTTON_SCALE, sel2));
	        dynamicQuestions.get(2).createRadioButtonScale(this, R.id.baseLinearLayout, ((NubisApplication)getApplicationContext()).settings.texts.getText("question" + Integer.toString(sel2) + "Message"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMinView" + Integer.toString(sel2) + "Label"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMaxView" + Integer.toString(sel2) + "Label"));
        }
        else {
            dynamicQuestions.add(new NubisDynamicQuestion(NubisDynamicQuestion.Q_RADIOBUTTON_SCALE, 1));
            dynamicQuestions.get(0).createSliderScale(this, R.id.baseLinearLayout, ((NubisApplication)getApplicationContext()).settings.texts.getText("question1Message"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMinView1Label"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMaxView1Label"));
            dynamicQuestions.add(new NubisDynamicQuestion(NubisDynamicQuestion.Q_RADIOBUTTON_SCALE, 2));
            dynamicQuestions.get(1).createSliderScale(this, R.id.baseLinearLayout, ((NubisApplication)getApplicationContext()).settings.texts.getText("question2Message"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMinView2Label"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMaxView2Label"));
            dynamicQuestions.add(new NubisDynamicQuestion(NubisDynamicQuestion.Q_RADIOBUTTON_SCALE, 3));
            dynamicQuestions.get(2).createSliderScale(this, R.id.baseLinearLayout, ((NubisApplication)getApplicationContext()).settings.texts.getText("question3Message"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMinView3Label"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMaxView3Label"));
            dynamicQuestions.add(new NubisDynamicQuestion(NubisDynamicQuestion.Q_RADIOBUTTON_SCALE, 4));
            dynamicQuestions.get(3).createSliderScale(this, R.id.baseLinearLayout, ((NubisApplication)getApplicationContext()).settings.texts.getText("question4Message"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMinView4Label"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMaxView4Label"));
            dynamicQuestions.add(new NubisDynamicQuestion(NubisDynamicQuestion.Q_RADIOBUTTON_SCALE, 5));
            dynamicQuestions.get(4).createSliderScale(this, R.id.baseLinearLayout, ((NubisApplication)getApplicationContext()).settings.texts.getText("question5Message"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMinView5Label"), ((NubisApplication)getApplicationContext()).settings.texts.getText("rangeMaxView5Label"));
        	
        }
        
        addButton();
        //////END DYNAMIC
        
        
    }
	
	private void addQuestion(String questionText){
		LinearLayout row2 = (LinearLayout) findViewById(R.id.baseLinearLayout);

	    LinearLayout question1 = new LinearLayout(this);
	    
	    RelativeLayout.LayoutParams lp = (LayoutParams) row2.getLayoutParams();
	    lp.width= RelativeLayout.LayoutParams.WRAP_CONTENT;
	    question1.setLayoutParams(lp);
	    
	    //Question text
	    TextView questiontext = new TextView(this);
	    questiontext.setPadding(0, 20, 0, 5);
	    questiontext.setText(questionText);
	    questiontext.setTextColor(getResources().getColor(R.color.NubisBlue));
	    questiontext.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics())); 
	    question1.addView(questiontext);
	    //End question text
	    LinearLayout answer1 = new LinearLayout(this);
	    answer1.setLayoutParams(lp);
	    //RANGE MIN
	    TextView rangeMin = new TextView(this);
	    rangeMin.setText("not at all");
	    rangeMin.setTextColor(getResources().getColor(R.color.NubisGray));
	    rangeMin.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics())); 
	    answer1.addView(rangeMin);
	    //RADIO GROUP!
	    RadioGroup answer1group = new RadioGroup(this);
	    answer1group.setOrientation(RadioGroup.HORIZONTAL);
	    RadioButton radio1 = new RadioButton(this);
	    radio1.setTag("1");
	    answer1group.addView(radio1);
	    RadioButton radio2 = new RadioButton(this);
	    radio2.setTag("2");
	    answer1group.addView(radio2);
	    RadioButton radio3 = new RadioButton(this);
	    radio3.setTag("3");
	    answer1group.addView(radio3);
	    RadioButton radio4 = new RadioButton(this);
	    radio4.setTag("4");
	    answer1group.addView(radio4);
        answer1.addView(answer1group);	    
	    //RANGE MAX	    
	    TextView rangeMax = new TextView(this);
	    rangeMax.setText("very much");
	    rangeMax.setTextColor(getResources().getColor(R.color.NubisGray));
	    rangeMax.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics())); 
	    answer1.addView(rangeMax);
	    
	    row2.addView(question1);
	    row2.addView(answer1);    
	    	
		
		
	}
	
	private void addButton(){
		LinearLayout row2 = (LinearLayout) findViewById(R.id.baseLinearLayout);
	    LinearLayout savePane = new LinearLayout(this);
	    saveButton = new Button(this);
	    saveButton.setText("Save responses");
	    saveButton.setTextColor(getResources().getColor(R.color.NubisWhite));
	    saveButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.nubis_buttons));
        saveButton.setPadding(25, 25, 25, 25);
        row2.addView(saveButton);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)saveButton.getLayoutParams();    
        params.gravity = Gravity.CENTER;
        params.setMargins(0, 20, 0, 0);
        saveButton.setLayoutParams(params);
        saveButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
        	 dispatchSaveIntent();
           }
        });
        
	}

    private void setTextViewMessage(int id, String text){
    	final TextView textview = (TextView) findViewById(id);
    	textview.setText(text);
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.nubis_questions, menu);
        return true;
    }
    
    public void dispatchSaveIntent() {
    	try {
    		
    		double answer1 = dynamicQuestions.get(0).getAnswer();
    		double answer2 = dynamicQuestions.get(1).getAnswer();
    		double answer3 = dynamicQuestions.get(2).getAnswer();

    		double answer4 = 0;
    		double answer5 = 0;
    		if (!((NubisApplication)getApplicationContext()).settings.randomizeQuestions){
        		answer4 = dynamicQuestions.get(3).getAnswer();
        		answer5 = dynamicQuestions.get(4).getAnswer();
    		}
    		
    	    if (answer1 == -1 || answer2 == -1 || answer3 == -1 || answer4 == -1 || answer5 == -1){
		        Toast.makeText(this.getBaseContext(), ((NubisApplication)getApplicationContext()).settings.texts.getText("pleaseAnswerAllQuestionsMessage"), Toast.LENGTH_LONG).show();
    	    }
    	    else { //send    	
    	    	saveButton.setEnabled(false);
		        Toast.makeText(this.getBaseContext(), ((NubisApplication)getApplicationContext()).settings.texts.getText("savingResponsesMessage"), Toast.LENGTH_LONG).show();
    	    	if (testMode == 0){ //not test mode!
	
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
	
					NubisDelayedAnswer delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_GET_READ);
					delayedanswer.addGetParameter("id", Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
					delayedanswer.addGetParameter("version", this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
					delayedanswer.addGetParameter("rtid", ((NubisApplication)getApplicationContext()).settings.getRtid());
					delayedanswer.addGetParameter("ceid", ceid);
					delayedanswer.addGetParameter("alarmtype", alarmtype);
	 			    delayedanswer.addGetParameter("subalarmtype", subalarmtype);
					delayedanswer.addGetParameter("phonets", NubisMain.showDateTime(now));
					delayedanswer.addGetParameter("p", "questions");
					
					delayedanswer.addGetParameter("a" + Integer.toString(dynamicQuestions.get(0).getQuestionNumber()), String.format("%.2f", answer1));
					delayedanswer.addGetParameter("a" + Integer.toString(dynamicQuestions.get(1).getQuestionNumber()), String.format("%.2f", answer2));
					delayedanswer.addGetParameter("a" + Integer.toString(dynamicQuestions.get(2).getQuestionNumber()), String.format("%.2f", answer3));
		    		if (!((NubisApplication)getApplicationContext()).settings.randomizeQuestions){
  					  delayedanswer.addGetParameter("a" + Integer.toString(dynamicQuestions.get(3).getQuestionNumber()), String.format("%.2f", answer4));
	  				  delayedanswer.addGetParameter("a" + Integer.toString(dynamicQuestions.get(4).getQuestionNumber()), String.format("%.2f", answer5));
		    		}  
					
			        ((NubisApplication)getApplicationContext()).communication.addNubisDelayedAnswer(delayedanswer);
			        ((NubisApplication)getApplicationContext()).communication.sendOrStoreLocal(this);
			        handleNextStatus();
    	    	}  
                if (answer1 >= 2.5 || answer2 >= 2.5 || answer3 >= 2.5 || answer4 >= 2.5 || answer5 >= 2.5){ //check answers? any 2 or 3: then go to open ended follow up screen!
                  ArrayList<String> labels = new ArrayList<String>();
                  if (answer1 >= 2.5)
                	labels.add(((NubisApplication)getApplicationContext()).settings.texts.getText("question" + Integer.toString(dynamicQuestions.get(0).getQuestionNumber()) + "Label"));
                  if (answer2 >= 2.5)
                	  labels.add(((NubisApplication)getApplicationContext()).settings.texts.getText("question" + Integer.toString(dynamicQuestions.get(1).getQuestionNumber()) + "Label"));
                  if (answer3 >= 2.5)
                	  labels.add(((NubisApplication)getApplicationContext()).settings.texts.getText("question" + Integer.toString(dynamicQuestions.get(2).getQuestionNumber()) + "Label"));
                  if (answer4 >= 2.5)
                	  labels.add(((NubisApplication)getApplicationContext()).settings.texts.getText("question" + Integer.toString(dynamicQuestions.get(3).getQuestionNumber()) + "Label"));
                  if (answer5 >= 2.5)
                	  labels.add(((NubisApplication)getApplicationContext()).settings.texts.getText("question" + Integer.toString(dynamicQuestions.get(4).getQuestionNumber()) + "Label"));
                  
                  String questionText = "";
            	  for (int i = 0; i < labels.size(); i++) {
            		if (i > 0 && i < labels.size() - 1){
            			questionText += ", ";
            		}
            		else if (i == labels.size() - 1 && labels.size() > 1){
            			questionText += " and ";
           			
            		}
            		questionText += labels.get(i);              		  
            	  }
                	
                  Intent intent2 = new Intent(this, NubisOpenEnded.class);
			      intent2.putExtra("test", testMode);
			      intent2.putExtra("questionText", ((NubisApplication)getApplicationContext()).settings.texts.getText("openEndedNegativeQuestion") + " " + questionText + "?");
			      startActivity(intent2);
                }
                else if (((NubisApplication)getApplicationContext()).lastMainAlarm != null && ((NubisApplication)getApplicationContext()).lastMainAlarm.isEveningAlarm() && (answer1 <= 2 || answer2 <= 2 || answer3 <= 2 || answer4 <= 2 || answer5 <= 2)){ //check answers? any 1 or 2 and evening?: then go to open ended follow up screen!
		          String text = "";
                  Intent intent2 = new Intent(this, NubisOpenEnded.class);
			      intent2.putExtra("test", testMode);
			      intent2.putExtra("questionText", ((NubisApplication)getApplicationContext()).settings.texts.getText("openEndedPositiveQuestion"));
			      startActivity(intent2);
                }
                else {//back to main screen
                  if (testMode == 1){
      	    	    final Intent intent2 = new Intent(this, NubisSettings.class);
    	    	    startActivity(intent2); 
                  }
                  else {  
		            NavUtils.navigateUpFromSameTask(this);
                  }
                }
    	    }
		        
	    }
        catch (Exception e) {
          e.printStackTrace();
        }
    }
    
    @Override
    public void onBackPressed() {
    	
    }
    
    public void handleNextStatus(){
      try {
    	  
      	//REMOVE WINDOW CLOSE FIRST

          int parentAlarm = ((NubisApplication)getApplicationContext()).lastMainAlarmIndex;		        	
    	  
    	  //REMOVE EXTRA ALARMS CORTISOL/QUESTIONS!
    	  if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_CORTISOL ||
    			  ((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS      ){
              removeAlarm(parentAlarm);
              removeAlarm(parentAlarm+1);
              removeAlarm(parentAlarm+2);
    	  }
    	  else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_CORTISOL_ALARM_1 ||
    			  ((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS_ALARM_1 ){
              removeAlarm(parentAlarm+1);
              removeAlarm(parentAlarm+2);
    	  }
    	  else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_CORTISOL_ALARM_2 ||
    			  ((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS_ALARM_2  ){
              removeAlarm(parentAlarm+2);
    	  }    	  
          //REMOVE EXTRA IN CASE OF 10 minute delay windows 2
    	  else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_CORTISOL_ALARM_1 ||
    			  ((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_QUESTIONS_ALARM_1 ){
              removeAlarm(parentAlarm+1);
              removeAlarm(parentAlarm+2);
    	  }
    	  else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_CORTISOL_ALARM_2 ||
    			  ((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_QUESTIONS_ALARM_2  ){
              removeAlarm(parentAlarm+2);
    	  }    	  
   	  
    	  //EVENING
    	  else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_CORTISOL ||
    			  ((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_QUESTIONS ){
              removeAlarm(parentAlarm);
              removeAlarm(parentAlarm+1);
    	  }
    	  else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_CORTISOL_ALARM ||
    			  ((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_QUESTIONS_ALARM  ){
              removeAlarm(parentAlarm+1);
    	  }    	  
    	  
          //SAVE SETTINGS!
    	  ((NubisApplication)getApplicationContext()).saveSettings();

    	  
    	  
		    //SEND NEW UPDATE FROM CORTISOL!
		    if ((((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_CORTISOL) ||
		        (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_CORTISOL_ALARM_1) ||
		        (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_CORTISOL_ALARM_2)){
		      ((NubisApplication)getApplicationContext()).status = NubisAlarm.S_MORNING_WINDOW_1_CORTISOL_CLOSED;
		    }
		    //SEND NEW UPDATE FROM CORTISOL EVENING!
		    else if ((((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_CORTISOL) ||
			        (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_CORTISOL_ALARM)){
			  ((NubisApplication)getApplicationContext()).status = NubisAlarm.S_EVENING_WINDOW_3_CORTISOL_CLOSED;
			} 
		    //SEND NEW UPDATE FROM CORTISOL MORNING 30 MINUTE!
		    else if ((((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_CORTISOL_ALARM_1) ||
		    		(((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_CORTISOL_ALARM_2)){
			  ((NubisApplication)getApplicationContext()).status = NubisAlarm.S_MORNING_WINDOW_2_CORTISOL_CLOSED;
			} 	    
		    
		    //SEND NEW UPDATE FROM QUESTIONS!
		    else if ((((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS) ||
		            (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS_ALARM_1) ||
		            (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS_ALARM_2)){
		          ((NubisApplication)getApplicationContext()).status = NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS_CLOSED;
	        }
		    //SEND NEW UPDATE FROM QUESTIONS MORNING 30 MINUTE!
		    else if ((((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_QUESTIONS_ALARM_1) ||
		    		(((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_2_QUESTIONS_ALARM_2)){
			  ((NubisApplication)getApplicationContext()).status = NubisAlarm.S_MORNING_WINDOW_2_QUESTIONS_CLOSED;
			} 	    
		    
		    
		    
		    //SEND NEW UPDATE FROM QUESTIONS EVENING!
		    else if ((((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_QUESTIONS) ||
			        (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_QUESTIONS_ALARM)){
			  ((NubisApplication)getApplicationContext()).status = NubisAlarm.S_EVENING_WINDOW_3_QUESTIONS_CLOSED;
			} 	    
		    
		    //SET TO ANSWERED! Add 30 minute alarm
		    if (((NubisApplication)getApplicationContext()).lastMainAlarm != null){

	    		//IT IS COMPLETED HERE!! OTHERWISE WE WON'T BE HERE!!
	    		if ( ((NubisApplication)getApplicationContext()).lastMainAlarm.isMorningAlarm()){ //morning alarm
	    			 ((NubisApplication)getApplicationContext()).lastMainAlarm.answered++;
	    		}
	    		else {  //evening
	    			 ((NubisApplication)getApplicationContext()).lastMainAlarm.answered2++;
	    		}
		    		    
		        if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_CORTISOL_CLOSED){
   		    	    //FIRST CORTISOL ALARM DONE.. SET NEXT ONE IN 30 MINUTES FROM NOW
		        	//SET 30 MINUTE ONE
  		    	    int index = ((NubisApplication)getApplicationContext()).alarms.size();
		    		Calendar now = Calendar.getInstance();
		    		now.setTimeInMillis(System.currentTimeMillis());
		    		now.add(Calendar.MINUTE,  ((NubisApplication)getApplicationContext()).lastMainAlarm.reminder30minute);
		    		now.set(Calendar.SECOND, 3); //extra alarm
			    	addNubisAlarm(index++, new NubisAlarm(now, ((NubisApplication)getApplicationContext()).lastMainAlarmIndex, -1, NubisAlarm.S_MORNING_WINDOW_2_CORTISOL_ALARM_1, 0, 0, true, true));

			    	//add extra 10 minutes reminder
		    		Calendar now3 = Calendar.getInstance();
		    		now3.setTimeInMillis(System.currentTimeMillis());
		    		now3.add(Calendar.MINUTE,  ((NubisApplication)getApplicationContext()).lastMainAlarm.reminder30minute + ((NubisApplication)getApplicationContext()).lastMainAlarm.window2ndReminder);
		    		now3.set(Calendar.SECOND, 3); //extra alarm
			    	addNubisAlarm(index++, new NubisAlarm(now3, ((NubisApplication)getApplicationContext()).lastMainAlarmIndex, -1, NubisAlarm.S_MORNING_WINDOW_2_CORTISOL_ALARM_2, 0, 0, true, true));
			    	
			    	//close window
			    	Calendar now2 = Calendar.getInstance();
			    	now2.setTimeInMillis(System.currentTimeMillis());
		    		now2.add(Calendar.MINUTE,  ((NubisApplication)getApplicationContext()).lastMainAlarm.reminder30minuteClose);
		    		now2.set(Calendar.SECOND, 3);
		    		addNubisAlarm(index++, new NubisAlarm(now2, ((NubisApplication)getApplicationContext()).lastMainAlarmIndex, -1, NubisAlarm.S_MORNING_WINDOW_2_CORTISOL_CLOSED, 0, 0, false, true));
		    	}	 
		        //QUESTIONS ONLY!
		        else if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_MORNING_WINDOW_1_QUESTIONS_CLOSED){
   		    	    //FIRST CORTISOL ALARM DONE.. SET NEXT ONE IN 30 MINUTES FROM NOW
		        	//SET 30 MINUTE ONE
  		    	    int index = ((NubisApplication)getApplicationContext()).alarms.size();
		    		Calendar now = Calendar.getInstance();
		    		now.setTimeInMillis(System.currentTimeMillis());
		    		now.add(Calendar.MINUTE,  ((NubisApplication)getApplicationContext()).lastMainAlarm.reminder30minute);
		    		now.set(Calendar.SECOND, 3); //extra alarm
			    	addNubisAlarm(index++, new NubisAlarm(now, ((NubisApplication)getApplicationContext()).lastMainAlarmIndex, -1, NubisAlarm.S_MORNING_WINDOW_2_QUESTIONS_ALARM_1, 0, 0, true, true));

			    	//add extra 10 minutes reminder
		    		Calendar now3 = Calendar.getInstance();
		    		now3.setTimeInMillis(System.currentTimeMillis());
		    		now3.add(Calendar.MINUTE,  ((NubisApplication)getApplicationContext()).lastMainAlarm.reminder30minute + ((NubisApplication)getApplicationContext()).lastMainAlarm.window2ndReminder);
		    		now3.set(Calendar.SECOND, 3); //extra alarm
			    	addNubisAlarm(index++, new NubisAlarm(now3, ((NubisApplication)getApplicationContext()).lastMainAlarmIndex, -1, NubisAlarm.S_MORNING_WINDOW_2_QUESTIONS_ALARM_2, 0, 0, true, true));
			    	
			    	//close window
			    	Calendar now2 = Calendar.getInstance();
			    	now2.setTimeInMillis(System.currentTimeMillis());
		    		now2.add(Calendar.MINUTE,  ((NubisApplication)getApplicationContext()).lastMainAlarm.reminder30minuteClose);
		    		now2.set(Calendar.SECOND, 3);
		    		addNubisAlarm(index++, new NubisAlarm(now2, ((NubisApplication)getApplicationContext()).lastMainAlarmIndex, -1, NubisAlarm.S_MORNING_WINDOW_2_QUESTIONS_CLOSED, 0, 0, false, true));
		    	}			        
		        
		    }
		    
		    //CHECK TO SEE IF WE HAD 9 ALARMS WHEN EVENING ALARM. IF SO: REMOVE ALL OTHER ALARMS
		    if (((NubisApplication)getApplicationContext()).status == NubisAlarm.S_EVENING_WINDOW_3_CORTISOL_CLOSED){
  		   	   if (((NubisApplication)getApplicationContext()).settings.numberOfCortisol >= ((NubisApplication)getApplicationContext()).settings.cortisolTotalMeasures){
  		   		   //remove all alarms
					for (int i = ((NubisApplication)getApplicationContext()).lastMainAlarmIndex; i < ((NubisApplication)getApplicationContext()).alarms.size(); i++) { 
						removeAlarm(i);
					}
  		   	   }
		    }
	    }     	
	    catch (Exception e) {
	      e.printStackTrace();
	    }
	    
    }
    
    public void removeAlarm(int index){
      try {
		    NubisAlarm removeAlarm = ((NubisApplication)getApplicationContext()).alarms.get(index).alarm;	
		   ((NubisApplication)getApplicationContext()).log += "\ndelete: " + index + ": " + NubisMain.showDate(removeAlarm.getCalendar());
		   ((NubisApplication)getApplicationContext()).alarms.get(index).CancelAlarm();
		   ((NubisApplication)getApplicationContext()).alarms.get(index).alarm.active = false;
		   ((NubisApplication)getApplicationContext()).saveSettings();
      }	   
	  catch (Exception e) {
		   e.printStackTrace();
      }

	   //SAVE this so when reload, they won't be active anymore!!
	   
    }
    
	public void addNubisAlarm(int i, NubisAlarm alarm){
	    NubisAlarmReceiver test1 = new NubisAlarmReceiver();
		test1.SetAlarm(this, ((NubisApplication)getApplicationContext()).alarmMgr, i, alarm, new Handler() {
					 public void handleMessage(Message msg) {
						 ((NubisApplication)getApplicationContext()).log += "\nhandlemessage";
                       } } );
		((NubisApplication)getApplicationContext()).alarms.add(test1);
	}
    
    
	public void onResume() {
	    super.onResume();
	    ((NubisApplication)getApplicationContext()).mainScreenActive = false;
	}
    
    
}
