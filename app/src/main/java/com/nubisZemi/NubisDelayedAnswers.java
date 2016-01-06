package com.nubisZemi;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class NubisDelayedAnswers {

    public List<NubisDelayedAnswer> delayedAnswers = new ArrayList<NubisDelayedAnswer>();
    
    public int getDelayedAnswerCount(){
    	return this.delayedAnswers.size();
    }   
       
    public NubisDelayedAnswer getDelayedAnswer(int index){
        if (index < this.delayedAnswers.size()){
      	  return this.delayedAnswers.get(index);
        }
        return null;
    }
    
    public void addDelayedAnswer(NubisDelayedAnswer answer){
    	this.delayedAnswers.add(answer);
    }
    
    public void deleteDelayedAnswer(int index){
    	this.delayedAnswers.remove(index);
    }
    
    public String serialize() {
        // Serialize this class into a JSON string using GSON
        Gson gson = new Gson();
        return gson.toJson(this);
    }
 
    static public NubisDelayedAnswers create(String serializedData) {
        // Use GSON to instantiate this class using the JSON representation of the state
        Gson gson = new Gson();
        return gson.fromJson(serializedData, NubisDelayedAnswers.class);
    }
	
}
