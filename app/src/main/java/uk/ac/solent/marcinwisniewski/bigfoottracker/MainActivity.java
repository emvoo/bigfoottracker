package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import uk.ac.solent.marcinwisniewski.bigfoottracker.db.DatabaseHelper;
import uk.ac.solent.marcinwisniewski.bigfoottracker.db.Step;
import uk.ac.solent.marcinwisniewski.bigfoottracker.repositories.DateTimeRepository;
import uk.ac.solent.marcinwisniewski.bigfoottracker.services.StepsService;

/**
 * Main class where magic begins ;)
 */
public class MainActivity extends AppCompatActivity {
    private int menu_item_id;
    public DatabaseHelper db;

    private LocationManager locationManager;
    private double latitude, longitude, altitude;
    private int stepCounter;
    private DateTimeRepository dateTimeRepository;

    private SharedPreferences prefs;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    public static final int MULTIPLE_PERMISSIONS = 4;
    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    private NotificationCompat.Builder notification;
    private int uniqueNotificationId;

    /*
     *
     *      ACTIVITY LIFECYCLE METHODS
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dateTimeRepository = new DateTimeRepository();
        initDatabase(); // start database connection
        initUI(savedInstanceState); // load UI elements
        checkPermissions(); // check needed permissions
        initLocation(); // initialize location services
        startService(new Intent(MainActivity.this, StepsService.class)); // start service to allow steps taking functionality

        // notifications section
        uniqueNotificationId = 987654321;
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);
        stepCounter = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestLocUpdates();
        LocalBroadcastManager.getInstance(this).registerReceiver(stepsReceiver, new IntentFilter("new-step"));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stepsReceiver);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (!getKeepTrackPreference()) {
            db.clearDatabase();
        }
        db.closeDB();
        stopLocationListener();
        super.onDestroy();
    }


    /**
     * Back button press actions.
     */
    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 1) {
            manager.popBackStack();
        } else {
            this.finish();
            return;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("menu_item_id", menu_item_id);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        menu_item_id = savedInstanceState.getInt("menu_item_id");
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Initiates database class.
     */
    private void initDatabase() {
        db = new DatabaseHelper(getApplicationContext());
    }

    /**
     * checks preferences value.
     *
     * @return boolean
     */
    public boolean getKeepTrackPreference() {
        return prefs.getBoolean("dbstore", true);
    }

    /**
     * Initiates UI.
     *
     * @param savedInstanceState
     */
    private void initUI(Bundle savedInstanceState) {
        // load layout file
        setContentView(R.layout.activity_main);
        // load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // initiate a DrawerLayout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawerLayout != null) {
            drawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));

            mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                    R.string.drawer_open, R.string.drawer_close) {

                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    getSupportActionBar().setTitle(getTitle());
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getSupportActionBar().setTitle(getTitle());
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };

            // Set the drawer toggle as the DrawerListener
            drawerLayout.addDrawerListener(mDrawerToggle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        ListView navigation_items = (ListView) findViewById(R.id.navigation_items);
        if (navigation_items != null) {
            navigation_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectItem(position);
                }
            });
        }
        showDisplay(savedInstanceState);
    }

    /**
     * Loads fragment based on selected item from drawer.
     *
     * @param position
     */
    private void selectItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new DashboardFragment();
                break;
            case 1:
                fragment = new MapFragment();
                break;
            case 2:
                fragment = new AltitudeFragment();
                break;
        }
        makeTransition(fragment, true);
    }

    /**
     * Sync the toggle state after onRestoreInstanceState has occurred.
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerLayout != null) {
            mDrawerToggle.syncState();
        }
    }

    /**
     * Initializes GPS.
     */
    private void initLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(!isGPSEnabled){
            // show alert dialog informing GPS is not enabled.
            showSettingsAlert();
        } else {
            requestLocUpdates();
        }
    }

    /**
     * Displays alert message if GPS is not enabled.
     */
    public void showSettingsAlert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Populate toolbar buttons.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_buttons, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handle left menu click actions.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerLayout != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_preferences:
                Intent preferences = new Intent(this, PreferencesActivity.class);
                startActivity(preferences);
                return true;
            case R.id.action_share:
                share();
                return true;
            case R.id.action_about:
                Intent aboutActivity = new Intent(this, AboutActivity.class);
                startActivity(aboutActivity);
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Share method.
     */
    public void share() {
        String summary = getDailySummary();
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, summary);
        startActivity(sharingIntent);
    }

    /**
     * Prepares message to be shared.
     *
     * @return String
     */
    public String getDailySummary() {
        // today's fields
        DateTimeRepository dtr = new DateTimeRepository();
        String today = dtr.getCurrentDate();
        double distance = db.countDistanceByDay(today);
        long steps = db.countAllStepsByDay(today);
        int calories = this.calculateCalories(steps);
        String appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
        String myString = "Hey! I walked today " + distance + "m taking " + steps + " steps and I've burnt " + calories + " kcal. I know that thanks to " + appName + " app.";

        return myString;
    }

    /**
     * Loads fragments.
     *
     * @param savedInstanceState
     */
    public void showDisplay(Bundle savedInstanceState) {
        ListView navigation_items = (ListView) findViewById(R.id.navigation_items);
        if (navigation_items != null) {
            if(navigation_items.getVisibility() == View.INVISIBLE){
                navigation_items.setVisibility(View.VISIBLE);
            }
        }
        // get number of users from db
        int count = db.getAllUsers().size();
        // check if there is account data in database
        if (count == 0) {
            if (getKeepTrackPreference()) {
                // no user data
                // load register fragment on first run
                makeTransition(new RegisterFragment(), false);
                // hide side panel
                if (drawerLayout != null) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
                if (navigation_items != null) {
                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                        navigation_items.setVisibility(View.INVISIBLE);
                    }
                }
            } else {
                continuesDisplaying(savedInstanceState);
            }
        } else {
            continuesDisplaying(savedInstanceState);
        }
    }

    /**
     * Loads fragment into activity frame.
     *
     * @param savedInstanceState
     */
    private void continuesDisplaying(Bundle savedInstanceState) {
        // user data exist in db
        // load navigation drawer
        if (drawerLayout != null) {
            setNavigationDrawer();
        }

        if (savedInstanceState != null && savedInstanceState.getInt("menu_item_id") != 0)
        {
            // load selected fragment on screen orientation
            menu_item_id = savedInstanceState.getInt("menu_item_id");
            Fragment fragment = instantiateFragment(menu_item_id);
            makeTransition(fragment, true);
        } else {
            // load users dashboard
            makeTransition(new DashboardFragment(), true);
        }
    }

    /**
     * Checks and loads/requires permissions.
     *
     * @return boolean
     */
    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }

    /**
     * Load navigation drawer.
     */
    private void setNavigationDrawer() {
        // initiate a Navigation View
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        // set setNavigationItemSelectedListener event on NavigationView
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                // get selected menu item's id
                menu_item_id = menuItem.getItemId();

                // create a Fragment Object
                Fragment fragment = instantiateFragment(menu_item_id);
                // check if fragment is null
                if (fragment != null) {
                    // load fragment
                    makeTransition(fragment, true);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Requests location updates.
     */
    public void requestLocUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
        } catch (SecurityException e) {
            String error = "Location could not be updated for the following reasons: " + e.getMessage();
            Log.e("MainActivity", error);
        }
    }

    /**
     * Location listener required to keep users position updated (centered)
     */
    private LocationListener myLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateLoc(location);
            centerMap();
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    /**
     * Stops location updates.
     */
    public void stopLocationListener() { locationManager.removeUpdates(myLocationListener); }

    /**
     * Sets longitude, latitude and altitude.
     *
     * @param location
     */
    private void updateLoc(Location location){
        // get current location and latitude
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitude = location.getAltitude();
    }

    /**
     * Instantiate fragment depending on clicked item in navigation menu.
     *
     * @param item_id
     * @return Fragment
     */
    private Fragment instantiateFragment(int item_id) {
        Fragment fragment = null;
        switch (item_id) {
            case R.id.dashboard_menu_item:
                fragment = new DashboardFragment();
                break;
            case R.id.map:
                fragment = new MapFragment();
                break;
            case R.id.altitude:
                fragment = new AltitudeFragment();
                break;
        }
        return fragment;
    }

    /**
     * Load new fragment into activity frame.
     *
     * @param fragment
     */
    private void makeTransition(Fragment fragment, boolean addBackStack) {
        String clsName = fragment.getClass().getSimpleName();
        String tag = null;
        switch (clsName) {
            case "DashboardFragment":
            case "MapFragment":
            case "AltitudeFragment":
                tag = parseClassName(clsName);
                break;
            default:
                break;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // set custom animations
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        if (addBackStack) {
            // replace a Fragment with Frame Layout
            transaction.replace(R.id.frame, fragment, tag).addToBackStack(null);
        } else {
            // replace a Fragment with Frame Layout
            transaction.replace(R.id.frame, fragment, tag);
        }
        // commit the changes
        transaction.commit();

        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(drawerLayout.LOCK_MODE_UNLOCKED);
            drawerLayout.closeDrawers();
        }
    }

    /**
     * Converts class name to tag.
     *
     * @param clsName
     * @return
     */
    private String parseClassName(String clsName) {
        String tag;
        String[] parts = clsName.split("(?=[A-Z])");
        tag = parts[1].toLowerCase();
        return tag;
    }

    /**
     * Latitude attribute getter.
     *
     * @return double latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Longitude attribute getter.
     *
     * @return double longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Altitude attribute getter.
     *
     * @return double altitude
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * Attempts to calculate distance between two steps taken.
     *
     * @param lat
     * @param lon
     * @return
     */
    public double getDistance(double lat, double lon) {
        double distance = 0.75; // distance measured in meters and 0.75 is average adult step length

        DateTimeRepository dtr = new DateTimeRepository();
        String todayDate = dtr.getCurrentDate();

        Cursor lastInsert = db.getLastStep();
        if (lastInsert != null) {
            String lastDate = db.getLastInsertDate(lastInsert);
            if (lastDate.equals(todayDate)) {
                double previousStepLat = db.getPreviousStepLatitude(lastInsert);
                double previousStepLon = db.getPreviousStepLongitude(lastInsert);
                if (previousStepLat != 0 && previousStepLon != 0) {
                    Location previousStepLocation = new Location(LocationManager.GPS_PROVIDER);
                    previousStepLocation.setLatitude(previousStepLat);
                    previousStepLocation.setLongitude(previousStepLon);

                    Location thisStepLocation = new Location(LocationManager.GPS_PROVIDER);
                    thisStepLocation.setLatitude(lat);
                    thisStepLocation.setLongitude(lon);
                    distance = previousStepLocation.distanceTo(thisStepLocation) / 10;
                    if (distance > 1.0) {
                        distance = 1;
                    }
                    distance = Math.round(distance * 100.0) / 100.0;
                }
            }
        }
        return distance;
    }

    /**
     * Receiver class responsible for receiving messages from StepsService.
     */
    private BroadcastReceiver stepsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // if message is not empty
            if (message != null) {
                stepCounter++;
                // get location latitude and longitude
                double lat = getLatitude();
                double lon = getLongitude();
                //save step to database
                double dist = getDistance(lat, lon);
                Step step = new Step();
                step.setStep(1);
                step.setLatitude(lat);
                step.setLongitude(lon);
                step.setAltitude(getAltitude());
                step.setDistance(dist);
                step.setDate_created(dateTimeRepository.getCurrentDate());
                step.setTime_created(dateTimeRepository.getCurrentTime());

                db.createStep(step);

                // update totals fields in dashboard fragment
                updateDashboard();
                // center map to newest location
                centerMap();

                // displays notification after every 1000 steps taken
                if (stepCounter == 1000) {
                    stepCounter = 0;
                    displayNotification();
                }
            }
        }
    };

    /**
     * Calls dashboard function and updates dashboard fragment values.
     */
    private void updateDashboard() {
        DashboardFragment dashboardFragment = (DashboardFragment) getSupportFragmentManager().findFragmentByTag("dashboard");
        if (dashboardFragment != null) {
            dashboardFragment.updateDashboardFields();
        }
    }

    /**
     * Calculates calories based on information available online
     * https://www.livestrong.com/article/320124-how-many-calories-does-the-average-person-use-per-step/
     *
     * @param steps
     * @return
     */
    private int calculateCalories(long steps) {
        return (int)steps/20;
    }
    /**
     * Centers map to current user location.
     */
    private void centerMap() {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("map");
        if (mapFragment != null) {
            mapFragment.centerMapView(getLatitude(), getLongitude());
        }
    }

    /**
     * Displays notification.
     */
    public void displayNotification() {
        notification.setSmallIcon(R.drawable.ic_bigfoot_notification);
        notification.setTicker("Wow! another 1000!");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Wow! You made it again");
        notification.setContentText("Another 1000 steps. Well done you!");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(uniqueNotificationId, notification.build());
    }
}
