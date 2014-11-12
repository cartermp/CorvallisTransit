package phillipcarter.com.mapsthing.model;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import phillipcarter.com.mapsthing.util.WebUtil;

public class GetStopsTask extends AsyncTask<Void, Void, Tuple<Boolean, List<Stop>>> {
    private double mLat;
    private double mLng;
    private TransitCallbacks mCallbacks;

    public GetStopsTask(TransitCallbacks callbacks, double lat, double lng) {
        mCallbacks = callbacks;
        mLat = lat;
        mLng = lng;
    }

    /**
     * Returns a Tuple indicating whether or not there was a network error,
     * and the List of Stops.
     */
    @Override
    protected Tuple<Boolean, List<Stop>> doInBackground(Void... params) {
        Tuple<Boolean, String> result = fetchStops();
        if (result.item1) {
            return Tuple.create(true, null);
        }

        String json = result.item2;
        if (json == null || json.isEmpty()) {
            return null;
        }

        Gson gson = new Gson();
        List<Stop> stops = new ArrayList<Stop>();

        JsonParser parser = new JsonParser();
        JsonArray jArray = parser.parse(json)
                .getAsJsonObject()
                .get("stops")
                .getAsJsonArray();

        for (JsonElement j : jArray) {
            stops.add(gson.fromJson(j, Stop.class));
        }

        return Tuple.create(false, stops);
    }

    @Override
    protected void onPostExecute(Tuple<Boolean, List<Stop>> result) {
        if (result.item1) {
            mCallbacks.onNetworkError();
        } else if (result.item2 == null || result.item2.isEmpty()) {
            mCallbacks.onNoTransitInfo();
        } else {
            mCallbacks.onStopsFetched(result.item2);
        }
    }

    /**
     * Gets json-encoded stops within a (default) 500 meter radius
     * of the provided lat/lng, with a limit of 20 stops.
     */
    private Tuple<Boolean, String> fetchStops() {
        String json = "";
        boolean networkError = false;
        try {
            json = WebUtil.downloadUrl(
                    "http://www.corvallis-bus.appspot.com/stops" +
                            "?lat=" + mLat + "&lng=" + mLng +
                            "&limit=20"
            );
        } catch (IOException ioe) {
            networkError = true;
        }

        return Tuple.create(networkError, json);
    }
}
