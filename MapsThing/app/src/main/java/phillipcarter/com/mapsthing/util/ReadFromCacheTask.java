package phillipcarter.com.mapsthing.util;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import phillipcarter.com.mapsthing.model.Route;
import phillipcarter.com.mapsthing.model.Tuple;

/**
 * Task for reading Route objects from shared preferences.
 */
public class ReadFromCacheTask extends AsyncTask<Void, Void, Tuple<Boolean, List<Route>>> {
    private SharedPreferences mSharedPreferences;
    private String mRouteKey;
    private SystemCallbacks mCallbacks;

    public ReadFromCacheTask(SharedPreferences sp, SystemCallbacks callbacks, String routeKey) {
        mSharedPreferences = sp;
        mRouteKey = routeKey;
        mCallbacks = callbacks;
    }

    @Override
    protected Tuple<Boolean, List<Route>> doInBackground(Void... params) {
        String json = mSharedPreferences.getString(mRouteKey, null);

        Type routeTye = new TypeToken<List<Route>>() {
        }.getType();
        
        List<Route> routes = new Gson().fromJson(json, routeTye);

        return Tuple.create(routes != null, routes);
    }

    @Override
    protected void onPostExecute(Tuple<Boolean, List<Route>> result) {
        if (result.item1) {
            mCallbacks.onReadFromCacheSuccess(result.item2);
        } else {
            mCallbacks.onCacheOpError();
        }
    }
}
