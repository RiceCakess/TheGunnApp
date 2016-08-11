package xyz.dchen.thegunnapp;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by David on 7/19/2016.
 */
public class EventItem {
    String summary,description;
    Date date;
    public EventItem(String name, String description, Date date){

        this.summary = name;
        this.description = description;
        this.date = date;

    }
    public String getTimeString(){
        DateFormat df = new SimpleDateFormat("hh:mm a");
        if(!df.format(date).equals("12:00 AM"))
            return df.format(date);
        return "";
    }
    public String getDateString(){
        DateFormat df = new SimpleDateFormat("MMMM dd");
        return df.format(date);
    }

    public static  ArrayList<EventItem> convertAll(ArrayList<JSONObject> events){
        ArrayList<EventItem> converted = new ArrayList<EventItem>();
        //parsing json data from google calendar to array of eventitems
        for(JSONObject obj : events){
            try{
                String description = obj.has("description") ? obj.getString("description") : "";
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                Date time = MainActivity.date;
                if(obj.getJSONObject("start").has("date")){
                    time = dateFormat.parse(obj.getJSONObject("start").getString("date"));
                } else if(obj.getJSONObject("start").has("dateTime")){
                    time = dateTimeFormat.parse(obj.getJSONObject("start").getString("dateTime"));
                }

                converted.add(new EventItem(obj.getString("summary"),description,time));
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        return converted;
    }
}