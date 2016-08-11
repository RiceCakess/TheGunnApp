package xyz.dchen.thegunnapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//custom adatper
public class StaffListAdapter extends BaseAdapter {
    LayoutInflater mInflater;
    public class ViewHolder {
        TextView txtTitle, txtSubTitle;
    }

    public List<Staff> list;

    public Context context;
    ArrayList<Staff> arraylist;

    public StaffListAdapter(List<Staff> apps, Context context) {
        this.list = apps;
        this.context = context;
        arraylist = new ArrayList<Staff>();
        arraylist.addAll(list);
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
    public void setDefaultList(List<Staff> stafflist){
        arraylist.addAll(stafflist);
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        ViewHolder viewHolder;

        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.staff_row, null);
            viewHolder = new ViewHolder();
            viewHolder.txtTitle = (TextView) rowView.findViewById(R.id.name);
            viewHolder.txtSubTitle = (TextView) rowView.findViewById(R.id.position);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txtTitle.setText(list.get(position).name + "");
        viewHolder.txtSubTitle.setText(list.get(position).position + " - " + list.get(position).department);
        return rowView;


    }
    //search bar filterer
    public void filter(String charText) {
        //check if typing and filter accordingly
        charText = charText.toLowerCase(Locale.getDefault());
        list.clear();
        if (charText.length() == 0) {
            list.addAll(arraylist);

        } else {
            for (Staff postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.name.toLowerCase(Locale.getDefault()).contains(charText)) {
                    list.add(postDetail);
                } else if (charText.length() != 0 && postDetail.position.toLowerCase(Locale.getDefault()).contains(charText)) {
                    list.add(postDetail);
                }
                else if (charText.length() != 0 && postDetail.department.toLowerCase(Locale.getDefault()).contains(charText)) {
                    list.add(postDetail);
                }
            }
        }
        notifyDataSetChanged();
    }

}
