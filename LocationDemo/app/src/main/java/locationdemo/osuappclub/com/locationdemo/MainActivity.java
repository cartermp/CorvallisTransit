package locationdemo.osuappclub.com.locationdemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;


public class MainActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;

    /*
     * Define a request code to send to Google Play services.
     * This code is returned in Activity.onActivityResult.
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Values for location updates.  These are required for updating location.
     */
    private static final int MILLS_PER_SEC = 1000;
    private static final int UPDATE_INTERVAL_SECS = 5;
    private static final long UPDATE_INTERVAL = UPDATE_INTERVAL_SECS * MILLS_PER_SEC;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL = FASTEST_INTERVAL_IN_SECONDS * MILLS_PER_SEC;

    /*
      * Called when the application is first run
      * (and other times ... see Activity Lifecycle for more details)
      *
      * Instantiates the Location Client, which will then call our
      * callbacks asynchronously.
      */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (servicesConnected()) {
            mLocationClient = new LocationClient(this, this, this);

            if (!mLocationClient.isConnected() || !mLocationClient.isConnecting()) {
                mLocationClient.connect();
            }

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        }
    }

    /*
     * Called when this activity is visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    /*
     * Called when the activity is no longer visible.
     */
    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */

                        break;
                }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    /*
     * This method explicitly implements what the app should do
     * when it receives a location update from GPlay services.
     */
    @Override
    public void onLocationChanged(Location location) {
        String msg = "Updated location: "
                     + location.getLatitude() + ","
                     + location.getLongitude();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /*
     * This method explicitly provides the implementation for
     * when the phone successfully connects to Google Play Services.
     */
    @Override
    public void onConnected(Bundle bundle) {
        // At this point, you can request the current location,
        // or start periodic location updates.
        mCurrentLocation = mLocationClient.getLastLocation();

        if (mCurrentLocation != null) {
            Toast.makeText(this, "Current lat/long: "
                                 + mCurrentLocation.getLatitude()
                                 + ","
                                 + mCurrentLocation.getLongitude(),
                                Toast.LENGTH_LONG).show();
        } else {
            // Should rarely happen if Location is indeed turned on, according to Google
            Toast.makeText(this, "Location call failed!", Toast.LENGTH_SHORT).show();
        }

        // Here, we can request location updates.
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    /*
     * This method explicitly implements what should happen
     * when the phone disconnects from Google Play Services.
     */
    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected.  Reconnect.", Toast.LENGTH_SHORT).show();

        // Perhaps something else which lets the user know more
        // about the problem.
    }

    /*
     * Explicitly implements what should happen on the phone
     * if the attempt to access Google Play Services succeeds,
     * but the attempt to access Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * GPlay services can resolve some errors it detects.
         * If the error has resolution,
         * try sending an Intent to start a GPlay services
         * activity that can resolve the error.
         */
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST
                );
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Connection failed.  Error code: "
                                 + connectionResult.getErrorCode(),
                                Toast.LENGTH_SHORT).show();
        }
    }

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode == ConnectionResult.SUCCESS) {
            Log.d("Location Updates",
                    "Google Play services is available.");
            return true;

            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            if (errorDialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(),"Location Updates");
            }

            return false;
        }
    }

    /*
     * Nested static class for displaying location-related error dialogs.
     *
     * Nested static classes which do one, simple thing, are commonly
     * implemented in Java code.  Personally, I prefer not to do this,
     * but you will see this out in the world if you work with Java.
     */
    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        // Default constructor.  Required method in this case.
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
