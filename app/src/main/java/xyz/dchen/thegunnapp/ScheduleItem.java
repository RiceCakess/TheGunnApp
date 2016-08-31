package xyz.dchen.thegunnapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by David on 7/19/2016.
 */
public class ScheduleItem{
    String name,time;
    public ScheduleItem(String name, String time){
        this.name = name;
        this.time = time;
    }
    public String getTimeString(){
        return time;
    }

    //convert string schedule to scheduleItem schedule
    public static ArrayList<ScheduleItem> convertSchedule(ArrayList<String> schedule){
        ArrayList<ScheduleItem> converted = new ArrayList<ScheduleItem>();
        SharedPreferences sharedPref = MainActivity.sharedPref;
        for(String s : schedule){
            String[] split = s.split("\\(");
            if(split.length == 2){
                //flip string to correct format
                String editTextName = split[0].trim();
                editTextName = editTextName.substring(editTextName.length()-1) + editTextName.substring(0,editTextName.length()-1);
                editTextName = editTextName.toLowerCase().trim();
                String name =  split[0].trim();
                if(sharedPref.contains(editTextName)){
                    name = sharedPref.getString(editTextName,null);
                }
                converted.add(new ScheduleItem(name, split[1].replaceAll("\\)","")));
            }
        }

        return converted;
    }
}