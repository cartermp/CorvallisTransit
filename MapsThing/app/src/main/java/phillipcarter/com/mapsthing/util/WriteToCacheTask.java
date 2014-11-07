package phillipcarter.com.mapsthing.util;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.util.List;

import phillipcarter.com.mapsthing.model.Route;

public class WriteToCacheTask extends AsyncTask<Void, Void, Void> {
    private SharedPreferences mSharedPreferences;
    private List<Route> mRoutes;
    private String mRouteKey;
    private SystemCallbacks mCallbacks;

    public WriteToCacheTask(SharedPreferences sp, SystemCallbacks callbacks,
                            List<Route> routes, String routeKey) {
        mSharedPreferences = sp;
        mRoutes = routes;
        mRouteKey = routeKey;
        mCallbacks = callbacks;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String json = new Gson().toJson(mRoutes, mRoutes.getClass());
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(mRouteKey, json);

        // Explicitly commit() rather than apply() since this is running
        // on a background thread.
        editor.commit();

        // Because of Java generics (or lack thereof), we have to return null
        // to signify doing nothing.
        return null;
    }

    @Override
    protected void onPostExecute(Void thing) {
        mCallbacks.onWriteToCacheSuccess();
    }
}
