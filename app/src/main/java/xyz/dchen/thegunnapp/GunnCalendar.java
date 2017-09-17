package xyz.dchen.thegunnapp;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by David on 7/19/2016.
 */
public class GunnCalendar {
    static String[] defaultSchedule = {
            "No School!",
            "Period A (8:25-9:45)\nBrunch (9:45-10:00)\nPeriod B (10:00-11:15)\nPeriod C (11:25-12:40)\nLunch (12:40-1:20)\nPeriod F (1:20-2:35)",
            "Period D (8:25-9:45)\nBrunch (9:45-10:00)\nFlexTime (10:00-10:50)\nPeriod E (11:00-12:15)\nLunch (12:15-12:55)\nPeriod A(12:55-2:15) \nPeriod G (2:25-3:40)",
            "Period B (8:25-9:50)\nBrunch (9:50-10:05)\nPeriod C (10:05-11:25)\nPeriod D (11:35-12:55)\nLunch (12:55-1:35)\nPeriod F (1:35-2:55)",
            "Period E (8:25-9:50)\nBrunch (9:50-10:05)\nFlexTime (10:05-11:15)\nPeriod B (11:30-12:35)\nLunch (12:45-1:15)\nPeriod A (1:25-2:35)\nPeriod G (2:45-3:35)",
            "Period C (8:25-9:40)\nBrunch (9:40-9:55)\nPeriod D (9:55-11:05)\nPeriod E (11:15-12:25)\nLunch (12:25-1:05)\nPeriod F (1:05-2:15)\nPeriod G (2:25-3:35)",
            "No School!"

    };
    String API_KEY = "AIzaSyDvy55aMfxGNdtDFSqnZTUPdce4NyF58k0";
    String CALENDAR_ID = "u5mgb2vlddfj70d7frf3r015h0@group.calendar.google.com";
    ArrayList<String> scheduleItems = new ArrayList<String>();
    ArrayList<String> tomorrowscheduleItems = new ArrayList<String>();
    boolean alternate = false;
    boolean tomorrow_alternate = false;
    ArrayList<JSONObject> events = null;
    public GunnCalendar(Activity mActivity){
        new Thread(new Runnable() {
            @Override
            public void run() {

                //retry every 30 seconds to get calendar data
                while (events == null) {
                    try {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar c = Calendar.getInstance();
                        c.setTime(MainActivity.date);
                        c.add(Calendar.DATE, 5);
                        //grab and parse calendar data from a curl request
                        String req = "https://www.googleapis.com/calendar/v3/calendars/" + CALENDAR_ID
                                + "/events?key=" + API_KEY
                                + "&timeMin=" + getISOString(MainActivity.date)
                                + "&timeMax=" + getISOString(c.getTime())
                                + "&maxResults=20&showDeleted=false&singleEvents=true&orderBy=startTime";
                        String json = readUrl(req);
                        events = new ArrayList<JSONObject>();
                        JSONObject jsonObj = new JSONObject(json);
                        JSONArray items = jsonObj.getJSONArray("items");
                        for (int i = 0; i < items.length(); i++) {
                            events.add(items.getJSONObject(i));
                        }
                        Thread.sleep(30*1000);
                    } catch (Exception e) {
                        System.out.println("Failed to grab calendar");
                        events = null;
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public void checkForEvents(final Runnable callback){
        //loop to keep waiting for the json request thread to complete
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (events == null){
                    try{
                        Thread.sleep(200);
                     }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }

                //run callback
                if(events !=null){
                    callback.run();
                    return;
                }

            }
        }).start();
    }
    public void getSchedule(final Runnable callback){

        new Thread(new Runnable() {
            @Override
            public void run() {
                //loop to keep waiting for the json request thread to complete
                while (events == null){
                    try{
                        Thread.sleep(200);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                try {
                    //clear anything from default values
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = MainActivity.date;
                    Calendar c = Calendar.getInstance();
                    c.setTime(MainActivity.date);
                    c.add(Calendar.DATE, 1);
                    Date tomorrow = c.getTime();
                    //loop through all events
                    for (JSONObject scheduleArr : events) {
                        String summary = scheduleArr.getString("summary");
                        String startTime = "";
                        try {
                            startTime = scheduleArr.getJSONObject("start").getString("date");

                        } catch (Exception e) {
                            startTime = scheduleArr.getJSONObject("start").getString("dateTime");
                        }
                        if((summary.toLowerCase().contains("holiday") || summary.toLowerCase().contains("no students")  || summary.toLowerCase().contains("break")) && !summary.toLowerCase().contains("luncheon")){
                            if(startTime.contains(dateFormat.format(date))) {
                                scheduleItems.clear();
                                scheduleItems.add("No School!");
                            }
                            else if(startTime.contains(dateFormat.format(tomorrow))) {
                                tomorrowscheduleItems.clear();
                                tomorrowscheduleItems.add("No School!");
                            }
                            callback.run();
                        }
                        //check if the event is an alternate schedule
                        else if (summary.toLowerCase().contains("schedule") && !summary.toLowerCase().contains("back to school") && (startTime.contains(dateFormat.format(date)) || startTime.contains(dateFormat.format(tomorrow)))) {
                            //add description to schedule item
                            if(startTime.contains(dateFormat.format(date))) {
                                scheduleItems.clear();
                                for (String s : scheduleArr.getString("description").split("\n")) {
                                    scheduleItems.add(s);
                                    alternate = true;
                                }
                            }
                            else if(startTime.contains(dateFormat.format(tomorrow))) {
                                tomorrowscheduleItems.clear();
                                for (String s : scheduleArr.getString("description").split("\n")) {
                                    tomorrowscheduleItems.add(s);
                                    tomorrow_alternate = true;
                                }
                            }
                            if(alternate)
                                callback.run();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //add default schedule regardless of whether live fetch is completed
        Calendar calendar = Calendar.getInstance();
        String schedule = defaultSchedule[calendar.get(Calendar.DAY_OF_WEEK) -1];
        for(String s : schedule.split("\n")){
            scheduleItems.add(s);
            callback.run();
        }
        String tomorrow_schedule = defaultSchedule[calendar.get(Calendar.DAY_OF_WEEK) % 7];
        for(String s : tomorrow_schedule.split("\n")){
            tomorrowscheduleItems.add(s);
        }

    }
    private static String getISOString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }
    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }
}
