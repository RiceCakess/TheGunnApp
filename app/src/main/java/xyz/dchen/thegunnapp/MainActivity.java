package xyz.dchen.thegunnapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Events event = new Events();
    StaffDirectory staff = new StaffDirectory();
    Schedule schedule = new Schedule();
    Map map = new Map();
    Portal portal = new Portal();
    BottomBar bottomBar;
    public static GunnCalendar calendar;
    public static Date date = new Date();
    List<Fragment> fragments = new ArrayList<>(5);
    boolean firstStart = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //create all necessary instances
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        calendar = new GunnCalendar(this);
        //add all fragments and show the default frag on open
        initFragments();
        showFrag(schedule);
        firstStart = false;
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
                    case R.id.map_item:
                        showFrag(map);
                        break;
                    case R.id.directory_item:
                        showFrag(staff);
                        break;
                    case R.id.portal_item:
                        showFrag(portal);
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
        //add all the fragments to the frag manager
        getFragmentManager().beginTransaction()
                .add(R.id.main_content, event)
                .add(R.id.main_content, staff)
                .add(R.id.main_content, schedule)
                .add(R.id.main_content, map)
                .add(R.id.main_content, portal)
                .commit();
    }
    @Override
    public void onResume(){
        super.onResume();
        if(firstStart){
           return;
        }
        //if a day has past since the app was refocused from background refresh schedule/event fragments
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if(!dateFormat.format(currentDate).equals(dateFormat.format(date))){
            date = currentDate;
           // getFragmentManager().beginTransaction().detach(schedule).attach(schedule).commit();
            //getFragmentManager().beginTransaction().detach(event).attach(event).commit();
        }

    }
    public void showFrag(Fragment frag){
        //show and hide correct fragments when commanded
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().hide(schedule).hide(staff).hide(map).hide(portal).hide(event).commit();
        getFragmentManager().beginTransaction().show(frag).commit();
    }
}
