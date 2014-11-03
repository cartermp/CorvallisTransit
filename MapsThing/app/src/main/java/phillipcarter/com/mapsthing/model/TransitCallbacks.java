package phillipcarter.com.mapsthing.model;

import java.util.List;

public interface TransitCallbacks {
    public void onTaskFailed();

    public void onRouteParsed(List<Route> routes);
}
