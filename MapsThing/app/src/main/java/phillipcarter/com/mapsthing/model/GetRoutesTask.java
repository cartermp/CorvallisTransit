package phillipcarter.com.mapsthing.model;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
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
        Type routeType = new TypeToken<List<Route>>() {}.getType();

        return gson.fromJson(json, routeType);
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
            json = WebUtil.downloadUrl("http://www.corvallis-bus.appspot.com/routes");
        } catch (IOException e) {
            // maybe do something
        }

        return json;
    }
}
