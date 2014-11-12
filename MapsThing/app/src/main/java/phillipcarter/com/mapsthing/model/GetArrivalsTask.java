package phillipcarter.com.mapsthing.model;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import phillipcarter.com.mapsthing.util.WebUtil;

public class GetArrivalsTask extends AsyncTask<Void, Void, Tuple<Boolean, List<Arrival>>> {
    private TransitCallbacks mCallbacks;
    private LatLng mMarkerLatLng;
    private List<Stop> mStops;

    public GetArrivalsTask(TransitCallbacks callbacks, LatLng latng, List<Stop> stops) {
        mCallbacks = callbacks;
        mMarkerLatLng = latng;
        mStops = stops;
    }

    private static Tuple<Boolean, String> getArrivalString(Stop stop) {
        String json = null;
        boolean networkError = false;

        try {
            json = WebUtil.downloadUrl(
                    "http://www.corvallis-bus.appspot.com/arrivals" +
                            "?stops=" + stop.ID);
        } catch (IOException ioe) {
            networkError = true;
        }

        return Tuple.create(networkError, json);
    }

    /**
     * Finds the nearest matching Stop which corresponds to the given lat/long.
     */
    private static Stop findStopByLatLng(List<Stop> stops, LatLng latLng) {
        double lat = latLng.latitude;
        double lng = latLng.longitude;

        for (Stop s : stops) {
            if (arePointsEqual(s.Lat, s.Long, lat, lng)) {
                return s;
            }
        }

        return null;
    }

    /**
     * Determines if two lat/long points are equal.
     * <p/>
     * The precision is 8 decimal points for latitude,
     * and 7 decimal points for longitude.
     */
    private static boolean arePointsEqual(double lat1, double lng1, double lat2, double lng2) {
        double latitudeLeeway = 0.00000001;
        double longitudeLeeway = 0.0000001;

        return Math.abs(lat1 - lat2) < latitudeLeeway &&
                Math.abs(lng1 - lng2) < longitudeLeeway;
    }

    /**
     * Returns a Tuple containing whether or not a network error occured,
     * and a List of Arrivals.
     */
    @Override
    protected Tuple<Boolean, List<Arrival>> doInBackground(Void... params) {
        Stop stop = findStopByLatLng(mStops, mMarkerLatLng);
        if (stop == null) {
            return null;
        }

        Tuple<Boolean, String> result = getArrivalString(stop);
        if (result.item1) {
            return Tuple.create(true, null);
        }

        String json = result.item2;

        if (json == null || json.isEmpty()) {
            return null;
        }

        List<Arrival> arrivals = new ArrayList<Arrival>();
        Gson gson = new Gson();

        JsonParser parser = new JsonParser();
        JsonArray jsonElements = parser.parse(json)
                .getAsJsonObject()
                .get(String.valueOf(stop.ID))
                .getAsJsonArray();

        for (JsonElement je : jsonElements) {
            arrivals.add(gson.fromJson(je, Arrival.class));
        }

        return Tuple.create(false, arrivals);
    }

    @Override
    protected void onPostExecute(Tuple<Boolean, List<Arrival>> result) {
        if (result.item1) {
            mCallbacks.onNetworkError();
        } else if (result.item2 == null || result.item2.isEmpty()){
            mCallbacks.onNoTransitInfo();
        } else {
            mCallbacks.onArrivalsForStopFetched(result.item2);
        }
    }
}
