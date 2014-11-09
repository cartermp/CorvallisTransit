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

public class GetRoutesTask extends AsyncTask<Void, Void, List<Route>> {
    TransitCallbacks mCallbacks;

    public GetRoutesTask(TransitCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    protected List<Route> doInBackground(Void... params) {
        String json = getRouteData();
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

        return routes;
    }

    @Override
    protected void onPostExecute(List<Route> routes) {
        if (routes == null || routes.isEmpty()) {
            mCallbacks.onTaskFailed();
        } else {
            mCallbacks.onRoutesFetched(routes);
        }
    }

    private String getRouteData() {
        String json = "";

        try {
            json = WebUtil.downloadUrl("http://www.corvallis-bus.appspot.com/routes?stops=true");
        } catch (IOException e) {
            // maybe do something
        }

        return json;
    }
}
