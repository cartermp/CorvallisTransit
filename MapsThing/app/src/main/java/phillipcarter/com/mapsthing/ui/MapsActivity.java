package phillipcarter.com.mapsthing.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import phillipcarter.com.mapsthing.R;
import phillipcarter.com.mapsthing.model.GetRoutesTask;
import phillipcarter.com.mapsthing.model.Route;
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
    private boolean mCached;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mLocationClient = new LocationClient(this, this, this);

        SharedPreferences settings = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        mCached = settings.getBoolean(ROUTES_CACHED_PREF, false);

        getRoutes();
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

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(100);

        mMap.setMyLocationEnabled(true);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .build();

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        Circle circle = mMap.addCircle(circleOptions);

        circle.setFillColor(Color.rgb(255, 165, 0));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = mLocationClient.getLastLocation();

        // spin off task to get stops by location

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
     *
     * @param connectionResult
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONNECTION_FAILURE_RESOLUTION_REQUEST &&
                resultCode == Activity.RESULT_OK) {
            mLocationClient.connect();
        }
    }

    @Override
    public void onTaskFailed() {
        // create error dialog
    }

    /**
     * Callback for route retrieval.  Should only happen once,
     * since application cache should have the necessary info.
     */
    @Override
    public void onRouteParsed(List<Route> routes) {
        mRoutes = routes;

        new WriteToCacheTask(getSharedPreferences(PREF_NAME, MODE_PRIVATE),
                this, mRoutes, ROUTE_CACHE_KEY)
                .execute();

        mCached = true;

        SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(ROUTES_CACHED_PREF, mCached);
        editor.apply();
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
        Toast.makeText(this, "got routes from cache!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWriteToCacheSuccess() {
        Toast.makeText(this, "wrote to cache!", Toast.LENGTH_LONG).show();
    }

    /**
     * Checks if the device can connect to GPlay services.
     *
     * @return A tuple containing the result code of the service call,
     * and a boolean representing if the service GPlay services were available.
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
