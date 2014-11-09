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

public class GetArrivalTask extends AsyncTask<Void, Void, List<Arrival>> {
    private TransitCallbacks mCallbacks;
    private LatLng mMarkerLatLng;
    private List<Stop> mStops;

    public GetArrivalTask(TransitCallbacks callbacks, LatLng latng, List<Stop> stops) {
        mCallbacks = callbacks;
        mMarkerLatLng = latng;
        mStops = stops;
    }

    private static String getArrivalString(Stop stop) {
        String json = null;

        try {
            json = WebUtil.downloadUrl(
                    "http://www.corvallis-bus.appspot.com/arrivals" +
                            "?stops=" + stop.ID);
        } catch (IOException ioe) {
            // do something maybe
        }

        return json;
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

    @Override
    protected List<Arrival> doInBackground(Void... params) {
        Stop stop = findStopByLatLng(mStops, mMarkerLatLng);
        if (stop == null) {
            return null;
        }

        String json = getArrivalString(stop);
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

        return arrivals;
    }

    @Override
    protected void onPostExecute(List<Arrival> arrivals) {
        if (arrivals == null || arrivals.isEmpty()) {
            mCallbacks.onTaskFailed();
        } else {
            mCallbacks.onArrivalsForStopFetched(arrivals);
        }
    }
}
