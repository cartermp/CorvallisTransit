package phillipcarter.com.mapsthing.ui;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import phillipcarter.com.mapsthing.R;
import phillipcarter.com.mapsthing.model.Route;

public class RoutesActivity extends ActionBarActivity {
    private static final SparseIntArray routeNameToColorDict;

    static {
        routeNameToColorDict = new SparseIntArray();
        routeNameToColorDict.put(0, R.color.route_1_color);
        routeNameToColorDict.put(1, R.color.route_2_color);
        routeNameToColorDict.put(2, R.color.route_3_color);
        routeNameToColorDict.put(3, R.color.route_4_color);
        routeNameToColorDict.put(4, R.color.route_5_color);
        routeNameToColorDict.put(5, R.color.route_6_color);
        routeNameToColorDict.put(6, R.color.route_7_color);
        routeNameToColorDict.put(7, R.color.route_8_color);
        routeNameToColorDict.put(8, R.color.route_NON_color);
        routeNameToColorDict.put(9, R.color.route_NOSE_color);
        routeNameToColorDict.put(10, R.color.route_NOSW_color);
        routeNameToColorDict.put(11, R.color.route_C1_color);
        routeNameToColorDict.put(12, R.color.route_C1R_color);
        routeNameToColorDict.put(13, R.color.route_C2_color);
        routeNameToColorDict.put(14, R.color.route_C3_color);
        routeNameToColorDict.put(15, R.color.route_CVA_color);
    }

    private List<Route> mRoutes;
    private RoutesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // THIS CODE DOES NOT EXIST
        SharedPreferences sp = getSharedPreferences(MapsActivity.PREF_NAME, 0);
        String json = sp.getString(MapsActivity.ROUTE_CACHE_KEY, null);
        Type routesType = new TypeToken<List<Route>>() {
        }.getType();
        mRoutes = new Gson().fromJson(json, routesType);

        setUpUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.routes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpUI() {
        ListView lv = (ListView) findViewById(R.id.route_list);
        mAdapter = new RoutesAdapter(this, mRoutes);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActionBar actionBar = getSupportActionBar();
                actionBar.setBackgroundDrawable(
                        new ColorDrawable(
                                getResources().getColor(routeNameToColorDict.get(position))));
                // expand to show stops
            }
        });
    }
}
