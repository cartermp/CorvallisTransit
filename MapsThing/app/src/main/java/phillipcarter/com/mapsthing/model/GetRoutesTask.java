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

public class GetRoutesTask extends AsyncTask<Void, Void, Tuple<Boolean, List<Route>>> {
    TransitCallbacks mCallbacks;

    public GetRoutesTask(TransitCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    /**
     * Returns a Tuple containing whether or not there was a network error,
     * and a List of routes.
     */
    @Override
    protected Tuple<Boolean, List<Route>> doInBackground(Void... params) {
        Tuple<Boolean, String> result = getRouteData();
        if (result.item1) {
            return Tuple.create(true, null);
        }

        String json = result.item2;
        if (json == null || json.isEmpty()) {
            return null;
        }

        Gson gson = new Gson();
        List<Route> routes = new ArrayList<Route>();

        JsonParser parser = new JsonParser();
        JsonArray jArray = parser.parse(json)
                .getAsJsonObject()
                .get("routes")
                .getAsJsonArray();

        for (JsonElement j : jArray) {
            routes.add(gson.fromJson(j, Route.class));
        }

        return Tuple.create(false, routes);
    }

    @Override
    protected void onPostExecute(Tuple<Boolean, List<Route>> result) {
        if (result.item1) {
            mCallbacks.onNetworkError();
        } else if (result.item2 == null || result.item2.isEmpty()) {
            mCallbacks.onNoTransitInfo();
        } else {
            mCallbacks.onRoutesFetched(result.item2);
        }
    }

    private Tuple<Boolean, String> getRouteData() {
        String json = "";
        boolean networkError = false;

        try {
            json = WebUtil.downloadUrl("http://www.corvallis-bus.appspot.com/routes?stops=true");
        } catch (IOException e) {
           networkError = true;
        }

        return Tuple.create(networkError, json);
    }
}
