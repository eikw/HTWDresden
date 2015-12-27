package de.htwdd.htwdresden;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import de.htwdd.htwdresden.interfaces.IToolbarTitel;

/**
 * Hinweis zum Navigation Drawer:
 * Das Highlighting ist aktuell in der Support-Libary nicht vollständig / richtig implentiert,
 * darum manuelle Behandlung im Code
 * @see <a href="https://guides.codepath.com/android/Fragment-Navigation-Drawer#limitations">Navigation Drawer Limitations</a>
 */

public class MainActivity extends AppCompatActivity implements IToolbarTitel {
    private DrawerLayout mDrawerLayout;
    private MenuItem mPreviousMenuItem;
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar actionBar;
    private NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Markiere aktuelles Feld
            if (mPreviousMenuItem != null) {
                mPreviousMenuItem.setChecked(false);
            }
            mPreviousMenuItem = item;
            item.setChecked(true);

            // Ändere Inhalt
            selectItem(item.getItemId());

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar einfügen
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Hole Views
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        // Actionbar Titel anpassen
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                // Schließe Tastatur
                if (getCurrentFocus() != null && getCurrentFocus() instanceof EditText) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        setupDrawerContent(mNavigationView);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Setze Start-Fragment
        if (savedInstanceState == null) {
            onNavigationItemSelectedListener.onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.navigation_overview));
            selectItem(R.id.navigation_overview);
            mPreviousMenuItem = mNavigationView.getMenu().findItem(R.id.navigation_overview);
            mPreviousMenuItem.setChecked(true);
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(onNavigationItemSelectedListener);
    }

    private void selectItem(int position) {
        Fragment fragment;
        String tag = null;

        FragmentManager fragmentManager = getFragmentManager();

        switch (position) {
            case R.id.navigation_overview:
                fragment = new Fragment();
                tag = "overview";
                break;
            case R.id.navigation_mensa:
                fragment = new MensaFragment();
                break;
            case R.id.navigation_settings:
                fragment = new SettingsFragment();
                break;
            case R.id.navigation_about:
                fragment = new AboutFragment();
                break;
            case R.id.navigation_uni_administration:
                fragment = new ManagementFragment();
                break;
            default:
                fragment = new Fragment();
                break;
        }

        // Lösche BackStack, ansonsten kommt es zu Überblendungen wenn Menü-Auswahl und Backtaste verwendet wird.
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Fragment ersetzen
        fragmentManager.beginTransaction().replace(R.id.activity_main_FrameLayout, fragment, tag).commit();

        // NavigationDrawer schliesen
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager.getBackStackEntryCount() == 0)
            // Wenn das Übersichtsfragment das aktuelle ist, die App beenden
            if (fragmentManager.findFragmentByTag("overview") != null)
                finish();
            else {
                // Zur Übersichtsseite springen
                NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
                onNavigationItemSelectedListener.onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.navigation_overview));
            }
        else fragmentManager.popBackStack();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mPreviousMenuItem != null)
            outState.putInt("mPreviousMenuItem", mPreviousMenuItem.getItemId());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
            if (savedInstanceState.containsKey("mPreviousMenuItem")) {
                NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
                mPreviousMenuItem = mNavigationView.getMenu().findItem(R.id.navigation_overview);
                mPreviousMenuItem.setChecked(false);
                mPreviousMenuItem = mNavigationView.getMenu().findItem(savedInstanceState.getInt("mPreviousMenuItem"));
                mPreviousMenuItem.setChecked(true);
                Log.d("Activity", "Markiere: " + mPreviousMenuItem.getTitle());
            }
    }

    @Override
    public void setTitle(String title) {
        if (title == null || title.isEmpty())
            actionBar.setTitle(R.string.app_name);
        else actionBar.setTitle(title);
    }
}
