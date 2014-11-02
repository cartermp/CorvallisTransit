package phillipcarter.com.mapsthing;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
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

public class MapsActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap mMap;
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mLocationClient = new LocationClient(this, this, this);

        getMapIfNeeded();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Tuple<Integer, Boolean> result = servicesConnected();
        if (result.item2) {
            mLocationClient.connect();
        } else {
            launchServiceDialog(result.item1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMapIfNeeded();
    }

    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

    private void getMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
    }

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

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected from Google Play Services.", Toast.LENGTH_LONG).show();
    }

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
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mLocationClient.connect();
                        break;
                }

        }
    }

    /**
     * Checks if the device can connect to GPlay services.
     */
    private Tuple<Integer, Boolean> servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return new Tuple<Integer, Boolean>(resultCode, resultCode == ConnectionResult.SUCCESS);
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
            errorFragment.show(getSupportFragmentManager(),"Location Updates");
        }
    }

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
