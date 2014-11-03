package phillipcarter.com.mapsthing.util;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import phillipcarter.com.mapsthing.model.Route;

public class WriteToCacheTask extends AsyncTask<Void, Void, Void> {
    private Context mContext;
    private List<Route> mRoutes;
    private String mFilename;

    public WriteToCacheTask(Context context, List<Route> routes, String filename) {
        mContext = context;
        mRoutes = routes;
        mFilename = filename;
    }

    @Override
    protected Void doInBackground(Void... params) {
        SystemUtil.writeRoutesToFile(mContext, mRoutes, mFilename);

        // Need to return a reference type because Type Erasure in Java
        // doesn't allow for primitive types as the generic type arguments...
        // go Java, right?
        return null;
    }
}
