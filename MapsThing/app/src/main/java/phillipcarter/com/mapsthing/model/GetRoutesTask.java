package phillipcarter.com.mapsthing.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import phillipcarter.com.mapsthing.R;
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
        Route rte = null;

        try {
            JsonParser parser = new JsonParser();
            JsonArray jArray = parser.parse(json)
                               .getAsJsonObject()
                               .get("routes")
                               .getAsJsonArray();

            for (JsonElement j : jArray) {
                rte = gson.fromJson(j, Route.class);
                routes.add(rte);
            }
        } catch (JsonSyntaxException jse) {
            Log.d("routes", jse.getMessage());
        } catch (JsonIOException jio) {
            Log.d("routes", jio.getMessage());
        } catch (IllegalStateException ise) {
            Log.d("routes", ise.getMessage());
        }

        return routes;
    }

    @Override
    protected void onPostExecute(List<Route> routes) {
        if (routes == null || routes.isEmpty()) {
            mCallbacks.onTaskFailed();
        } else {
            mCallbacks.onRouteParsed(routes);
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
