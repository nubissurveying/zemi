package com.nubisZemi;

import java.io.Serializable;
import java.util.Calendar;

public class NubisAlarm implements Serializable {

	public static final int S_NOTHING = 0;
	public static final int S_MORNING_WINDOW_1_CORTISOL = 1;
	public static final int S_MORNING_WINDOW_1_CORTISOL_ALARM_1 = 2;
	public static final int S_MORNING_WINDOW_1_CORTISOL_ALARM_2 = 3;
	public static final int S_MORNING_WINDOW_1_CORTISOL_CLOSED = 4;

	public static final int S_MORNING_WINDOW_2_CORTISOL_ALARM_1 = 5;
	public static final int S_MORNING_WINDOW_2_CORTISOL_ALARM_2 = 6;
	public static final int S_MORNING_WINDOW_2_CORTISOL_CLOSED = 7;

	public static final int S_EVENING_WINDOW_3_CORTISOL = 8;
	public static final int S_EVENING_WINDOW_3_CORTISOL_ALARM = 9;
	public static final int S_EVENING_WINDOW_3_CORTISOL_CLOSED = 10;
	
	
	public static final int S_MORNING_WINDOW_1_QUESTIONS = 101;
	public static final int S_MORNING_WINDOW_1_QUESTIONS_ALARM_1 = 102;
	public static final int S_MORNING_WINDOW_1_QUESTIONS_ALARM_2 = 103;
	public static final int S_MORNING_WINDOW_1_QUESTIONS_CLOSED = 104;

	public static final int S_MORNING_WINDOW_2_QUESTIONS_ALARM_1 = 105;
	public static final int S_MORNING_WINDOW_2_QUESTIONS_ALARM_2 = 106;
	public static final int S_MORNING_WINDOW_2_QUESTIONS_CLOSED = 107;
	
	public static final int S_EVENING_WINDOW_3_QUESTIONS = 108;
	public static final int S_EVENING_WINDOW_3_QUESTIONS_ALARM = 109; 
	public static final int S_EVENING_WINDOW_3_QUESTIONS_CLOSED = 110;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Calendar calendar;
	private int alarmtype;
	public int answered = 0;
	public int answered2 = 0;
	public boolean active;
	public boolean alert;
	public int parentid = -1;
	public int ceid = -1;  //link to the schedule id!

	
	/*
	public int windowOpen = -2;
	public int window2ndReminder = 4;
	public int windowClose = 6;
	public int reminder30minute = 2;
	public int reminder30minuteClose = 4;
	*/

	
	public int windowOpen = -60;
	public int window2ndReminder = 10;
	public int windowClose = 30;
	public int reminder30minute = 30;
	public int reminder30minuteClose = 60;
  
