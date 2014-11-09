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

public class GetStopsTask extends AsyncTask<Void, Void, List<Stop>> {
    private double mLat;
    private double mLng;
    private TransitCallbacks mCallbacks;

    public GetStopsTask(TransitCallbacks callbacks, double lat, double lng) {
        mCallbacks = callbacks;
        mLat = lat;
        mLng = lng;
    }

    @Override
    protected List<Stop> doInBackground(Void... params) {
        String json = fetchStops();
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

        return stops;
    }

    @Override
    protected void onPostExecute(List<Stop> stops) {
        if (stops == null || stops.isEmpty()) {
            mCallbacks.onTaskFailed();
        } else {
            mCallbacks.onStopsFetched(stops);
        }
    }

    /**
     * Gets json-encoded stops within a (default) 500 meter radius
     * of the provided lat/lng, with a limit of 20 stops.
     */
    private String fetchStops() {
        String json = "";
        try {
            json = WebUtil.downloadUrl(
                    "http://www.corvallis-bus.appspot.com/stops" +
                            "?lat=" + mLat + "&lng=" + mLng +
                            "&limit=20"
            );
        } catch (IOException ioe) {
            // maybe do something
        }

        return json;
    }
}
