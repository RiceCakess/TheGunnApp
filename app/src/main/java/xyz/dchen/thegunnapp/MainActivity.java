package xyz.dchen.thegunnapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        calendar = new GunnCalendar(this);
        initFragments();
        showFrag(schedule);
        bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.setItemsFromMenu(R.menu.bottom_nav, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
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
        //calendar.getSchedule();
    }
    @Override
    public void onSaveInstanceState(Bundle outState){
        bottomBar.onSaveInstanceState(outState);
    }
    public void initFragments(){
        getFragmentManager().beginTransaction()
                .add(R.id.main_content, event)
                .add(R.id.main_content, staff)
                .add(R.id.main_content, schedule)
                .hide(staff)
                .hide(event)
                .commit();
    }

    public void showFrag(Fragment frag){
        if(frag == event)
            getFragmentManager().beginTransaction().show(frag).hide(schedule).hide(staff).commit();
        else if(frag == staff)
            getFragmentManager().beginTransaction().show(frag).hide(schedule).hide(event).commit();
        else if(frag == schedule)
            getFragmentManager().beginTransaction().show(frag).hide(event).hide(staff).commit();
    }
}
