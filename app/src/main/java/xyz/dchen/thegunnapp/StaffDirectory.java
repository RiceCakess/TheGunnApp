package xyz.dchen.thegunnapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by David on 7/17/2016.
 */
public class StaffDirectory extends Fragment {
    ListView staff_directory;
    ArrayList<Staff> stafflist = new ArrayList<Staff>();
    StaffListAdapter listadapter;
    Activity mActivity;
    @Override
    public void onAttach(Activity act)
    {
        super.onAttach(act);

        this.mActivity = act;
    }
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle
                                     savedInstanceState) {
    //create necessary view/listadatapers for the two list on Events tab
        View view =  inflater.inflate(R.layout.staff_directory,
                container, false);
        staff_directory = (ListView) view.findViewById(R.id.staff_list);

        initStaffList();
        listadapter = new StaffListAdapter(stafflist, mActivity);
        staff_directory.setAdapter(listadapter);
        staff_directory.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                createStaffInfo(stafflist.get(position));
            }
        });
        this.setHasOptionsMenu(true);
        return view;
    }

    public void createStaffInfo(final Staff s) {
        //dialog creator when user clicks on staff member
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(s.name);
        String message = "Phone Number: " + s.telephone + "\n" + "Email: " + s.email + "\nPosition: " + s.position + " \nDepartment: " + s.department;
        if (s.website.contains(".")) {
            message += "\nWebsite: " + s.website;
        }
        builder.setMessage(message);

        builder.setNeutralButton("Call", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialNumber(s.telephone);
            }
        });

        builder.setNegativeButton("Email", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendEmail(s.email);
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void initStaffList() {
        //parse staff list from online website
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //read webpage and string manipulation to extract data
                    URL webpage = new URL("http://www.gunn.pausd.org/people");
                    BufferedReader read = new BufferedReader(new InputStreamReader(webpage.openStream()));
                    String line;
                    boolean hittable = false;
                    String tabledata = "";
                    while ((line = read.readLine()) != null) {
                        if (line.contains("<tbody>") || line.contains("</tbody>")) {
                            hittable = !hittable;
                        }
                        if (hittable) {
                            tabledata += line;
                        }
                    }
                    tabledata = tabledata.replaceAll("<tr class=\"even\">", "");
                    tabledata = tabledata.replaceAll("<tr class=\"odd\">", "");
                    String[] rowsplit = tabledata.split("</tr>");
                    stafflist.clear();
                    for (String s : rowsplit) {
                        String[] columnsplit = s.split("</td>");
                        for (int i = 0; i < columnsplit.length; i++) {
                            String columnstring = columnsplit[i];
                            String col = android.text.Html.fromHtml(columnstring).toString();
                            col = col.trim();
                            columnsplit[i] = col;
                        }
                        stafflist.add(new Staff(columnsplit[0], columnsplit[1], columnsplit[2], columnsplit[3], columnsplit[4], columnsplit[5]));
                    }
                    read.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((BaseAdapter) staff_directory.getAdapter()).notifyDataSetChanged();
                        ((StaffListAdapter) staff_directory.getAdapter()).setDefaultList(stafflist);
                    }
                });
            }
        });
        t.start();
        loadOfflineData();
    }

    public void dialNumber(String number) {
        //push to call page without need to copy phonenumber
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)));
    }

    public void sendEmail(String email) {
        //create new email draft to send staff email
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        intent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(Intent.createChooser(intent, ""));
    }
    //custom adapter
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                //filter on text change
                listadapter.filter(searchQuery.toString().trim());
                staff_directory.invalidate();
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //did not write this by hand, had a script do it for me
    public void loadOfflineData(){
        stafflist.add(new Staff("Allie Ackard", "Teacher", "English", "354-8238", "aackard@pausd.org", ""));
        stafflist.add(new Staff("Laila Adle", "Vision Specialist", "Special Education", "354-8223", "ladle@pausd.org", ""));
        stafflist.add(new Staff("Steve Ames", "Instructional Supervisor, Teacher", "Physical Education", "354-8266", "sames@pausd.org", ""));
        stafflist.add(new Staff("Amy Anderson", "Teacher", "Physical Education", "354-8215", "amanderson@pausd.org", ""));
        stafflist.add(new Staff("Adriana Aro", "Teacher", "Special Education", "354-8230", "aaro@pausd.org", ""));
        stafflist.add(new Staff("Arlena Arteaga", "Teacher", "Social Studies", "354-8237", "aarteaga@pausd.org", ""));
        stafflist.add(new Staff("Timothy Aston", "Teacher", "Science", "354-8246", "taston@pausd.org", ""));
        stafflist.add(new Staff("Jack Ballard", "Assistant Principal", "Administration", "354-8200, x6885", "jballard@pausd.org", ""));
        stafflist.add(new Staff("Neil Barana", "Custodian", "Custodial", "354-8205", "nbarana@pausd.org", ""));
        stafflist.add(new Staff("Ana Barrios", "Teacher", "World Languages", "354-8241", "abarrios@pausd.org", ""));
        stafflist.add(new Staff("Michael Bautista", "Teacher", "Mathematics", "354-8247", "mbautista@pausd.org", "Webpage"));
        stafflist.add(new Staff("Chris Bell", "Teacher, Technology Coordinator", "Technology, Career Technical Education", "354-8247", "cbell@pausd.org", "Webpage"));
        stafflist.add(new Staff("Gretchen Berg", "Aide", "Special Education", "354-8230", "gberg@pausd.org", ""));
        stafflist.add(new Staff("David Bisbee", "Teacher", "Social Studies", "", "dbisbee@pausd.org", ""));
        stafflist.add(new Staff("Kristy Blackburn", "Instructional Supervisor, Teacher", "English", "354-8238", "kblackburn@pausd.org", ""));
        stafflist.add(new Staff("Cris Boswell", "Aide", "Special Education", "354-8230", "cboswell@pausd.org", ""));
        stafflist.add(new Staff("Don Bratton", "Custodian", "Custodial", "354-8205", "dbratton@pausd.org", ""));
        stafflist.add(new Staff("Don Briggs", "Teacher", "Physical Education", "354-8266", "dbriggs@pausd.org", ""));
        stafflist.add(new Staff("Justin Brown", "Teacher", "English", "354-8238", "jdbrown@pausd.org", ""));
        stafflist.add(new Staff("John Bulan", "Tech Support", "Technology", "849-7902", "jbulan@pausd.org", ""));
        stafflist.add(new Staff("Dean Bunderson", "Theatre Manager", "Spangenberg", "444-7127", "dbunderson@pausd.org", "Webpage"));
        stafflist.add(new Staff("Mary Cabanski", "Aide", "Special Education", "354-8230", "mcabanski@pausd.org", ""));
        stafflist.add(new Staff("Mike Camicia", "Teacher", "Career Technical Education", "354-8255", "mcamicia@pausd.org", "Webpage"));
        stafflist.add(new Staff("Courtney Carlomagno", "Teacher", "Special Education", "354-8262", "ccarlomagno@pausd.org", ""));
        stafflist.add(new Staff("Olga Celis", "Academic Technology", "Technology", "354-8200, x5308", "ocelis@pausd.org", ""));
        stafflist.add(new Staff("Goldie Chan", "Counselor", "Guidance", "354-8208", "gchan@pausd.org", ""));
        stafflist.add(new Staff("Dr. Frank Chen", "Physician", "Athletics", "(650) 853-2943", "", ""));
        stafflist.add(new Staff("Melissa Clark", "Psychologist", "Psychologist", "354-8216", "mclark@pausd.org", ""));
        stafflist.add(new Staff("Cathy Cohn", "Teacher", "Science", "354-8246", "ccohn@pausd.org", ""));
        stafflist.add(new Staff("Warren Collier", "Teacher", "Social Studies", "354-8237", "wcollier@pausd.org", ""));
        stafflist.add(new Staff("Myesha Compton", "Counselor", "Guidance", "354-8207", "mcompton@pausd.org", ""));
        stafflist.add(new Staff("Rachel Congress", "Teacher", "Mathematics", "354-8247", "rcongress@pausd.org", ""));
        stafflist.add(new Staff("Robert Cormia", "Teacher, Nanotech", "Career Technical Education", "354-8200", "rcormia@pausd.org", ""));
        stafflist.add(new Staff("Carol Cornett", "Campus Supervisor", "Campus Supervisor", "354-8200", "pcornett@pausd.org", ""));
        stafflist.add(new Staff("Edward Corpuz", "Teacher", "Career Technical Education", "849-7905", "ecorpuz@pausd.org", ""));
        stafflist.add(new Staff("Grace Dadzie", "Aide", "Special Education", "354-8230", "gdadzie@pausd.org", ""));
        stafflist.add(new Staff("Edwin De Guzman", "Custodian", "Support Staff", "354-8205", "edeguzman@pausd.org", ""));
        stafflist.add(new Staff("Dave Deggeller", "Instructional Supervisor, Teacher", "Mathematics", "354-8247", "ddeggeller@pausd.org", "Webpage"));
        stafflist.add(new Staff("Karin Delgadillo", "Campus Supervisor", "Support Staff", "354-8288", "kdelgadillo@pausd.org", ""));
        stafflist.add(new Staff("Narinder Dhillon", "Aide", "Special Programs", "849-7962", "ndhillon@pausd.org", ""));
        stafflist.add(new Staff("Rachel Dion", "Teacher", "Mathematics", "354-8247", "rdion@pausd.org", ""));
        stafflist.add(new Staff("Tracy Douglas", "Registrar", "Guidance", "354-8284", "tdouglas@pausd.org", ""));
        stafflist.add(new Staff("Anne Dumontier", "Teacher", "World Languages", "354-8241", "adumontier@pausd.org", "Webpage"));
        stafflist.add(new Staff("Bill Dunbar", "Teacher", "Science", "354-8247", "bdunbar@pausd.org", "Webpage"));
        stafflist.add(new Staff("Paul Dunlap", "Teacher", "English", "354-8238", "pdunlap@pausd.org", ""));
        stafflist.add(new Staff("Marie Durquet", "Teacher", "Visual and Performing Arts", "354-7903", "mdurquet@pausd.org", ""));
        stafflist.add(new Staff("Martha Elderon", "Secretary to the Principal", "Support Staff", "354-8288", "melderon@pausd.org", ""));
        stafflist.add(new Staff("Scott Elfving", "Teacher", "Social Studies", "354-8237", "selfving@pausd.org", ""));
        stafflist.add(new Staff("Ellen Feigenbaum", "Teacher", "English", "354-8238", "efeigenbaum@pausd.org", ""));
        stafflist.add(new Staff("Darlene Feldstein", "Instructional Technology", "Technology", "849-7902", "dfeldstein@pausd.org", ""));
        stafflist.add(new Staff("Jon Fidani", "Counselor", "Guidance", "", "jfidani@pausd.org", ""));
        stafflist.add(new Staff("Tara Firenzi", "Teacher", "Social Studies", "354-8237", "tfirenzi@pausd.org", ""));
        stafflist.add(new Staff("Cristina Florea", "Teacher", "Mathematics", "354-8247", "ccismas@pausd.org", "Webpage"));
        stafflist.add(new Staff("Robin Francesconi", "Guidance Technician Asst", "Guidance", "354-8212", "rfrancesconi@pausd.org", ""));
        stafflist.add(new Staff("Megan Garcia", "Asst. Librarian", "Support Staff", "354-8252", "mgarcia@pausd.org", ""));
        stafflist.add(new Staff("Gabriela Garger", "Teacher", "World Languages", "354-8241", "ggarger@pausd.org", "Webpage"));
        stafflist.add(new Staff("Daljeet Gill", "Librarian", "Library", "354-8252", "dgill@pausd.org", ""));
        stafflist.add(new Staff("Diane Gleason", "Teacher", "Mathematics", "354-8247", "dgleason@pausd.org", ""));
        stafflist.add(new Staff("Mark Gleason", "Teacher", "Visual and Performing Arts", "849-7906", "mgleason@pausd.org", "Webpage"));
        stafflist.add(new Staff("Lynn Glover", "Department Secretary", "English", "354-8247", "lglover@pausd.org", ""));
        stafflist.add(new Staff("Melanie Gomez", "Cafeteria Supervisor", "Food Services", "354-8233", "mgomez@pausd.org", ""));
        stafflist.add(new Staff("Gordon Goodspeed", "Custodian", "Custodial", "354-8205", "ggoodspeed@pausd.org", ""));
        stafflist.add(new Staff("Kristina Granlund", "Teacher", "Career Technical Education", "354-8254", "kgranlund@pausd.org", ""));
        stafflist.add(new Staff("Deborah Grant", "Health Tech Nurse", "Health Office", "354-8211", "dgrant@pausd.org", ""));
        stafflist.add(new Staff("Mark Grieb", "Teacher", "Science", "354-8246", "mgrieb@pausd.org", ""));
        stafflist.add(new Staff("Patricia Guevara", "Teacher", "Special Education", "354-8230", "pguevara@pausd.org", ""));
        stafflist.add(new Staff("Meri Gyves", "Teacher/Coordinator Work Experience", "Guidance", "354-8221", "mgyves@pausd.org", ""));
        stafflist.add(new Staff("Ronen Habib", "Teacher", "Social Studies", "354-8237", "rhabib@pausd.org", "Webpage"));
        stafflist.add(new Staff("Alice Hahn", "Aide", "Special Education", "354-8218", "ahahn@pausd.org", ""));
        stafflist.add(new Staff("Daniel Hahn", "Teacher", "Mathematics", "354-8247", "dhahn@pausd.org", "Webpage"));
        stafflist.add(new Staff("Lisa Hall", "Teacher, Director of Student Activities", "English", "354-8228", "lhall@pausd.org", "Webpage"));
        stafflist.add(new Staff("Nigel Hansen", "Custodian", "Custodial", "354-8205", "nhansen@pausd.org", ""));
        stafflist.add(new Staff("Yukiko Hara", "Teacher", "World Languages", "354-8241", "yhara@pausd.org", ""));
        stafflist.add(new Staff("Rene Hart", "Site Council Coordinator", "Site Council", "849-7932", "rhart@pausd.org", ""));
        stafflist.add(new Staff("John Hebert", "Teacher", "Social Studies", "354-8237", "jhebert@pausd.org", ""));
        stafflist.add(new Staff("Kerstin Helbing", "Teacher/Coordinator", "English Language Learners", "354-8241", "khelbing@pausd.org", "Webpage"));
        stafflist.add(new Staff("Susan Henderson", "Aide", "Special Education", "849-7953", "shenderson@pausd.org", ""));
        stafflist.add(new Staff("Luciano Hernandez", "Custodian - Supervisor", "Custodial", "354-8205", "lhernandez@pausd.org", ""));
        stafflist.add(new Staff("Mark Hernandez", "Teacher", "Social Studies", "354-8237", "mhernandez@pausd.org", ""));
        stafflist.add(new Staff("Peter Herreshoff", "Teacher", "Mathematics", "354-8247", "pherreshoff@pausd.org", ""));
        stafflist.add(new Staff("Dr. Denise Herrmann", "Principal", "Administration", "354-8200", "dherrmann@pausd.org", ""));
        stafflist.add(new Staff("Norma Hesterman", "Volunteer Coordinator", "Support Staff", "354-8234", "nhesterman@pausd.org", ""));
        stafflist.add(new Staff("Jessica Hexsel", "Teacher", "Mathematics", "849-7930", "jhexsel@pausd.org", ""));
        stafflist.add(new Staff("Yukie Hikida", "Teacher", "World Languages", "354-8241", "yhikida@pausd.org", "Webpage"));
        stafflist.add(new Staff("Hindi Hipp", "Budget Secretary", "Support Staff", "354-8274", "hhipp@pausd.org", ""));
        stafflist.add(new Staff("Jia Ho", "Teacher", "English", "354-8238", "jho@pausd.org", ""));
        stafflist.add(new Staff("Shaina Holdener", "Teacher", "English", "354-8238", "sholdener@pausd.org", ""));
        stafflist.add(new Staff("Patricia Holmes", "Teacher", "Social Studies", "849-7951", "pholmes@pausd.org", ""));
        stafflist.add(new Staff("Chris Horpel", "Instructional Supervisor, Teacher", "Physical Education", "354-8266", "chorpel@pausd.org", ""));
        stafflist.add(new Staff("Laurel Howard", "Teacher", "Social Studies", "354-8237", "lhoward@pausd.org", ""));
        stafflist.add(new Staff("Joanna Hubenthal", "Teacher", "Special Education", "354-8262", "jhubenthal@pausd.org", ""));
        stafflist.add(new Staff("Keith Hunter", "Teacher", "Visual and Performing Arts", "354-8264", "kphillips@pausd.org", ""));
        stafflist.add(new Staff("Diane Ichikawa", "Teacher", "English", "354-8238", "dichikawa@pausd.org", "Webpage"));
        stafflist.add(new Staff("Marc Igler", "Teacher", "English", "354-8238", "migler@pausd.org", "Webpage"));
        stafflist.add(new Staff("Marcus Jamison", "Teacher", "Mathematics", "354-8247", "mjamison@pausd.org", ""));
        stafflist.add(new Staff("Curt Johansen", "Athletic Director", "Athletics", "354-8257", "cjohansen@pausd.org", ""));
        stafflist.add(new Staff("Derek Johanson", "Counselor", "Guidance", "849-7935", "djohanson@pausd.org", ""));
        stafflist.add(new Staff("Chris Johnson", "Teacher", "Social Studies", "354-8237", "cjohnson@pausd.org", ""));
        stafflist.add(new Staff("Matt Johnson", "Boys Water Polo Coach", "Athletics", "354-8257", "johnson.matt650@gmail.com", ""));
        stafflist.add(new Staff("Bridget Johnson", "Facilities Secretary", "Support Staff", "(650) 354-8280", "bjohnson@pausd.org", ""));
        stafflist.add(new Staff("Takeshi Kaneko", "Teacher", "Mathematics", "354-8247", "tkaneko@pausd.org", ""));
        stafflist.add(new Staff("Chris Karas", "Teacher", "Mathematics", "354-8247", "ckaras@pausd.org", ""));
        stafflist.add(new Staff("Tara Keith", "Assistant Principal, Student Services & Counseling", "Administration", "354-8227", "tkeith@pausd.org", ""));
        stafflist.add(new Staff("Steve Kelly", "Custodian - Supervisor", "Custodial", "354-8205", "skelly@pausd.org", ""));
        stafflist.add(new Staff("Tony Kelly", "Football Coach", "Athletics", "354-8257", "akelly@pausd.org", ""));
        stafflist.add(new Staff("Jordan King", "Teacher", "Visual and Performing Arts", "849-7948", "jking@pausd.org", ""));
        stafflist.add(new Staff("Arthur Kinyanjui", "Teacher", "Social Studies", "354-8237", "akinyanjui@pausd.org", ""));
        stafflist.add(new Staff("Linda Kirsch", "College & Career Counselor", "College & Career", "354-8204", "lkirsch@pausd.org", ""));
        stafflist.add(new Staff("Terence Kitada", "Teacher", "English", "354-8238", "tkitada@pausd.org", ""));
        stafflist.add(new Staff("Kim Knaack", "SAC clerk", "SAC", "354-8229", "kknaack@pausd.org", ""));
        stafflist.add(new Staff("Jacintha Kompella", "Teacher", "Science", "354-8246", "jkompella@pausd.org", ""));
        stafflist.add(new Staff("Leslie Kousnetz", "SAC clerk", "SAC", "354-8229", "lkousnetz@pausd.org", ""));
        stafflist.add(new Staff("Danielle Kovacich", "Teacher", "English", "354-8238", "dkovacich@pausd.org", ""));
        stafflist.add(new Staff("Leighton Lang", "College & Career Coordinator", "College & Career", "354-8219", "llang@pausd.org", ""));
        stafflist.add(new Staff("Eric Ledgerwood", "Teacher", "Science", "354-8246", "eledgerwood@pausd.org", "Webpage"));
        stafflist.add(new Staff("Michelle Lee", "Mental Health Therapist/AC", "Special Education", "354-8200, x1498", "mlee@pausd.org", ""));
        stafflist.add(new Staff("Teri Lee", "Teacher", "Special Education", "354-8230", "tlee@pausd.org", ""));
        stafflist.add(new Staff("Jena Lee", "Teacher", "Science", "354-8246", "jlee@pausd.org", ""));
        stafflist.add(new Staff("David Leftwich", "Counselor", "Guidance", "354-8225", "dleftwich", ""));
        stafflist.add(new Staff("Kira Levina", "Aide", "English Language Learners", "849-7922", "klevina@pausd.org", ""));
        stafflist.add(new Staff("Sandra Lewis", "Teacher", "Visual and Performing Arts", "354-8264", "slewis@pausd.org", "Webpage"));
        stafflist.add(new Staff("Jessy (Xiaojie) Li", "Teacher", "World Languages", "354-8241", "xli@pausd.org", ""));
        stafflist.add(new Staff("Curtis Liang", "Asst. Athletic Director", "Athletics", "354-8259", "cliang@pausd.org", ""));
        stafflist.add(new Staff("Curtis Liang", "Track and Field Coach", "", "354-8259", "cliang@pausd.org", ""));
        stafflist.add(new Staff("Bill Liberatore", "Teacher", "Visual and Performing Arts", "354-8287", "bliberatore@pausd.org", "Webpage"));
        stafflist.add(new Staff("Devin Licon", "Technical Support Specialist", "Technology", "3548200 Ext. 6405", "dlicon@pausd.org", ""));
        stafflist.add(new Staff("Dawna Linsdell", "Teacher", "Social Studies", "354-8237", "dlinsdell@pausd.org", ""));
        stafflist.add(new Staff("Laura Lizundia", "Teacher", "World Languages", "354-8241", "llizundia@pausd.org", ""));
        stafflist.add(new Staff("Kristen Lo", "Teacher", "Visual and Performing Arts", "354-8258", "klo@pausd.org", ""));
        stafflist.add(new Staff("Katrina Lortie", "Attendance Clerk", "Support Staff", "354-8210", "klortie@pausd.org", ""));
        stafflist.add(new Staff("Marcel Losier", "Teacher", "World Languages", "354-8241", "mlosier@pausd.org", "Webpage"));
        stafflist.add(new Staff("Krissy Ludemann", "Department Secretary", "World Languages", "354-8241", "kludemann@pausd.org", ""));
        stafflist.add(new Staff("Phil Lyons", "Teacher", "Social Studies", "354-8237", "plyons@pausd.org", "Webpage"));
        stafflist.add(new Staff("Armando Macias", "Custodian", "Support Staff", "354-8205", "amacias@pausd.org", ""));
        stafflist.add(new Staff("Alec MacLean", "Teacher", "Science", "354-8246", "amaclean@pausd.org", "Webpage"));
        stafflist.add(new Staff("Sophie Magid-Gutkin", "Aide", "Special Education", "354-8250", "smagidgutkin@pausd.org", ""));
        stafflist.add(new Staff("Lisa Mahpour", "Food Services", "Food Services", "354-8233", "lmahpour@pausd.org", ""));
        stafflist.add(new Staff("Carole Main", "Secretary", "Support Staff", "354-8254", "cmain@pausd.org", ""));
        stafflist.add(new Staff("Jeanne-Claudius Martin", "Science Prep Aide", "Special Programs, Science", "354-8246", "jmartin@pausd.org", ""));
        stafflist.add(new Staff("Rolando Martinez", "Technology asst.", "Technology", "849-77902", "rmartinez@pausd.org", ""));
        stafflist.add(new Staff("Carlos Martinez", "Teacher", "Mathematics", "354-8247", "cmartinez@pausd.org", ""));
        stafflist.add(new Staff("Liz Matchett", "Instructional Supervisor, Teacher", "World Languages", "354-8241", "lmatchett@pausd.org", ""));
        stafflist.add(new Staff("Charles Mayman", "Robotics Aide", "Career Technical Education", "354-8254", "cmayman@pausd.org", ""));
        stafflist.add(new Staff("Claire McCole", "Food Services Manager", "Food Services", "354-8233", "cmccole@pausd.org", ""));
        stafflist.add(new Staff("Matt McGinn", "Teacher", "Physical Education", "354-8266", "mmcginn@pausd.org", ""));
        stafflist.add(new Staff("Norma Medina", "Teacher", "World Languages", "354-8241", "nmedina@pausd.org", ""));
        stafflist.add(new Staff("Heather Mellows", "Teacher", "Science", "354-8246", "hmellows@pausd.org", ""));
        stafflist.add(new Staff("Nicole Menache", "Teacher", "English", "354-8238", "nmenache@pausd.org", ""));
        stafflist.add(new Staff("Stephanie Mendoza", "Translator", "Special Programs", "354-8231", "smendoza@pausd.org", ""));
        stafflist.add(new Staff("Angela Merchant", "Teacher", "Science, Career Technical Education", "354-8246", "amerchant@pausd.org", "Webpage"));
        stafflist.add(new Staff("Deanna Messinger", "Teacher", "Visual and Performing Arts", "354-8256", "dmessinger@pausd.org", "Webpage"));
        stafflist.add(new Staff("Anne Marie Metzler", "Teacher", "Special Education", "354-8230", "ametzler@pausd.org", ""));
        stafflist.add(new Staff("Challis Michael", "Counselor", "Guidance", "354-8226", "cmichael@pausd.org", ""));
        stafflist.add(new Staff("Joanne Michels", "Mental Health therapist", "Wellness", "354-8253", "jmichels@pausd.org", ""));
        stafflist.add(new Staff("Brian Miguel", "Teacher", "Social Studies", "354-8237", "bmiguel@pausd.org", ""));
        stafflist.add(new Staff("Ahmed Mohmand", "Aide", "Special Education", "354-8230", "amohmand@pausd.org", ""));
        stafflist.add(new Staff("Ginny Moyer", "Teacher", "English", "354-8238", "vmoyer@pausd.org", ""));
        stafflist.add(new Staff("Julie Munger", "Teacher", "English", "354-8238", "jmunger@pausd.org", ""));
        stafflist.add(new Staff("Lynne Navarro", "Instructional Supervisor", "Social Studies", "354-8237", "lnavarro@pausd.org", "Webpage"));
        stafflist.add(new Staff("Vicky Niemann", "Special Ed. Secretary", "Special Education", "354-8262", "vniemann@pausd.org", ""));
        stafflist.add(new Staff("Casey O Connell", "Teacher", "Science", "354-8246", "coconnell@pausd.org", "Webpage"));
        stafflist.add(new Staff("Dennis Ochoa", "TRC Coordinator", "Support Staff", "", "dochoa@pausd.org", ""));
        stafflist.add(new Staff("Kristen Owen", "Teacher", "English", "354-8238", "kowen@pausd.org", "Webpage"));
        stafflist.add(new Staff("Jarrod Pagan", "Attendance Clerk", "Support Staff", "354-8230", "jpagan@pausd.org", ""));
        stafflist.add(new Staff("Josh Paley", "Teacher", "Career Technical Education, Mathematics", "354-8247", "jpaley@pausd.org", "Webpage"));
        stafflist.add(new Staff("Marjorie Paronable", "Teacher", "English", "354-8238", "mparonable@pausd.org", ""));
        stafflist.add(new Staff("Jeff Patrick", "Teacher", "Social Studies", "354-8237", "jpatrick@pausd.org", ""));
        stafflist.add(new Staff("Cecilia Peltier", "Teacher", "Mathematics", "354-8247", "cpeltier@pausd.org", ""));
        stafflist.add(new Staff("Laurie Pennington", "Instructional Supervisor, Teacher", "Science", "354-8246", "lpennington@pausd.org", ""));
        stafflist.add(new Staff("Cindy Peters", "Instructional Supervisor, Teacher", "Career Technical Education", "354-8245", "cpeters@pausd.org", ""));
        stafflist.add(new Staff("Emily Pinkston", "Teacher", "Mathematics", "354-8247", "epinkston@pausd.org", ""));
        stafflist.add(new Staff("Ken Plough", "Teacher", "Guidance", "849-7961", "kplough@pausd.org", ""));
        stafflist.add(new Staff("Maria Powell", "Teacher", "Science", "354-8246", "mpowell@pausd.org", ""));
        stafflist.add(new Staff("Samira Rastegar", "Psychologist", "Psychologist", "354-8213", "srastegar@pausd.org", ""));
        stafflist.add(new Staff("Chris Redfield", "Teacher", "Mathematics", "354-8247", "credfield@pausd.org", "Webpage"));
        stafflist.add(new Staff("Joel-Nicole Reid", "Counselor", "Guidance", "", "jreid@pausd.org", ""));
        stafflist.add(new Staff("Daisy Renazco", "Teacher, (ToSA)", "Mathematics", "354-8247", "drenazco@pausd.org", "Webpage"));
        stafflist.add(new Staff("Isabel Romo", "Kitchen staff", "Food Services", "354-8233", "iromo@pausd.org", ""));
        stafflist.add(new Staff("Cora Ross", "Counselor", "Guidance", "354-8290", "cross@pausd.org", ""));
        stafflist.add(new Staff("Richard Rullo", "Teacher", "Special Education", "354-8230", "rrullo@pausd.org", ""));
        stafflist.add(new Staff("Kim Sabbag", "Teacher", "Physical Education", "354-8215", "ksabbag@pausd.org", ""));
        stafflist.add(new Staff("Shirley Sachs", "Secretary", "Support Staff", "354-8282", "ssachs@pausd.org", ""));
        stafflist.add(new Staff("Ernesto Salcedo", "Custodian", "Custodial", "354-8205", "esalcedo@pausd.org", ""));
        stafflist.add(new Staff("Jorge Sanchez", "Campus Supervisor", "Support Staff", "354-8288", "jsanchez@pausd.org", ""));
        stafflist.add(new Staff("Karen Saxena", "Teacher", "Mathematics", "354-8247", "ksaxena@pausd.org", ""));
        stafflist.add(new Staff("Elizabeth Schnackenberg", "Teacher", "Special Education", "354-8230", "eschnackenberg@pausd.org", ""));
        stafflist.add(new Staff("Claudia Schroeppel", "Teacher", "World Languages", "354-8241", "cschroeppel@pausd.org", ""));
        stafflist.add(new Staff("Neeti Schworetzky", "Teacher", "Science", "354-8246", "nschworetzky@pausd.org", ""));
        stafflist.add(new Staff("Jackie Selfridge", "Teacher", "Special Education", "354-8230", "jselfridge@pausd.org", ""));
        stafflist.add(new Staff("Howard Selznick", "Aide", "Special Education", "354-8250", "hselznick@pausd.org", ""));
        stafflist.add(new Staff("Jim Shelby", "Teacher", "Visual and Performing Arts", "354-8258", "jshelby@pausd.org", "Webpage"));
        stafflist.add(new Staff("Tovah Skiles", "Teacher", "World Languages", "354-8241", "tskiles@pausd.org", ""));
        stafflist.add(new Staff("Toni Smith", "Teacher", "Mathematics", "354-8247", "tsmith@pausd.org", ""));
        stafflist.add(new Staff("Shayna Stebbins", "Aide", "Special Education", "354-8230", "sstebbins@pausd.org", ""));
        stafflist.add(new Staff("Erin Stein-Wright", "Behaviorist", "Wellness", "354-8251", "esteinwright@pausd.org", ""));
        stafflist.add(new Staff("Miriam Stevenson", "Assistant Principal", "Administration", "354-8244", "mstevenson@pausd.org", ""));
        stafflist.add(new Staff("Pam Steward", "Resource Specialist", "Academic Center", "354-8271", "psteward@pausd.org", ""));
        stafflist.add(new Staff("Todd Summers", "Teacher", "Visual and Performing Arts", "354-8264", "tsummers@pausd.org", "Webpage"));
        stafflist.add(new Staff("Peggy Syvertson", "Speech Therapist", "Special Education", "354-8230", "psyvertson@pausd.org", ""));
        stafflist.add(new Staff("Daissy Tabares", "Teacher", "World Languages", "354-8241", "dtabares@pausd.org", ""));
        stafflist.add(new Staff("Jessica Tabron", "Department Secretary", "Science, Social Studies", "354-8237", "jtabron@pausd.org", ""));
        stafflist.add(new Staff("Lynn Tabuchi", "TOSA, Inclusion", "Special Education", "354-8230", "ltabuchi@pausd.org", ""));
        stafflist.add(new Staff("Jeanette Tucker", "Teacher", "Special Education", "354-8230", "jtucker@pausd.org", ""));
        stafflist.add(new Staff("Ariane Tuomy", "Teacher", "Social Studies", "354-8237", "atuomy@pausd.org", "Webpage"));
        stafflist.add(new Staff("Nestor Vidonia", "Custodian", "Custodial", "354-8205", "nvidonia@pausd.org", ""));
        stafflist.add(new Staff("Yanan Vrudny", "Teacher", "World Languages", "354-8241", "yvrudny@pausd.org", ""));
        stafflist.add(new Staff("Anna Ward", "Teacher", "Social Studies", "354-8237", "award@pausd.org", ""));
        stafflist.add(new Staff("Lettie Weinmann", "Teacher", "Science", "354-8246", "lweinmann@pausd.org", "Webpage"));
        stafflist.add(new Staff("Mark Weisman", "Teacher", "Social Studies", "354-8237", "mweisman@pausd.org", ""));
        stafflist.add(new Staff("Jordan Wells", "Teacher", "English", "354-8238", "jhuizing@pausd.org or jwells@pausd.org", ""));
        stafflist.add(new Staff("Heather Wheeler", "Assistant Principal", "Administration", "354-8206", "hwheeler@pausd.org", ""));
        stafflist.add(new Staff("Tarn Wilson", "Teacher", "English", "849-7930", "twilson@pausd.org", ""));
        stafflist.add(new Staff("Christina Woznicki", "Teacher", "Science", "354-8246", "cwoznicki@pausd.org", ""));
        stafflist.add(new Staff("Sharon Yost", "Teacher", "Special Education", "354-8250", "syost@pausd.org", "Webpage"));
        stafflist.add(new Staff("Emily Yun", "Teacher", "Mathematics", "354-8247", "eyun@pausd.org", ""));
        stafflist.add(new Staff("Myrna Zendejas", "Social Worker", "Special Programs", "354-8230", "mzendejas@pausd.org", ""));
        stafflist.add(new Staff("Elana Zizmor", "Teacher", "Science", "354-8246", "ezizmor@pausd.org", "Webpage"));
    }
}
