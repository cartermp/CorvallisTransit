package phillipcarter.com.mapsthing.model;

import java.util.List;

public interface TransitCallbacks {

    /**
     * Used to signify when a network task for getting transit info fails.
     */
    public void onTaskFailed();

    /**
     * Used to signify when routes are fetched from the network successfully.
     */
    public void onRoutesFetched(List<Route> routes);

    /**
     * Used to signify when stops are fetched from the network successfully.
     */
    public void onStopsFetched(List<Stop> stops);
}
