package app.thecity.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import app.thecity.AppConfig;
import app.thecity.R;
import app.thecity.adapter.AdapterImageList;
import app.thecity.advertise.AdNetworkHelper;
import app.thecity.connection.RestAdapter;
import app.thecity.connection.callbacks.CallbackPlaceDetails;
import app.thecity.data.Constant;
import app.thecity.data.DatabaseHandler;
import app.thecity.data.SharedPref;
import app.thecity.data.ThisApplication;
import app.thecity.model.Images;
import app.thecity.model.Place;
import app.thecity.utils.Tools;
import retrofit2.Call;
import retrofit2.Response;

public class ActivityPlaceDetail extends AppCompatActivity {

    private static final String EXTRA_OBJ = "key.EXTRA_OBJ";
    private static final String EXTRA_NOTIF_FLAG = "key.EXTRA_NOTIF_FLAG";

    // give preparation animation activity transition
    public static void navigate(AppCompatActivity activity, View sharedView, Place p) {
        Intent intent = new Intent(activity, ActivityPlaceDetail.class);
        intent.putExtra(EXTRA_OBJ, p);
        ActivityCompat.startActivity(activity, intent, null);
    }

    public static Intent navigateBase(Context context, Place obj, Boolean from_notif) {
        Intent i = new Intent(context, ActivityPlaceDetail.class);
        i.putExtra(EXTRA_OBJ, obj);
        i.putExtra(EXTRA_NOTIF_FLAG, from_notif);
        return i;
    }

    private Place place = null;
    private ImageView favouriteButton;
    private WebView description = null;
    private View parent_view = null;
    private GoogleMap googleMap;
    private DatabaseHandler db;

    private boolean onProcess = false;
    private boolean isFromNotif = false;
    private Call<CallbackPlaceDetails> callback;
    private View lyt_progress;
    private View lyt_distance;
    private float distance = -1;
    private Snackbar snackbar;
    private ArrayList<String> new_images_str = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_detail_layout);
        parent_view = findViewById(android.R.id.content);

        db = new DatabaseHandler(this);

        place = (Place) getIntent().getSerializableExtra(EXTRA_OBJ);
        isFromNotif = getIntent().getBooleanExtra(EXTRA_NOTIF_FLAG, false);

        findViewById(R.id.back).setOnClickListener(e -> backAction());
