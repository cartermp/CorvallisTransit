package phillipcarter.com.mapsthing.util;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import phillipcarter.com.mapsthing.model.Route;
import phillipcarter.com.mapsthing.model.Tuple;

public class ReadFromCacheTask extends AsyncTask<Void, Void, Tuple<Boolean, List<Route>>> {
    private Context mContext;
    private String mFilename;
    private SystemCallbacks mCallbacks;

    public ReadFromCacheTask(Context context, SystemCallbacks callbacks, String filename) {
        mContext = context;
        mFilename = filename;
        mCallbacks = callbacks;
    }

    @Override
    protected Tuple<Boolean, List<Route>> doInBackground(Void... params) {
        return SystemUtil.getRoutes(mContext, mFilename);
    }

    @Override
    protected void onPostExecute(Tuple<Boolean, List<Route>> result) {
        if (result.item1) {
            mCallbacks.onCacheOpSuccess(result.item2);
        } else {
            mCallbacks.onCacheOpError();
        }
    }
}
