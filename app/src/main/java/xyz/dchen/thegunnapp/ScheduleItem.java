package xyz.dchen.thegunnapp;

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

    public static ArrayList<ScheduleItem> convertSchedule(ArrayList<String> schedule){
        ArrayList<ScheduleItem> converted = new ArrayList<ScheduleItem>();
        for(String s : schedule){
            String[] split = s.split("\\(");

            if(split.length == 2){
                converted.add(new ScheduleItem(split[0].trim(), split[1].replaceAll("\\)","")));
            }
        }
        return converted;
    }

    public static String getRowColor(String name){
        String eventName = name.toLowerCase();
        if(eventName.contains("period a"))
            return "#E57373";
        else if(eventName.contains("period b"))
            return "#03A9F4";
        else if(eventName.contains("period c"))
            return "#EF5350";
        else if(eventName.contains("period d"))
            return "#FFEB3B";
        else if(eventName.contains("period e"))
            return "#00BCD4";
        else if(eventName.contains("period f"))
            return "#8BC34A";
        else if(eventName.contains("period g"))
            return "#BA68C8";
        else if(eventName.contains("period h"))
            return "#607D8B";

        return null;
    }
}