//        findViewById(R.id.action_share).setOnClickListener(e -> );

        setImageGallery(db.getListImageByPlaceId(place.place_id));

        FrameLayout gallery = findViewById(R.id.gallery);
        gallery.setOnClickListener(e -> openImageGallery(0));

        TextView destination = findViewById(R.id.destination_name);
        destination.setText(place.name);

        TextView address = findViewById(R.id.address);
        address.setText(place.address);

        favouriteButton = findViewById(R.id.favourite_fab);
        favouriteButton.setOnClickListener((e) -> {
            if (db.isFavoritesExist(place.place_id)) {
                db.deleteFavorites(place.place_id);
                Snackbar.make(parent_view, place.name + " " + getString(R.string.remove_favorite), Snackbar.LENGTH_SHORT).show();
                // analytics tracking
                ThisApplication.getInstance().trackEvent(Constant.Event.FAVORITES.name(), "REMOVE", place.name);
            } else {
                db.addFavorites(place.place_id);
                Snackbar.make(parent_view, place.name + " " + getString(R.string.add_favorite), Snackbar.LENGTH_SHORT).show();
                // analytics tracking
                ThisApplication.getInstance().trackEvent(Constant.Event.FAVORITES.name(), "ADD", place.name);
            }
            fabToggle();
        });

        TextView rating = findViewById(R.id.rating);
        rating.setText(place.rating + "");

        TextView distance = findViewById(R.id.distance);
        distance.setText(Math.round(place.distance) + " km");

        TextView telephone = findViewById(R.id.telephone);
        telephone.setText(place.phone);

        TextView description = findViewById(R.id.description);
        description.setText(place.description);

        if (place.image != null) {
            Tools.displayImage(this, (ImageView) findViewById(R.id.image_gallery), Constant.getURLimgPlace(place.image));
        }

        fabToggle();
        initMap();

        // for system bar in lollipop
        Tools.systemBarLolipop(this);
        Tools.RTLMode(getWindow());

        // analytics tracking
        ThisApplication.getInstance().trackScreenView("View place : " + (place.name == null ? "name" : place.name));
    }

    @Override
    protected void onResume() {
//        loadPlaceData();
        if (description != null) description.onResume();
        super.onResume();
    }

    // this method name same with android:onClick="clickLayout" at layout xml
    private void setImageGallery(List<Images> images) {
        // add optional image into list
        List<Images> new_images = new ArrayList<>();
        new_images.add(new Images(place.place_id, place.image));
        new_images.addAll(images);
        new_images_str = new ArrayList<>();
        for (Images img : new_images) {
            new_images_str.add(Constant.getURLimgPlace(img.name));
        }
    }

    private void openImageGallery(int position) {
        Intent i = new Intent(ActivityPlaceDetail.this, ActivityFullScreenImage.class);
        i.putExtra(ActivityFullScreenImage.EXTRA_POS, position);
        i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, new_images_str);
        startActivity(i);
    }

    private void fabToggle() {
        if (db.isFavoritesExist(place.place_id)) {
            favouriteButton.setImageResource(R.drawable.ic_favourite_fill_rounded);
        } else {
            favouriteButton.setImageResource(R.drawable.ic_not_favourite_fill_rounded);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_details, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            backAction();
            return true;
        } else if (id == R.id.action_share) {
            if (!place.isDraft()) {
                Tools.methodShare(ActivityPlaceDetail.this, place);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMap() {
        if (googleMap == null) {
            MapFragment mapFragment1 = (MapFragment) getFragmentManager().findFragmentById(R.id.mapPlaces);
            mapFragment1.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap gMap) {
                    googleMap = gMap;
                    if (googleMap == null) {
                        Snackbar.make(parent_view, R.string.unable_create_map, Snackbar.LENGTH_SHORT).show();
                    } else {
                        // config map
                        googleMap = Tools.configStaticMap(ActivityPlaceDetail.this, googleMap, place);
                    }
                }
            });
        }

        ((Button) findViewById(R.id.bt_navigate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(),"OPEN", Toast.LENGTH_LONG).show();
                Intent navigation = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + place.lat + "," + place.lng));
                startActivity(navigation);
            }
        });
        ((Button) findViewById(R.id.bt_view)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlaceInMap();
            }
        });
        ((LinearLayout) findViewById(R.id.map)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlaceInMap();
            }
        });
    }

    private void openPlaceInMap() {
        Intent intent = new Intent(ActivityPlaceDetail.this, ActivityMaps.class);
        intent.putExtra(ActivityMaps.EXTRA_OBJ, place);
        startActivity(intent);
    }

    private void prepareAds() {
        AdNetworkHelper adNetworkHelper = new AdNetworkHelper(this);
        adNetworkHelper.loadBannerAd(AppConfig.ads.ad_place_details_banner);
    }

    @Override
    protected void onDestroy() {
        if (callback != null && callback.isExecuted()) callback.cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backAction();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (description != null) description.onPause();
    }

    private void backAction() {
        if (isFromNotif) {
            Intent i = new Intent(this, ActivityMain.class);
            startActivity(i);
        }
        finish();
    }

    private void requestDetailsPlace(int place_id) {
        if (onProcess) {
            Snackbar.make(parent_view, R.string.task_running, Snackbar.LENGTH_SHORT).show();
            return;
        }
        onProcess = true;
        showProgressbar(true);
        callback = RestAdapter.createAPI().getPlaceDetails(place_id);
        callback.enqueue(new retrofit2.Callback<CallbackPlaceDetails>() {
            @Override
            public void onResponse(Call<CallbackPlaceDetails> call, Response<CallbackPlaceDetails> response) {
                CallbackPlaceDetails resp = response.body();
                if (resp != null) {
                    place = db.updatePlace(resp.place);
//                    displayDataWithDelay(place);
                } else {
                    onFailureRetry(getString(R.string.failed_load_details));
                }

            }

            @Override
            public void onFailure(Call<CallbackPlaceDetails> call, Throwable t) {
                if (call != null && !call.isCanceled()) {
                    boolean conn = Tools.cekConnection(ActivityPlaceDetail.this);
                    if (conn) {
                        onFailureRetry(getString(R.string.failed_load_details));
                    } else {
                        onFailureRetry(getString(R.string.no_internet));
                    }
                }
            }
        });
    }

    private void onFailureRetry(final String msg) {
        showProgressbar(false);
        onProcess = false;
        snackbar = Snackbar.make(parent_view, msg, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.RETRY, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                loadPlaceData();
            }
        });
        snackbar.show();
        retryDisplaySnackbar();
    }

    private void retryDisplaySnackbar() {
        if (snackbar != null && !snackbar.isShown()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    retryDisplaySnackbar();
                }
            }, 1000);
        }
    }

    private void showProgressbar(boolean show) {
        lyt_progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
