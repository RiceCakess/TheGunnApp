package xyz.dchen.thegunnapp;

/**
 * Created by David on 7/18/2016.
 */
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.transition.Scene;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Schedule extends Fragment {

    Activity mActivity;
    ArrayList<ScheduleItem> scheduleItems = new ArrayList<ScheduleItem>();
    ListView listView;
    ScheduleAdapter scheduleAdapter;
    public static Schedule newInstance() {
        return new Schedule ();
    }
    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);

        this.mActivity = act;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        //create necessary view/listadatapers for the two list on Events tab
        View view = inflater.inflate(R.layout.schedule, container, false);
        listView = (ListView) view.findViewById(R.id.schedule_list);
        scheduleAdapter = new ScheduleAdapter(scheduleItems,mActivity);
        listView.setAdapter(scheduleAdapter);
        final DateFormat dateFormat = new SimpleDateFormat("MMMM dd");
        final TextView motd = ((TextView) view.findViewById(R.id.motd));
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
                        motd.setText("Alternate Schedule (" + dateFormat.format(MainActivity.date) + ")");
                    }
                });

            }
        });
        //set correct motd (top text)
        Calendar c = Calendar.getInstance();
        c.setTime(MainActivity.date);
        if(c.get(Calendar.DAY_OF_WEEK) == 1 || c.get(Calendar.DAY_OF_WEEK) == 7)
            motd.setText("No School! (" + dateFormat.format(MainActivity.date) + ")");
        else
            motd.setText("Normal Schedule (" + dateFormat.format(MainActivity.date) + ")");
        return view;
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
            String hexColor = ScheduleItem.getRowColor(list.get(position).name);
            if(hexColor != null) {
                rowView.setBackgroundColor(Color.parseColor(hexColor));
            }
            return rowView;
        }
    }
}