	public int[] questionArray = { 2, 3, 4, 5, 6, 7 };
	
	
	public NubisAlarm(Calendar setCalendar, int parentid, int ceid, int type, int Answered, int Answered2, boolean alert, boolean active){
	  this.calendar = setCalendar;
	  alarmtype = type;
	  answered = Answered;
	  answered2 = Answered2;
	  this.active = active;
	  this.alert = alert;
	  this.parentid = parentid;
	  this.ceid = ceid;
	  NubisMain.shuffleArray(questionArray);
	}
	
	
	public NubisAlarm(int year, int month, int day, int hour, int minute, int parentid, int ceid, int type, int Answered, int Answered2, boolean alert, boolean active){
		calendar  = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1); //minus one.. month is zero based in Android
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 1);
		alarmtype = type;
		answered = Answered;
		answered2 = Answered2;

		this.active = active;
		this.alert = alert;
		this.parentid = parentid;
		this.ceid = ceid;
	}

	public Calendar getCalendar(){
		return this.calendar;
	}
	
	public Calendar getCopyCalendar(){
		Calendar time = Calendar.getInstance();
		time.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
		time.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
		time.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
		time.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
		time.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
		time.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
        return time;		
	}
	
	public Calendar getCopyCalendar(int addMinutes){
		Calendar time = this.getCopyCalendar();
		time.add(Calendar.MINUTE, addMinutes);
		return time;
	}
	
	
	public int getAlarmType(){
		return this.alarmtype;
	}
	
	public int getAnswered(){
		return this.answered;
	}

	public int getAnswered2(){
		return this.answered2;
	}
	
	public void setAnswered(int ans){
		this.answered = ans;
	}
	
	public String showIsMain(){
		if (this.parentid == -1){
			return "*";
		}
        return "";		
	}
	
	   public boolean isNewSessionAlarm(){
			return (this.alarmtype == S_MORNING_WINDOW_1_CORTISOL ||
					this.alarmtype == S_EVENING_WINDOW_3_CORTISOL ||
					
					this.alarmtype == S_MORNING_WINDOW_1_QUESTIONS ||
					this.alarmtype == S_EVENING_WINDOW_3_QUESTIONS);	   
		   
	   }

	   public boolean isNewSessionMainAlarm(){
			return (this.alarmtype == S_MORNING_WINDOW_1_CORTISOL_ALARM_1 ||
					this.alarmtype == S_EVENING_WINDOW_3_CORTISOL_ALARM ||
					
					this.alarmtype == S_MORNING_WINDOW_1_QUESTIONS_ALARM_1 ||
					this.alarmtype == S_EVENING_WINDOW_3_QUESTIONS_ALARM);	   
		   
	   }
    
	   public boolean isCortisolAlarm(){
			return (this.alarmtype == S_MORNING_WINDOW_1_CORTISOL ||
					this.alarmtype == S_MORNING_WINDOW_1_CORTISOL_ALARM_1 ||
					this.alarmtype == S_MORNING_WINDOW_1_CORTISOL_ALARM_2 ||
					this.alarmtype == S_MORNING_WINDOW_1_CORTISOL_CLOSED ||
										
					this.alarmtype == S_MORNING_WINDOW_2_CORTISOL_ALARM_1 ||
					this.alarmtype == S_MORNING_WINDOW_2_CORTISOL_ALARM_2 ||
					this.alarmtype == S_MORNING_WINDOW_2_CORTISOL_CLOSED ||

					this.alarmtype == S_EVENING_WINDOW_3_CORTISOL ||
					this.alarmtype == S_EVENING_WINDOW_3_CORTISOL_ALARM ||
					this.alarmtype == S_EVENING_WINDOW_3_CORTISOL_CLOSED);
	   }
	   
	   public boolean isMainAlarm(){
			return (this.alarmtype == S_MORNING_WINDOW_1_CORTISOL_ALARM_1 ||
					this.alarmtype == S_MORNING_WINDOW_1_CORTISOL_ALARM_2 ||
					this.alarmtype == S_MORNING_WINDOW_2_CORTISOL_ALARM_1 ||
				    this.alarmtype == S_MORNING_WINDOW_2_CORTISOL_ALARM_2 ||
					this.alarmtype == S_EVENING_WINDOW_3_CORTISOL_ALARM ||
					
					this.alarmtype == S_MORNING_WINDOW_1_QUESTIONS_ALARM_1 ||
					this.alarmtype == S_MORNING_WINDOW_1_QUESTIONS_ALARM_2 ||
					this.alarmtype == S_MORNING_WINDOW_2_QUESTIONS_ALARM_1 ||
					this.alarmtype == S_MORNING_WINDOW_2_QUESTIONS_ALARM_2 ||
					this.alarmtype == S_EVENING_WINDOW_3_QUESTIONS_ALARM);
	   }
	
	   public boolean isMorningAlarm(){
			return (this.alarmtype == S_MORNING_WINDOW_1_CORTISOL_ALARM_1 ||
					this.alarmtype == S_MORNING_WINDOW_1_CORTISOL_ALARM_2 ||
					this.alarmtype == S_MORNING_WINDOW_1_QUESTIONS_ALARM_1 ||
					this.alarmtype == S_MORNING_WINDOW_1_QUESTIONS_ALARM_2);
	   }
	   
	   public boolean is30MinuteAlarm(){
			return (this.alarmtype == S_MORNING_WINDOW_2_CORTISOL_ALARM_1 ||
					this.alarmtype == S_MORNING_WINDOW_2_CORTISOL_ALARM_2 ||
					this.alarmtype == S_MORNING_WINDOW_2_QUESTIONS_ALARM_1 ||
					this.alarmtype == S_MORNING_WINDOW_2_QUESTIONS_ALARM_2);
		   
	   }
	   
	   public boolean isEveningAlarm(){
		  return !(this.isMorningAlarm()); 
	   }
	
}
