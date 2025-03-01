package com.example.qrapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This class is the MainActivity from wish the application begins when it loads
 */
public class MainActivity extends AppCompatActivity implements MainFragment.Scrollable, HelperMapFragment.HMFListener, SearchFragment.Scrollable {

    BottomAppBar bottomAppBar; //solely to hide nav bar when scrolling

    BottomNavigationView nav_bar;//nav bar object
    ImageButton SCAN;// scan button object
    ImageButton MYPROFILE;// get to myprofile page
    ImageView BACK;// get back to main fragment from Leaderboard

    FirebaseFirestore DB;
    FirebaseAuth Auth;

    String userID;
    String username;
    String UsernameBundleKey = "UB";
    String UserIDBundleKey = "ID";

    //display fragments
    Fragment selected;


    /**
     * The onCreate method sets important attributes and objects required for the application and controls
     * main navigation across the application.
     * Some objects include the Scan button, the bottom navigation bar and the MYprofile button. Since
     * the Main Activity is the first Activity executed, it also checks for valid users. If there is no current user account,
     * the user is taken to the SignUp page. Otherwise, the application is ready for use.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //code to prevent listview pushing
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);


        //bottom app bar
        bottomAppBar = findViewById(R.id.bottom_nav_bar);

        bottomAppBar.setHideOnScroll(true);


        //set profile button
        MYPROFILE = findViewById(R.id.button_MYprofile);
        //set scan button
        SCAN = findViewById(R.id.button_scan);
        //set back to main button
        BACK = findViewById(R.id.button_backtomain);
        BACK.setVisibility(View.GONE);//visible only in rank fragment

        //set nav_bar
        nav_bar = findViewById(R.id.nav_barview);
        nav_bar.setOnItemSelectedListener(navbar_listener);

        // start at menu
        nav_bar.setSelectedItemId(R.id.main_tab);


        //database
        Auth = FirebaseAuth.getInstance();
        DB = FirebaseFirestore.getInstance();


        //get username to use for MapFragment
        username = "----";


        Auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    // User is not signed in, force them to sign up using Firebase
                    System.out.println("User is not signed in");
                    startActivity(new Intent(MainActivity.this, SignUpActivity.class));

                } else {

                    userID = currentUser.getUid();//get username to use for MapFragment
                    setUsername(userID);

                    System.out.println("User is signed in");
                    currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser updatedUser = Auth.getCurrentUser();
                                if (updatedUser == null) {
                                    // User account has been deleted
                                    System.out.println("User account has been deleted");
                                }
                            }
                        }
                    });
                }
            }
        });


        //SCAN BUTTON
        //This takes the player to the Scanning[and Picture] activity
        SCAN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NEW SCANNING ACTIVITY, might be easier to use an activity for this one
                //Toast.makeText(MainActivity.this, "scanow", Toast.LENGTH_SHORT).show();
                Intent ScanIntent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(ScanIntent);
            }
        });


        // start at menu tab when created
        nav_bar.setSelectedItemId(R.id.main_tab);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, new MainFragment())
                .commit();

        //MYPROFILE BUTTON
        //This takes the player to the MyProfile activity
        MYPROFILE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start myprofile activity
                Intent myprofileIntent = new Intent(MainActivity.this, MyProfile.class);
                startActivity(myprofileIntent);
            }
        });

        //go back to main from rank
        BACK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomAppBar.setVisibility(View.VISIBLE);
                SCAN.setVisibility(View.VISIBLE);
                BACK.setVisibility(View.GONE);
                nav_bar.setSelectedItemId(R.id.main_tab);
            }
        });

    }







    //THE NAVIGATION BAR items
    NavigationBarView.OnItemSelectedListener navbar_listener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            return false;


            selected = new MainFragment();
            //Fragment selected = null;
            // not really necessary to initially set to anything but places emphasis that the app loads to main fragment


            /*Overview:
            Code works by creating a new, respective frament when each of the items are clicked
            The clicked fragment is commited to the "frame" of the Main Activity->see activity_main.xml for frame
            There is always at least and only 1 fragment chosen at any given time.
            This is the fragment that is commited
             */


            switch (item.getItemId()){
                case R.id.leaderboard_tab:
                    hideBottomNavBar();//temporarily
                    selected = new RankFragment();
                    break;

                case R.id.main_tab:
                    selected = new MainFragment();
                    break;

                case R.id.map_tab:
                    selected = new MapFragment();

                    //passing the userID and player name so no need to query later in map
                    Bundle UsernameBundle = new Bundle();
                    UsernameBundle.putString(UsernameBundleKey,username);
                    UsernameBundle.putString(UserIDBundleKey, userID);
                    selected.setArguments(UsernameBundle);

                    break;

                case R.id.search_tab:
                    selected = new SearchFragment();
                    break;
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame, selected).commit();//SHOW FRAGMENT

            return true;
        }

    };

    /**
     * This method hides the bottom nav bar options, used when showing leaderboard
     */
    private void hideBottomNavBar() {
        bottomAppBar.setVisibility(View.INVISIBLE);
        SCAN.setVisibility(View.INVISIBLE);
        BACK.setVisibility(View.VISIBLE);
    }

    //hide bottom nva bar when scrolling sown

    /**
     * This method sets the visibility of the bottom app bar options when scrolling on a long list
     * @param scrollState
     */
    @Override
    public void Scrollable(int scrollState) {
        Log.d("INTERFACE",String.format("I got %d",scrollState));
        if(scrollState==2){
            bottomAppBar.setVisibility(View.INVISIBLE);
        }else{
            bottomAppBar.setVisibility(View.VISIBLE);
        }
    }


    /**
     * This method sets the username of the user to be displayed on the map
     * @param userID the user's unique ID
     */
    private void setUsername(String userID){
        DB.collection("Users").document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {username = document.getString("username");}
                //else {Toast.makeText(getContext(), "User Document doesnt exist", Toast.LENGTH_SHORT).show();}
            }else {
                Log.d("Main Acitivity", "Could not set username because user does not exist");
                //Toast.makeText(getContext(), "Username Task Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * This method updates the display for number of codes on the map
     * once a marker is removed from the map
     * @param lost
     */
    @Override
    public void onRemovedMarker(boolean lost) {
        if(selected instanceof MapFragment && lost){
            TextView ptsView = ((MapFragment) selected).points;
            int curr = Integer.parseInt(ptsView.getText().toString());
            if(curr>0){
                ptsView.setText(String.valueOf(curr-1));
            }
        }
    }
}

/*CITATIONS

1)Scrolling interface to hide nav bar sometimes x2
https://stackoverflow.com/questions/16791100/detect-scroll-up-scroll-down-in-listview
https://stackoverflow.com/questions/9343241/passing-data-between-a-fragment-and-its-container-activity


2)Soft keyboard settings(prevent view pushing. Instead, the keyboard is on top of the view)
https://stackoverflow.com/questions/4207880/android-how-do-i-prevent-the-soft-keyboard-from-pushing-my-view-up


3)the layout of the nav bar original
https://www.youtube.com/watch?v=x6-_va1R788

4)general nav bar design principles
https://m2.material.io/components/bottom-navigation/android
https://m2.material.io/components/app-bars-bottom#usage


5)making a nav bar with fragments
https://www.youtube.com/watch?v=OV25x3a55pk


6)Multiple interfaces and fragment communication with interfaces
https://codinginflow.com/tutorials/android/fragment-to-fragment-communication-with-interfaces
https://stackoverflow.com/questions/21263607/can-a-normal-class-implement-multiple-interfaces

 */