package phillipcarter.com.mapsthing.util;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import phillipcarter.com.mapsthing.model.Route;

public class WriteToCacheTask extends AsyncTask<Void, Void, Boolean> {
    private Context mContext;
    private List<Route> mRoutes;
    private String mFilename;
    private SystemCallbacks mCallbacks;

    public WriteToCacheTask(Context context, SystemCallbacks callbacks,
                            List<Route> routes, String filename) {
        mContext = context;
        mRoutes = routes;
        mFilename = filename;
        mCallbacks = callbacks;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return SystemUtil.writeRoutesToCache(mContext, mRoutes, mFilename);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            mCallbacks.onWriteToCacheSuccess();
        } else {
            mCallbacks.onCacheOpError();
        }
    }
}
