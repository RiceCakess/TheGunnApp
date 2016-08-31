package xyz.dchen.thegunnapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Events event = new Events();
    StaffDirectory staff = new StaffDirectory();
    Schedule schedule = new Schedule();
    Map map = new Map();
    Barcode portal = new Barcode();
    BottomBar bottomBar;

    public static GunnCalendar calendar;
    public static Date date = new Date();
    Fragment currentFragment;
    Settings settings = null;
    static SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //create all necessary instances
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        calendar = new GunnCalendar(this);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        //add all fragments and show the default frag on open
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
        //getSupportActionBar().setHomeButtonEnabled(true);

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
        //if time has past since the app was refocused from background, refresh schedule/event fragments
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm");
        if(!dateFormat.format(currentDate).equals(dateFormat.format(date))){
            System.out.println("Date changed, updating information");
            date = currentDate;
            calendar = new GunnCalendar(this);
            schedule.updateSchedule();
            event.updateEvents();
            schedule.updateProgress();
        }

    }
    public void hideAllFrag(){
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().hide(schedule).hide(staff).hide(map).hide(portal).hide(event).commit();
    }
    public void showFrag(Fragment frag){
        //show and hide correct fragments when commanded
        hideAllFrag();
        getFragmentManager().beginTransaction().show(frag).commit();
        currentFragment = frag;
    }
    public void returnFrag(){
        showFrag(currentFragment);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if(settings != null) {
                    settings.close();
                }
                returnFrag();
                return true;
            case R.id.settingsBtn:
                System.out.println(settings);
                if(settings == null) {
                    FragmentManager fm = getFragmentManager();
                    hideAllFrag();
                    bottomBar.hide();
                    settings = new Settings();
                    fm.beginTransaction().add(R.id.main_content, settings).commit();
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
                return true;
            case R.id.portalBtn:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://id.pausd.org"));
                startActivity(browserIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
