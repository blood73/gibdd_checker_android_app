package ru.bloodsoft.gibddchecker.ui.base;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.ui.AboutActivity;
import ru.bloodsoft.gibddchecker.ui.EaistoActivity;
import ru.bloodsoft.gibddchecker.ui.ExampleReportActivity;
import ru.bloodsoft.gibddchecker.ui.FinesActivity;
import ru.bloodsoft.gibddchecker.ui.FsspActivity;
import ru.bloodsoft.gibddchecker.ui.FullReportActivity;
import ru.bloodsoft.gibddchecker.ui.HistoryActivity;
import ru.bloodsoft.gibddchecker.ui.MileageActivity;
import ru.bloodsoft.gibddchecker.ui.MileageInappActivity;
import ru.bloodsoft.gibddchecker.ui.MyReportsActivity;
import ru.bloodsoft.gibddchecker.ui.PerekupAct1;
import ru.bloodsoft.gibddchecker.ui.PerekupAct2;
import ru.bloodsoft.gibddchecker.ui.PerekupAct3;
import ru.bloodsoft.gibddchecker.ui.PhoneActivity;
import ru.bloodsoft.gibddchecker.ui.PlateActivity;
import ru.bloodsoft.gibddchecker.ui.PolisActivity;
import ru.bloodsoft.gibddchecker.ui.ReestrActivity;
import ru.bloodsoft.gibddchecker.ui.SettingsActivity;
import ru.bloodsoft.gibddchecker.ui.InsuranceActivity;
import ru.bloodsoft.gibddchecker.ui.SravniAct;
import ru.bloodsoft.gibddchecker.ui.quote.ListActivity;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

/**
 * The base class for all Activity classes.
 * This class creates and provides the navigation drawer and toolbar.
 * The navigation logic is handled in {@link BaseActivity#goToNavDrawerItem(int)}
 *
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = makeLogTag(BaseActivity.class);

    protected static final int NAV_DRAWER_ITEM_INVALID = -1;

    private DrawerLayout drawerLayout;
    private Toolbar actionBarToolbar;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();
    }

    /**
     * Sets up the navigation drawer.
     */
    private void setupNavDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout == null) {
            // current activity does not have a drawer.
            return;
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {

            Menu navMenu = navigationView.getMenu();
            navigationView.setItemIconTintList(null);

            SettingsStorage settings = new SettingsStorage();
            Boolean showPhoneSearch = settings.showPhoneSearch();
            if (showPhoneSearch) {
                navMenu.findItem(R.id.nav_phone).setVisible(true);
            } else {
                navMenu.findItem(R.id.nav_phone).setVisible(false);
            }

            /*Boolean showVinDecoder = settings.showVinDecoder();
            if (showVinDecoder) {
                navMenu.findItem(R.id.nav_decoder).setVisible(true);
            } else {
                navMenu.findItem(R.id.nav_decoder).setVisible(false);
            }*/

            setupDrawerSelectListener(navigationView);
            setSelectedItem(navigationView);
        }
    }

    /**
     * Updated the checked item in the navigation drawer
     * @param navigationView the navigation view
     */
    private void setSelectedItem(NavigationView navigationView) {
        // Which navigation item should be selected?
        int selectedItem = getSelfNavDrawerItem(); // subclass has to override this method
        navigationView.setCheckedItem(selectedItem);
    }

    /**
     * Creates the item click listener.
     * @param navigationView the navigation view
     */
    private void setupDrawerSelectListener(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        drawerLayout.closeDrawers();
                        onNavigationItemClicked(menuItem.getItemId());
                        return true;
                    }
                });
    }

    /**
     * Handles the navigation item click.
     * @param itemId the clicked item
     */
    private void onNavigationItemClicked(final int itemId) {
        if(itemId == getSelfNavDrawerItem()) {
            // Already selected
            closeDrawer();
            return;
        }

        goToNavDrawerItem(itemId);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Handles the navigation item click and starts the corresponding activity.
     * @param item the selected navigation item
     */
    private void goToNavDrawerItem(int item) {
        switch (item) {
            case R.id.nav_gibdd:
                startActivity(new Intent(this, ListActivity.class));
                finish();
                break;
            case R.id.nav_full_report:
                startActivity(new Intent(this, FullReportActivity.class));
                break;
            case R.id.nav_my_reports:
                startActivity(new Intent(this, MyReportsActivity.class));
                break;
            case R.id.nav_example_report:
                startActivity(new Intent(this, ExampleReportActivity.class));
                break;
            case R.id.nav_history:
                startActivity(new Intent(this, HistoryActivity.class));
                break;
            case R.id.nav_phone:
                startActivity(new Intent(this, PhoneActivity.class));
                break;
            case R.id.nav_plate:
                startActivity(new Intent(this, PlateActivity.class));
                break;
            case R.id.nav_insurance:
                startActivity(new Intent(this, InsuranceActivity.class));
                break;
            case R.id.nav_polis:
                startActivity(new Intent(this, PolisActivity.class));
                break;
            case R.id.nav_reestr:
                startActivity(new Intent(this, ReestrActivity.class));
                break;
            case R.id.nav_eaisto:
                startActivity(new Intent(this, EaistoActivity.class));
                break;
            case R.id.nav_fssp:
                startActivity(new Intent(this, FsspActivity.class));
                break;
            case R.id.nav_fines:
                startActivity(new Intent(this, FinesActivity.class));
                break;
            /*case R.id.nav_decoder:
                startActivity(new Intent(this, VinDecoderActivity.class));
                break;*/
            case R.id.nav_mileage:
                startActivity(new Intent(this, MileageActivity.class));
                break;
            case R.id.nav_mileage_inapp:
                startActivity(new Intent(this, MileageInappActivity.class));
                break;
            case R.id.perekup_1:
                startActivity(new Intent(this, PerekupAct1.class));
                break;
            case R.id.perekup_2:
                startActivity(new Intent(this, PerekupAct2.class));
                break;
            case R.id.perekup_3:
                startActivity(new Intent(this, PerekupAct3.class));
                break;
            case R.id.sravni:
                startActivity(new Intent(this, SravniAct.class));
                break;
            case R.id.nav_vk:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://vk.com/antiperekup_app"));
                startActivity(i);
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
    }

    /**
     * Provides the action bar instance.
     * @return the action bar.
     */
    protected ActionBar getActionBarToolbar() {
        if (actionBarToolbar == null) {
            actionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (actionBarToolbar != null) {
                setSupportActionBar(actionBarToolbar);
            }
        }
        return getSupportActionBar();
    }


    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses
     * have to override this method.
     */
    protected int getSelfNavDrawerItem() {
        return NAV_DRAWER_ITEM_INVALID;
    }

    protected void openDrawer() {
        if (drawerLayout == null) {
            return;
        }

        drawerLayout.openDrawer(GravityCompat.START);
    }

    protected void closeDrawer() {
        if (drawerLayout == null) {
            return;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public abstract boolean providesActivityToolbar();

    public void setToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}