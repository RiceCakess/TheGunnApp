package xyz.dchen.thegunnapp;

/**
 * Created by David on 7/18/2016.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.Scene;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Events extends Fragment {

    Activity mActivity;
    ArrayList<EventItem> todayItems = new ArrayList<EventItem>();
    ArrayList<EventItem> upcomingItems = new ArrayList<EventItem>();
    ListView todayView,upcomingView;
   EventAdapter todayAdapter,upcomingAdapter;

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);

        this.mActivity = act;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.events, container, false);
        todayView = (ListView) view.findViewById(R.id.todaylist);
        upcomingView = (ListView) view.findViewById(R.id.upcominglist);
        todayAdapter = new EventAdapter(todayItems,mActivity);
        upcomingAdapter = new EventAdapter(upcomingItems,mActivity);
        todayView.setAdapter(todayAdapter);
        upcomingView.setAdapter(upcomingAdapter);
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                        .setTitle(todayItems.get(position).summary)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        });
                builder.setMessage(todayItems.get(position).description);
                if(!todayItems.get(position).description.equals("")){
                    builder.show();
                }
            }
        };
        todayView.setOnItemClickListener(listener);
        upcomingView.setOnItemClickListener(listener);
        MainActivity.calendar.checkForEvents(new Runnable() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        todayItems.clear();
                        upcomingItems.clear();
                        ArrayList<EventItem> items = EventItem.convertAll(MainActivity.calendar.events);
                        for(EventItem eItem: items){
                            Date today = MainActivity.date;
                            DateFormat df = new SimpleDateFormat("MMMM dd");
                            if(eItem.getDateString().equals(df.format(today)))
                            //if(eItem.getDateString().equals("May 27"))
                                todayItems.add(eItem);
                            else
                                upcomingItems.add(eItem);
                        }


                        ((EventAdapter) todayView.getAdapter()).notifyDataSetChanged();
                        ((EventAdapter) upcomingView.getAdapter()).notifyDataSetChanged();
                    }
                });
            }
        });
        return view;
    }

    public class EventAdapter extends BaseAdapter {
        LayoutInflater mInflater;

        public class ViewHolder {
            TextView txtSummary, txtDescription, txtTime;
        }

        public List<EventItem> list;

        public Context context;

        public EventAdapter(List<EventItem> apps, Context context) {
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
                rowView = mInflater.inflate(R.layout.event_row, null);
                viewHolder = new ViewHolder();
                viewHolder.txtSummary = (TextView) rowView.findViewById(R.id.summary);
                viewHolder.txtTime = (TextView) rowView.findViewById(R.id.event_time);
                viewHolder.txtDescription = (TextView) rowView.findViewById(R.id.event_date);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.txtSummary.setText(list.get(position).summary + "");
            viewHolder.txtTime.setText(list.get(position).getTimeString() + "");
            viewHolder.txtDescription.setText(list.get(position).getDateString() + "");
            return rowView;
        }
    }
}
