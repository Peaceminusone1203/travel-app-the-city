package app.thecity.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import app.thecity.AppConfig;
import app.thecity.R;
import app.thecity.advertise.AdNetworkHelper;
import app.thecity.data.DatabaseHandler;
import app.thecity.data.SharedPref;
import app.thecity.fragment.FragmentCategory;
import app.thecity.fragment.HomeFragment;
import app.thecity.utils.PermissionUtil;
import app.thecity.utils.Tools;

public class ActivityMain extends AppCompatActivity {

    public ActionBar actionBar;
    public Toolbar toolbar;
    private int[] cat;
    private NavigationView navigationView;
    private DatabaseHandler db;
    private SharedPref sharedPref;
    private RelativeLayout nav_header_lyt;

    static ActivityMain activityMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityMain = this;

        db = new DatabaseHandler(this);
        sharedPref = new SharedPref(this);

        initToolbar();
        initDrawerMenu();
        cat = getResources().getIntArray(R.array.id_category);

        // first drawer view
        onItemSelected(R.id.nav_all, getString(R.string.title_nav_all));

        // Permission Notification
        PermissionUtil.checkAndRequestNotification(this);

        // for system bar in lollipop
        Tools.systemBarLolipop(this);
        Tools.RTLMode(getWindow());

        updateCurrentPosition();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        Tools.setActionBarColor(this, actionBar);
    }

    private void initDrawerMenu() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                updateFavoritesCounter(navigationView, R.id.nav_favorites, db.getFavoritesSize());
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                return onItemSelected(item.getItemId(), item.getTitle().toString());
            }
        });
        if (!AppConfig.general.enable_news_info) navigationView.getMenu().removeItem(R.id.nav_news);

        // navigation header
        View nav_header = navigationView.getHeaderView(0);
        nav_header_lyt = (RelativeLayout) nav_header.findViewById(R.id.nav_header_lyt);
        nav_header_lyt.setBackgroundColor(Tools.colorBrighter(sharedPref.getThemeColorInt()));
        (nav_header.findViewById(R.id.menu_nav_setting)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ActivitySetting.class);
                startActivity(i);
            }
        });

        (nav_header.findViewById(R.id.menu_nav_map)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ActivityMaps.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            doExitApp();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), ActivitySetting.class);
            startActivity(i);
        } else if (id == R.id.action_more) {
            Tools.directLinkToBrowser(this, AppConfig.general.more_apps_url);
        } else if (id == R.id.action_rate) {
            Tools.rateAction(ActivityMain.this);
        } else if (id == R.id.action_about) {
            Tools.aboutAction(ActivityMain.this);
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onItemSelected(int id, String title) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        //sub menu
        /* IMPORTANT : cat[index_array], index is start from 0
         */
        if (id == R.id.nav_all) {
            fragment = new HomeFragment();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, -1);
            actionBar.setTitle(title);
            // favorites
        } else if (id == R.id.nav_favorites) {
            fragment = new FragmentCategory();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, -2);
            actionBar.setTitle(title);
            // news info
        } else if (id == R.id.nav_news) {
            Intent i = new Intent(this, ActivityNewsInfo.class);
            startActivity(i);
        } else if (id == R.id.nav_featured) {
            fragment = new FragmentCategory();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[10]);
            actionBar.setTitle(title);
        } else if (id == R.id.nav_tour) {
            fragment = new FragmentCategory();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[0]);
            actionBar.setTitle(title);
        } else if (id == R.id.nav_food) {
            fragment = new FragmentCategory();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[1]);
            actionBar.setTitle(title);
        } else if (id == R.id.nav_hotels) {
            fragment = new FragmentCategory();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[2]);
            actionBar.setTitle(title);
        } else if (id == R.id.nav_ent) {
            fragment = new FragmentCategory();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[3]);
            actionBar.setTitle(title);
        } else if (id == R.id.nav_sport) {
            fragment = new FragmentCategory();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[4]);
            actionBar.setTitle(title);
        } else if (id == R.id.nav_shop) {
            fragment = new FragmentCategory();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[5]);
            actionBar.setTitle(title);
        } else if (id == R.id.nav_transport) {
            fragment = new FragmentCategory();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[6]);
            actionBar.setTitle(title);
        } else if (id == R.id.nav_religion) {
            fragment = new FragmentCategory();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[7]);
            actionBar.setTitle(title);
        } else if (id == R.id.nav_public) {
            fragment = new FragmentCategory();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[8]);
            actionBar.setTitle(title);
        } else if (id == R.id.nav_money) {
            fragment = new FragmentCategory();
            bundle.putInt(FragmentCategory.TAG_CATEGORY, cat[9]);
            actionBar.setTitle(title);
        }

        if (fragment != null) {
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_content, fragment);
            fragmentTransaction.commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private long exitTime = 0;

    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, R.string.press_again_exit_app, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }


    @Override
    protected void onResume() {
        updateFavoritesCounter(navigationView, R.id.nav_favorites, db.getFavoritesSize());
        if (actionBar != null) {
            Tools.setActionBarColor(this, actionBar);
            // for system bar in lollipop
            Tools.systemBarLolipop(this);
        }
        if (nav_header_lyt != null) {
            nav_header_lyt.setBackgroundColor(Tools.colorBrighter(sharedPref.getThemeColorInt()));
        }
        super.onResume();
    }

    static boolean active = false;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        active = false;
    }


    private void updateFavoritesCounter(NavigationView nav, @IdRes int itemId, int count) {
        TextView view = (TextView) nav.getMenu().findItem(itemId).getActionView().findViewById(R.id.counter);
        view.setText(String.valueOf(count));
    }

    public void updateCurrentPosition() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ android.Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        } else {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                updateCityInfo(location.getLatitude(), location.getLongitude());
            });
        }
    }

    private String updateCityInfo(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);

                TextView city = findViewById(R.id.city);
                TextView detailCity = findViewById(R.id.detail_city);

                city.setText(address.getSubAdminArea());
                detailCity.setText(address.getAdminArea() + ", " + address.getCountryName());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "-";
        }
        return "-";
    }

    public static ActivityMain getInstance() {
        return activityMain;
    }

}
