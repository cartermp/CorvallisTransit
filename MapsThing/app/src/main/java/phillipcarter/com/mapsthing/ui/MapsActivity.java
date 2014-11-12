package phillipcarter.com.mapsthing.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.r0adkll.postoffice.PostOffice;
import com.r0adkll.postoffice.model.Design;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;

import phillipcarter.com.mapsthing.R;
import phillipcarter.com.mapsthing.model.Arrival;
import phillipcarter.com.mapsthing.model.GetArrivalsTask;
import phillipcarter.com.mapsthing.model.GetRoutesTask;
import phillipcarter.com.mapsthing.model.GetStopsTask;
import phillipcarter.com.mapsthing.model.Route;
import phillipcarter.com.mapsthing.model.Stop;
import phillipcarter.com.mapsthing.model.TransitCallbacks;
import phillipcarter.com.mapsthing.model.Tuple;
import phillipcarter.com.mapsthing.util.ReadFromCacheTask;
import phillipcarter.com.mapsthing.util.SystemCallbacks;
import phillipcarter.com.mapsthing.util.WriteToCacheTask;

public class MapsActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        TransitCallbacks,
        SystemCallbacks {
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final String ROUTE_CACHE_KEY = "route_cache";
    private static final String PREF_NAME = "cts_prefs";
    private static final String ROUTES_CACHED_PREF = "routes_cached";
    private GoogleMap mMap;
    private LocationClient mLocationClient;
    private List<Route> mRoutes;
    private List<Stop> mCurrentDisplayedStops;
    private boolean mCached;
    private SlidingUpPanelLayout mStopSlide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mLocationClient = new LocationClient(this, this, this);

        SharedPreferences settings = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        mCached = settings.getBoolean(ROUTES_CACHED_PREF, false);

        getRoutes();
        setUpStopSlider();
    }

    /**
     * Explicitly reconnects the Location Client when the activity is visible.
     * This is done because the Location Client is explicitly disconnected
     * when the activity is no longer visible.
     */
    @Override
    protected void onStart() {
        super.onStart();

        getMapIfNeeded();

        Tuple<Integer, Boolean> result = servicesConnected();
        if (result.item2) {
            mLocationClient.connect();
        } else {
            launchServiceDialog(result.item1);
        }
    }

    /**
     * Explicitly disconnects the Location Client to prevent a memory leak
     * before the map activity is no longer visible.
     */
    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

    /**
     * Handles toggling the view of the Stop Slider.
     */
    @Override
    public void onBackPressed() {
        if (mStopSlide != null &&
                (mStopSlide.isPanelExpanded() || mStopSlide.isPanelAnchored())) {
            mStopSlide.collapsePanel();
        } else if (mStopSlide != null && !mStopSlide.isPanelHidden()) {
            mStopSlide.hidePanel();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Callback for when the phone connects to Google Play Services.
     * Performs location-related setup when this happens.
     */
    @Override
    public void onConnected(Bundle bundle) {
        Location location = mLocationClient.getLastLocation();

        if (location != null) {
            new GetStopsTask(this, location.getLatitude(),
                    location.getLongitude())
                    .execute();
        }

        if (mMap != null) {
            setUpMap(location);
        }
    }

    /**
     * TODO: something meaningful
     */
    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected from Google Play Services.", Toast.LENGTH_LONG).show();
    }

    /**
     * TODO: something meaningful when connectionResult has no resolution
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.d("location", "failed to start connection resolution", e);
            }
        }
    }

    /**
     * Explicitly connects the Location Client before onResume() is called
     * when the Activity is restarting.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONNECTION_FAILURE_RESOLUTION_REQUEST &&
                resultCode == Activity.RESULT_OK) {
            mLocationClient.connect();
        }
    }

    /**
     * Callback for when some network error occurs when getting transit info.
     */
    @Override
    public void onNetworkError() {
        PostOffice.newMail(this)
                .setTitle("Network Error")
                .setMessage("Problem getting transit info from the network.")
                .setDesign(Design.MATERIAL_LIGHT)
                .setCanceledOnTouchOutside(true)
                .show(getSupportFragmentManager());
    }

    /**
     * Callback for when some error occurs that isn't related to the network.
     */
    @Override
    public void onNoTransitInfo() {
        PostOffice.newMail(this)
                .setTitle("No Scheduled Times")
                .setMessage("This stop has no bus running right now.")
                .setDesign(Design.MATERIAL_LIGHT)
                .setCanceledOnTouchOutside(true)
                .show(getSupportFragmentManager());
    }

    /**
     * Callback for route retrieval.  Should only happen once,
     * since application cache should have the necessary info.
     */
    @Override
    public void onRoutesFetched(List<Route> routes) {
        mRoutes = routes;

        new WriteToCacheTask(getSharedPreferences(PREF_NAME, MODE_PRIVATE),
                mRoutes, ROUTE_CACHE_KEY)
                .execute();

        mCached = true;

        SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(ROUTES_CACHED_PREF, mCached);
        editor.apply();
    }

    /**
     * Callback for stop retrieval.  The map is updated with new markers
     * once this is called.
     */
    @Override
    public void onStopsFetched(List<Stop> stops) {
        setMapMakers(stops);
        mCurrentDisplayedStops = stops;
    }

    /**
     * Callback for arrivals retrieval for a particular stop.  Opens Stop slideview.
     */
    @Override
    public void onArrivalsForStopFetched(List<Arrival> arrivals) {
        this.mStopSlide.showPanel();
    }

    /**
     * Callback for when routes cannot be read from the cache.
     * If this happens, uh-oh?
     */
    @Override
    public void onCacheOpError() {
        //new GetRoutesTask(this, this).execute();
    }

    /**
     * Callback for when routes are read from the cache.
     */
    @Override
    public void onReadFromCacheSuccess(List<Route> routes) {
        mRoutes = routes;
    }

    /**
     * Checks if the device can connect to GPlay services.
     *
     * @return A tuple containing the result code of the service call,
     * and a boolean representing if GPlay services were available.
     */
    private Tuple<Integer, Boolean> servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return Tuple.create(resultCode, resultCode == ConnectionResult.SUCCESS);
    }

    /**
     * Launches an ErrorDialog based on the result code from
     * a failed GPlay service connection attempt.
     */
    private void launchServiceDialog(int resultCode) {
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                resultCode,
                this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST);

        if (errorDialog != null) {
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(errorDialog);
            errorFragment.show(getSupportFragmentManager(), "Location Updates");
        }
    }

    /**
     * Method to get CTS route info either from the network or the local cache.
     */
    private void getRoutes() {
        if (mRoutes == null || mRoutes.isEmpty()) {
            if (mCached) {
                new ReadFromCacheTask(getSharedPreferences(PREF_NAME, MODE_PRIVATE),
                        this, ROUTE_CACHE_KEY)
                        .execute();
            } else {
                new GetRoutesTask(this).execute();
            }
        }
    }

    /**
     * Grabs a map UI element if no map exists.
     */
    private void getMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
    }

    /**
     * Performs setup operations on the map based on a given location.
     */
    private void setUpMap(Location location) {
        LatLng latLng = (location == null)
                ? new LatLng(0, 0)
                : new LatLng(location.getLatitude(),
                location.getLongitude());

        mMap.setMyLocationEnabled(true);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(16)
                .build();

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                new GetArrivalsTask(MapsActivity.this, marker.getPosition(),
                        MapsActivity.this.mCurrentDisplayedStops)
                        .execute();
                return false;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                SlidingUpPanelLayout slider = MapsActivity.this.mStopSlide;
                if (slider != null && !slider.isPanelHidden()) {
                    slider.hidePanel();
                }
            }
        });
    }

    /**
     * Sets markers corresponding to the list of stops on the map.
     */
    private void setMapMakers(List<Stop> stops) {
        for (Stop s : stops) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(s.Lat, s.Long)));
        }
    }

    private void setUpStopSlider() {
        mStopSlide = (SlidingUpPanelLayout) findViewById(R.id.route_slide);
        mStopSlide.hidePanel();
        mStopSlide.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {

            }

            @Override
            public void onPanelCollapsed(View view) {

            }

            @Override
            public void onPanelExpanded(View view) {

            }

            @Override
            public void onPanelAnchored(View view) {

            }

            @Override
            public void onPanelHidden(View view) {

            }
        });
    }

    /**
     * Nested class designed to easily facilitate an error dialog that
     * can manage its own lifecycle, rather than relying on the calling activity.
     */
    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
