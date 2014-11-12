package phillipcarter.com.mapsthing.model;

import java.util.List;

public interface TransitCallbacks {

    /**
     * Used to signify when a network error occurs.
     */
    public void onNetworkError();

    /**
     * Used to signify when there was no transit info from the server to handle.
     */
    public void onNoTransitInfo();

    /**
     * Used to signify when routes are fetched from the network successfully.
     */
    public void onRoutesFetched(List<Route> routes);

    /**
     * Used to signify when stops are fetched from the network successfully.
     */
    public void onStopsFetched(List<Stop> stops);

    /**
     * Used to signify when a list of arrivals for a stop are fetched from
     * the network successfully.
     */
    public void onArrivalsForStopFetched(List<Arrival> arrivals);
}
