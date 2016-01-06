package com.nubisZemi;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NubisDynamicQuestion {

	public static final int Q_RADIOBUTTON_SCALE = 1;
	public static final int Q_OPENENDED = 2;
	
	private int type;
	private int questionNumber;
	private RadioGroup answergroup;
	public List<RadioButton> radioButtons = new ArrayList<RadioButton>();
	public SeekBar seekBar;
	public boolean seekBarClicked = false;
	
	public NubisDynamicQuestion(int type, int questionNumber){
		this.type = type;
		this.questionNumber = questionNumber;
	}
	
	public int getQuestionNumber(){
		return questionNumber;
	}
	
	public LinearLayout createSliderScale(Activity activity, int layoutId, String questionText, String rangeMinText, String rangeMaxText){
		int textSizeQuestion = 8;
		int textSizeRange = 8;
		LinearLayout row2 = (LinearLayout) activity.findViewById(layoutId);
	    RelativeLayout.LayoutParams lp = (LayoutParams) row2.getLayoutParams();
	    lp.width= RelativeLayout.LayoutParams.FILL_PARENT;
	    LinearLayout labels = new LinearLayout(activity);
	    labels.setLayoutParams(lp);
	    //RANGE MIN
	    TextView rangeMin = new TextView(activity);
	    rangeMin.setText(rangeMinText);
	    rangeMin.setTextColor(activity.getResources().getColor(R.color.NubisBlue));
	    rangeMin.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSizeQuestion, activity.getResources().getDisplayMetrics())); 
	    labels.addView(rangeMin);
	    //RANGE MAX	    
	    TextView rangeMax = new TextView(activity);
	    rangeMax.setText(rangeMaxText);
	    rangeMax.setTextColor(activity.getResources().getColor(R.color.NubisBlue));
	    rangeMax.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSizeQuestion, activity.getResources().getDisplayMetrics()));
	    rangeMax.setWidth(2000);
	    rangeMax.setGravity(Gravity.RIGHT);
	    
	    labels.addView(rangeMax);

	    //ADD answercategories
	    LinearLayout answers = new LinearLayout(activity);
	    answers.setLayoutParams(lp);

	    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
	    	     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	layoutParams.setMargins(0, 10, 0, 30);   // was 0,10,0,40
	    
	    seekBar = new SeekBar(activity);
	    

	    
	    
	    //lp.setMargins(0, 100, 0, 0);
	   // RelativeLayout.LayoutParams lp2 = (LayoutParams) answers.getLayoutParams();
	  //  lp2.setMargins(0, 100, 0, 0);
	    seekBar.setLayoutParams(layoutParams);
	    seekBar.setProgressDrawable(new ColorDrawable(R.color.NubisGray));
	    Drawable thumb = seekBar.getThumb();
        thumb.mutate().setAlpha(0);

        // bar.setOnClickListener(mainBtnOnClickListener);
        
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int progressChanged = 0;
 
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				progressChanged = progress;
			}
 
			public void onStartTrackingTouch(SeekBar seekBar) {
			    Drawable thumb = seekBar.getThumb();
				thumb.mutate().setAlpha(255); 
				seekBarClicked = true;
				// TODO Auto-generated method stub
			}
 
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
        	    
//        LayoutParams lp2 = (LayoutParams) seekBar.getLayoutParams();
//        lp2.topMargin = 500;
        
	    answers.addView(seekBar);	  
    
        //add to row2	    
	    row2.addView(labels);   
	    

	    row2.addView(answers);    
		
	    return row2;
	}
	
	
	//4 radio buttons. no questiontexts
	public LinearLayout createRadioButtonScale2(Activity activity, int layoutId, String questionText, String rangeMinText, String rangeMaxText){
		int textSizeQuestion = 10;
		int textSizeRange = 8;
		LinearLayout row2 = (LinearLayout) activity.findViewById(layoutId);
    	
//	    row2.setLayoutParams(lp);
//	    row2.setGravity(Gravity.CENTER);

		
		
	    RelativeLayout.LayoutParams lp = (LayoutParams) row2.getLayoutParams();
	    lp.width= RelativeLayout.LayoutParams.FILL_PARENT;
       // lp.width = 5000;
	    
	    LinearLayout labels = new LinearLayout(activity);
	    labels.setLayoutParams(lp);
	    //RANGE MIN
	    TextView rangeMin = new TextView(activity);
	    rangeMin.setText(rangeMinText);
	    rangeMin.setTextColor(activity.getResources().getColor(R.color.NubisBlue));
	    rangeMin.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSizeQuestion, activity.getResources().getDisplayMetrics())); 
	    labels.addView(rangeMin);
	    //RANGE MAX	    
	    TextView rangeMax = new TextView(activity);
	    rangeMax.setText(rangeMaxText);
	    rangeMax.setTextColor(activity.getResources().getColor(R.color.NubisBlue));
	    rangeMax.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSizeQuestion, activity.getResources().getDisplayMetrics()));
	    
	    
