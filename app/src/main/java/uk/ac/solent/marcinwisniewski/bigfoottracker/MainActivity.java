package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.solent.marcinwisniewski.bigfoottracker.db.StepsDB;
import uk.ac.solent.marcinwisniewski.bigfoottracker.db.UserDetailsDB;
import uk.ac.solent.marcinwisniewski.bigfoottracker.services.StepsService;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private int item_id;
    public UserDetailsDB userDB;
    public StepsDB stepsDB;
    private NavigationView navigationView;
    private MenuItem dashboard_menu_item;
    public static final int MULTIPLE_PERMISSIONS = 4;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private double latitude, longitude, altitude;

    private String[] permissions= new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatabases();
        initUI(savedInstanceState);
        checkPermissions();
        initLocation();
        startService(new Intent(MainActivity.this, StepsService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestLocUpdates();
        LocalBroadcastManager.getInstance(this).registerReceiver(stepsReceiver, new IntentFilter("new-step"));
    }

    @Override
    protected void onPause() {
        stopLocationListener();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stepsReceiver);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        stepsDB.openDB();
        userDB.openDB();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stepsDB.closeDB();
        userDB.closeDB();
    }

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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permissions granted.
                }
//                else {
//                    String permissions = "";
//                    for (String per : permissionsList) {
//                        permissions += "\n" + per;
//                    }
//                    // permissions list of don't granted permission
//                }
                return;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("menu_item_id", item_id);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        item_id = savedInstanceState.getInt("menu_item_id");
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void initDatabases() {
        userDB = new UserDetailsDB(getApplicationContext());
        stepsDB = new StepsDB(getApplicationContext());
    }

    private void initUI(Bundle savedInstanceState) {
        // load layout file
        setContentView(R.layout.activity_main);
        // load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // initiate a DrawerLayout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        showDisplay(savedInstanceState);
    }

    private void initLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(!isGPSEnabled){
            // show alert dialog informing GPS is not enabled.
            showSettingsAlert();
        } else {
            requestLocUpdates();
        }
    }

    // display alert message if GPS is not enabled
    public void showSettingsAlert(){
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

    public void showDisplay(Bundle savedInstanceState) {
        // get number of users from db
        int count = userDB.getAllUsers().getCount();
        // check if there is account data in database
        if (count == 0) {
            // no user data
            // load register fragment on first run
            makeTransition(new RegisterFragment(), false);
            // hide side panel
            drawerLayout.setDrawerLockMode(drawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            // user data exist in db
            // load navigation drawer
            setNavigationDrawer();


            // check if
            if (savedInstanceState != null && savedInstanceState.getInt("menu_item_id") != 0)
            {
                // load selected fragment on screen orientation
                item_id = savedInstanceState.getInt("menu_item_id");
                Fragment fragment = instantiateFragment(item_id);
                makeTransition(fragment, true);
            } else {
                // load users dashboard
                makeTransition(new DashboardFragment(), true);
            }
        }
    }

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
        navigationView = (NavigationView) findViewById(R.id.navigation);
        // set setNavigationItemSelectedListener event on NavigationView
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                // get selected menu item's id
                item_id = menuItem.getItemId();

                // create a Fragment Object
                Fragment fragment = instantiateFragment(item_id);
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

    public void requestLocUpdates(){
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
        } catch (SecurityException e) {
            String error = "Location could not be updated for the following reasons: " + e.getMessage();
            Log.e("MainActivity", error);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    public void stopLocationListener(){ locationManager.removeUpdates(myLocationListener); }

    // location listener required to keep users position updated (centered)
    private LocationListener myLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateLoc(location);
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

    // center map to users current position
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
     * Load new fragment.
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
        if (addBackStack) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // replace a Fragment with Frame Layout
            transaction.replace(R.id.frame, fragment, tag).addToBackStack(null);
            // commit the changes
            transaction.commit();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // replace a Fragment with Frame Layout
            transaction.replace(R.id.frame, fragment, tag);
            // commit the changes
            transaction.commit();
        }

        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(drawerLayout.LOCK_MODE_UNLOCKED);
            drawerLayout.closeDrawers();
        }
    }

    private String parseClassName(String clsName) {
        String tag = "";
        String[] parts = clsName.split("(?=[A-Z])");
        tag = parts[1].toLowerCase();
        displayToast(tag);
        return tag;
    }

    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    private BroadcastReceiver stepsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // if message is not empty
            if (message != null) {
                //save step to database
                stepsDB.insert(getLatitude(), getLongitude(), getAltitude());
                // update totals fields in dashboard fragment
                DashboardFragment dashboardFragment = (DashboardFragment) getSupportFragmentManager().findFragmentByTag("dashboard");
                dashboardFragment.updateDashboardFields();
            }
        }
    };
}
