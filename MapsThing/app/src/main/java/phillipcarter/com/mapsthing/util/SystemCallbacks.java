package phillipcarter.com.mapsthing.util;

import java.util.List;

import phillipcarter.com.mapsthing.model.Route;

public interface SystemCallbacks {
    public void onCacheOpError();

    public void onReadFromCacheSuccess(List<Route> routes);
}