//	    LinearLayout.LayoutParams params = new  LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,  LinearLayout.LayoutParams.FILL_PARENT);
	    
	    
	    //RelativeLayout.LayoutParams lp2 = (LayoutParams) row2.getLayoutParams();
	    //lp2.width= RelativeLayout.LayoutParams.WRAP_CONTENT;
        //lp2.width = 400;

	    
	//    rangeMax.setLayoutParams(lp2);
	    rangeMax.setWidth(2000);
	    rangeMax.setGravity(Gravity.RIGHT);
	    
	    labels.addView(rangeMax);

	    //ADD answercategories
	    LinearLayout answers = new LinearLayout(activity);
	    answers.setLayoutParams(lp);

	    //RADIO GROUP!
	    answergroup = new RadioGroup(activity);
	    answergroup.setOrientation(RadioGroup.HORIZONTAL);
	    
	    
	    RadioButton temp = new RadioButton(activity);
	    temp.setWidth(100);
	    radioButtons.add(temp);
	    radioButtons.get(0).setTag("1");
	    answergroup.addView(radioButtons.get(0));
	    
	    temp = new RadioButton(activity);
	    temp.setWidth(100);
	    radioButtons.add(temp);
	    radioButtons.get(1).setTag("2");
	    answergroup.addView(radioButtons.get(1));
	    
	    temp = new RadioButton(activity);
	    temp.setWidth(100);
	    radioButtons.add(temp);
	    radioButtons.get(2).setTag("3");
	    answergroup.addView(radioButtons.get(2));
	    
	    temp = new RadioButton(activity);
	    temp.setWidth(100);
	    radioButtons.add(temp);
	    radioButtons.get(3).setTag("4");
	    answergroup.addView(radioButtons.get(3));
	    
	    answergroup.setLayoutParams(lp);
	    answergroup.setGravity(Gravity.CENTER);
	    answers.addView(answergroup);	    	    
    
        //add to row2	    
	    row2.addView(labels);   
	    

	    row2.addView(answers);    
		
	    return row2;
	}
	
	public LinearLayout createRadioButtonScale(Activity activity, int layoutId, String questionText, String rangeMinText, String rangeMaxText){
		int textSizeQuestion = 10;
		int textSizeRange = 8;
		
		LinearLayout row2 = (LinearLayout) activity.findViewById(layoutId);
	
	    LinearLayout question1 = new LinearLayout(activity);
	    
	    RelativeLayout.LayoutParams lp = (LayoutParams) row2.getLayoutParams();
	    lp.width= RelativeLayout.LayoutParams.WRAP_CONTENT;
	    question1.setLayoutParams(lp);
	    
	    //Question text
	    TextView questiontext = new TextView(activity);
	    questiontext.setPadding(0, 20, 0, 5);
	    questiontext.setText(questionText);
	    questiontext.setTextColor(activity.getResources().getColor(R.color.NubisBlue));
	    questiontext.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSizeQuestion, activity.getResources().getDisplayMetrics())); 
	    question1.addView(questiontext);
	    //End question text
	    LinearLayout answer1 = new LinearLayout(activity);
	    answer1.setLayoutParams(lp);
	    //RANGE MIN
	    TextView rangeMin = new TextView(activity);
	    rangeMin.setText(rangeMinText);
	    rangeMin.setTextColor(activity.getResources().getColor(R.color.NubisGray));
	    rangeMin.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSizeRange, activity.getResources().getDisplayMetrics())); 
	    answer1.addView(rangeMin);
	    //RADIO GROUP!
	    answergroup = new RadioGroup(activity);
	    answergroup.setOrientation(RadioGroup.HORIZONTAL);
	    radioButtons.add(new RadioButton(activity));
	    radioButtons.get(0).setTag("1");
	    answergroup.addView(radioButtons.get(0));
	    
	    radioButtons.add(new RadioButton(activity));
	    radioButtons.get(1).setTag("2");
	    answergroup.addView(radioButtons.get(1));
	    
	    radioButtons.add(new RadioButton(activity));
	    radioButtons.get(2).setTag("3");
	    answergroup.addView(radioButtons.get(2));
	    
	    radioButtons.add(new RadioButton(activity));
	    radioButtons.get(3).setTag("4");
	    answergroup.addView(radioButtons.get(3));
	    
	    answer1.addView(answergroup);	    
	    //RANGE MAX	    
	    TextView rangeMax = new TextView(activity);
	    rangeMax.setText(rangeMaxText);
	    rangeMax.setTextColor(activity.getResources().getColor(R.color.NubisGray));
	    rangeMax.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSizeRange, activity.getResources().getDisplayMetrics())); 
	    answer1.addView(rangeMax);
	    
	    row2.addView(question1);
	    row2.addView(answer1);    
	    return row2;
	}
	
	public double getAnswer(){
		for (RadioButton rbutton : radioButtons) {
          if (rbutton.isChecked()){
        	  return radioButtons.indexOf(rbutton) + 1;
          }
		}
		if (seekBar != null){
			if (seekBarClicked){ //has been clicked?
			  return (((double) seekBar.getProgress() / 100) * 3) + 1;
			}
		}
		return -1;
	}
}
