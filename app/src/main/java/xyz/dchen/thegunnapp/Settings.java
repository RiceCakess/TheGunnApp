package xyz.dchen.thegunnapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.lang.reflect.Field;

/**
 * Created by David on 8/27/2016.
 */
public class Settings extends Fragment {
    Activity mActivity;

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);

        this.mActivity = act;
    }
    static int[] periods = {R.id.aperiod,R.id.bperiod,R.id.cperiod,R.id.dperiod,R.id.eperiod,R.id.fperiod,R.id.gperiod };
    static String[] sPeriod = {"aperiod","bperiod","cperiod","dperiod","eperiod","fperiod","gperiod"};
    SharedPreferences sharedPref;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        //create necessary view/listadatapers for the two list on Events tab
        View view = inflater.inflate(R.layout.settings, container, false);
        sharedPref  = mActivity.getPreferences(Context.MODE_PRIVATE);
        final Button cancel = (Button) view.findViewById(R.id.cancelBtn);

        for(int i = 0; i < periods.length; i++) {
            if(sharedPref.contains(sPeriod[i])){
                ((EditText)view.findViewById(periods[i])).setText(sharedPref.getString(sPeriod[i],null));
            }
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });
        Button ok = (Button) view.findViewById(R.id.okBtn);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor edit = sharedPref.edit();
                int index = 0;
                for(int period : periods) {
                    EditText et = (EditText) mActivity.findViewById(period);
                    edit.putString(sPeriod[index], et.getText().toString());
                    index++;
                }
                edit.commit();
                close();
            }
        });
        return view;
    }

    public void close(){
        getFragmentManager().beginTransaction().remove(this).commit();
        ((MainActivity) mActivity).bottomBar.show();
        ((MainActivity) mActivity).settings = null;
        ((MainActivity) mActivity).returnFrag();
        ((MainActivity) mActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }
}
