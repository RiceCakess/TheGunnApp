package xyz.dchen.thegunnapp;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Events event = new Events();
    StaffDirectory staff = new StaffDirectory();
    Schedule schedule = new Schedule();
    BottomBar bottomBar;
    public static GunnCalendar calendar;
    public static Date date = new Date();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //create all necessary instances
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        calendar = new GunnCalendar(this);
        initFragments();
        showFrag(schedule);
        //attach the bottom nav bar
        bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.setItemsFromMenu(R.menu.bottom_nav, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
                //switch clause to switch between frags
                switch (itemId) {
                    case R.id.schedule_item:
                        showFrag(schedule);
                        break;
                    case R.id.events_item:
                        showFrag(event);
                        break;
                    case R.id.directory_item:
                        showFrag(staff);
                        break;
                }
            }
        });
    }
    @Override
    public void onSaveInstanceState(Bundle outState){
        bottomBar.onSaveInstanceState(outState);
    }
    public void initFragments(){
        //add all the fragments to the frag manager and hide all fragments except for schedule (displays on start)
        getFragmentManager().beginTransaction()
                .add(R.id.main_content, event)
                .add(R.id.main_content, staff)
                .add(R.id.main_content, schedule)
                .hide(staff)
                .hide(event)
                .commit();
    }
    @Override
    public void onResume(){
        super.onResume();
        //if a day has past since the app was refocused from background refresh schedule/event fragments
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if(!dateFormat.format(currentDate).equals(dateFormat.format(date))){
            date = currentDate;
            getFragmentManager().beginTransaction().detach(schedule).attach(schedule).commit();
            getFragmentManager().beginTransaction().detach(event).attach(event).commit();
        }

    }
    public void showFrag(Fragment frag){
        //show and hide correct fragments when commanded
        if(frag == event)
            getFragmentManager().beginTransaction().show(frag).hide(schedule).hide(staff).commit();
        else if(frag == staff)
            getFragmentManager().beginTransaction().show(frag).hide(schedule).hide(event).commit();
        else if(frag == schedule)
            getFragmentManager().beginTransaction().show(frag).hide(event).hide(staff).commit();
    }
}
