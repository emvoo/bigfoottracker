package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import uk.ac.solent.marcinwisniewski.bigfoottracker.db.UserDetailsDB;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private int item_id;
    public UserDetailsDB userDB;
    private NavigationView navigationView;
    private MenuItem dashboard_menu_item;
    public static final int MULTIPLE_PERMISSIONS = 4;

    String[] permissions= new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userDB = new UserDetailsDB(getApplicationContext());

        // load layout file
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // initiate a DrawerLayout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        checkPermissions();
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
    protected void onStart() {
        super.onStart();
        userDB.openDB();
    }

    @Override
    protected void onStop() {
        super.onStop();
        userDB.closeDB();
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

    /**
     * Instantiate fragment depending on clicked item in navigation menu.
     *
     * @param item_id
     * @return Fragment
     */
    private Fragment instantiateFragment(int item_id)
    {
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
    private void makeTransition(Fragment fragment, boolean addBackStack)
    {
        if (addBackStack) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // replace a Fragment with Frame Layout
            transaction.replace(R.id.frame, fragment).addToBackStack(null);
            // commit the changes
            transaction.commit();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // replace a Fragment with Frame Layout
            transaction.replace(R.id.frame, fragment);
            // commit the changes
            transaction.commit();
        }

        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(drawerLayout.LOCK_MODE_UNLOCKED);
            drawerLayout.closeDrawers();
        }
    }

    @Override
    public void onBackPressed()
    {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 1) {
            manager.popBackStack();
        } else {
            MainActivity.this.finish();
            return;
//            System.exit(0);
        }
    }

    /**
     * Get database instance.
     * @return UserDetailsDB
     */
    public UserDetailsDB getUserDB() {
        if (userDB == null) {
            userDB = new UserDetailsDB(getApplicationContext());
        }
        return userDB;
    }

}
