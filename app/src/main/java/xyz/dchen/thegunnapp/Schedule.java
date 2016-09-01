package xyz.dchen.thegunnapp;

/**
 * Created by David on 7/18/2016.
 */
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Schedule extends Fragment {

    MainActivity mActivity;
    ArrayList<ScheduleItem> scheduleItems = new ArrayList<ScheduleItem>();
    ListView listView;
    ScheduleAdapter scheduleAdapter;
    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);

        this.mActivity = (MainActivity) act;
    }
    DateFormat dateFormat = new SimpleDateFormat("MMMM dd");
    TextView motd;
    ProgressBar progress;
    TextView progressText;
    boolean tomorrowView = false;
    SharedPreferences sharedPref;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        //create necessary view/listadatapers for the two list on Events tab
        View view = inflater.inflate(R.layout.schedule, container, false);
        listView = (ListView) view.findViewById(R.id.schedule_list);
        progress = (ProgressBar) view.findViewById(R.id.progressBar);
        progressText = (TextView) view.findViewById(R.id.progressText);
        scheduleAdapter = new ScheduleAdapter(scheduleItems,mActivity);
        listView.setAdapter(scheduleAdapter);
        motd = ((TextView) view.findViewById(R.id.motd));
        Button tomorrow = (Button) view.findViewById(R.id.btnTomorrow);
        Button today = (Button) view.findViewById(R.id.btnToday);
        sharedPref  = mActivity.getPreferences(Context.MODE_PRIVATE);
        tomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tomorrowView = true;
                Calendar c = Calendar.getInstance();
                c.setTime(MainActivity.date);
                c.add(Calendar.DATE, 1);
                Date date = c.getTime();
                scheduleItems.clear();
                scheduleItems.addAll(ScheduleItem.convertSchedule(MainActivity.calendar.tomorrowscheduleItems));
                //set correct motd (top text)
                if(MainActivity.calendar.tomorrow_alternate)
                    motd.setText("Alternate Schedule (" + dateFormat.format(date) + ")");
                else
                    motd.setText("Normal Schedule (" + dateFormat.format(date) + ")");

                if(scheduleItems.size() == 0)
                    motd.setText("No School! (" + dateFormat.format(date) + ")");
                progress.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                ((ScheduleAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        });
        today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tomorrowView = false;
                Date date = MainActivity.date;
                Calendar c = Calendar.getInstance();
                c.setTime(MainActivity.date);

                scheduleItems.clear();
                scheduleItems.addAll(ScheduleItem.convertSchedule(MainActivity.calendar.scheduleItems));
                //set correct motd (top text)
                if(MainActivity.calendar.alternate)
                    motd.setText("Alternate Schedule (" + dateFormat.format(date) + ")");
                else
                    motd.setText("Normal Schedule (" + dateFormat.format(date) + ")");

                if(scheduleItems.size() == 0)
                    motd.setText("No School! (" + dateFormat.format(date) + ")");
                progress.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
                ((ScheduleAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        });
        updateSchedule();
        return view;
    }
    public void updateProgress(){
        if(tomorrowView){
            return;
        }
        Date d = new Date();
        DateFormat df = new SimpleDateFormat("h:mm");
        DateFormat df2 = new SimpleDateFormat("dd-M-yyyy");
        String date = df2.format(d);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy h:mm a");
        Date lastEnd = null;
        for(ScheduleItem s : scheduleItems){
            try {
                String time = s.time.trim();
                String[] between = time.split("-");
                int beginHour = Integer.parseInt(between[0].split(":")[0]);
                int endHour = Integer.parseInt(between[1].split(":")[0]);
                Date startDate = sdf.parse(date + " "+ between[0] + " " + (beginHour < 4 || beginHour ==12 ? "pm" : "am"));
                Date endDate = sdf.parse(date + " " + between[1] + " " + (endHour < 4 || endHour ==12? "pm" : "am"));
                //System.out.println(String.format(sdf.format(startDate) + ", " + sdf.format(endDate) + ", " + (d.before(endDate) && d.after(startDate)) + ", " + sdf.format(d)));
                if(lastEnd != null){
                    if(d.before(startDate) && d.after(lastEnd)){
                        System.out.println(s.name);
                        long timeLeft = startDate.getTime() - d.getTime();
                        long total = startDate.getTime() - lastEnd.getTime();
                        progressText.setText((((timeLeft) / (60 * 1000)) +1) + " minutes left in Passing");
                        progress.setProgress((int)((float)(total-timeLeft)/total*100));
                    }
                }
                if(d.before(endDate) && d.after(startDate)) {
                    long timeLeft = endDate.getTime() - d.getTime();
                    long total = endDate.getTime() - startDate.getTime();
                    progressText.setText((((timeLeft) / (60 * 1000)) + 1) + " minutes left in " + s.name);
                    progress.setProgress((int) ((float) (total - timeLeft) / total * 100));
                }
                if(scheduleItems.get(scheduleItems.size()-1) == s && d.after(endDate)){
                    progressText.setText("School's out!");
                }
                if(scheduleItems.get(0) == s && d.before(startDate)){
                    long timeLeft = startDate.getTime() - d.getTime();
                    progressText.setText("School starts in " + (((timeLeft) / (60 * 1000)) + 1) + " minutes");
                }
                lastEnd = endDate;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void updateSchedule(){
        //get schedule with callback
        MainActivity.calendar.getSchedule(new Runnable() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //transfer items to local array and notify adapter
                        scheduleItems.clear();
                        scheduleItems.addAll(ScheduleItem.convertSchedule(MainActivity.calendar.scheduleItems));
                        ((ScheduleAdapter) listView.getAdapter()).notifyDataSetChanged();
                        //set correct motd (top text)
                        if(MainActivity.calendar.alternate)
                            motd.setText("Alternate Schedule (" + dateFormat.format(MainActivity.date) + ")");
                        if(scheduleItems.size() == 0)
                            motd.setText("No School! (" + dateFormat.format(MainActivity.date) + ")");
                        updateProgress();
                    }
                });

            }
        });
        updateProgress();
        //set correct motd (top text)
        Calendar c = Calendar.getInstance();
        c.setTime(MainActivity.date);
        if(scheduleItems.size() == 0)
            motd.setText("No School! (" + dateFormat.format(MainActivity.date) + ")");
        else
            motd.setText("Normal Schedule (" + dateFormat.format(MainActivity.date) + ")");

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    mActivity.runOnUiThread(new Runnable() // start actions in UI thread
                    {
                        public void run(){
                            updateProgress();
                        }
                    });

                    try {
                        Thread.sleep(10 * 1000);
                    } catch (Exception e) {}
                }
            }
        }).start();
    }
    //custom adapter
    public class ScheduleAdapter extends BaseAdapter {
        LayoutInflater mInflater;

        public class ViewHolder {
            TextView txtTitle, txtTime;
        }

        public List<ScheduleItem> list;

        public Context context;

        public ScheduleAdapter(List<ScheduleItem> apps, Context context) {
            this.list = apps;
            this.context = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View rowView = convertView;
            ViewHolder viewHolder;

            if (rowView == null) {
                rowView = mInflater.inflate(R.layout.schedule_row, null);
                viewHolder = new ViewHolder();
                viewHolder.txtTitle = (TextView) rowView.findViewById(R.id.schedule_item);
                viewHolder.txtTime = (TextView) rowView.findViewById(R.id.schedule_time);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.txtTitle.setText(list.get(position).name + "");
            viewHolder.txtTime.setText(list.get(position).getTimeString() + "");
            String editTextName = list.get(position).name.replaceAll(" ","").toLowerCase();

            return rowView;
        }
    }
}
