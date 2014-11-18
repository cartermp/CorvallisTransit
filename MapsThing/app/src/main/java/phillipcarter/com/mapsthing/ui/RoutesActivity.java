package phillipcarter.com.mapsthing.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import phillipcarter.com.mapsthing.R;
import phillipcarter.com.mapsthing.model.Route;
import phillipcarter.com.mapsthing.util.ReadFromCacheTask;

public class RoutesActivity extends ActionBarActivity {
    private List<Route> mRoutes;
    private RoutesAdapter mAdapter;

    static final String ROUTE_KEY = "route";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // THIS CODE DOES NOT EXIST
        SharedPreferences sp = getSharedPreferences(MapsActivity.PREF_NAME, 0);
        String json = sp.getString(MapsActivity.ROUTE_CACHE_KEY, null);
        Type routesType = new TypeToken<List<Route>>(){}.getType();
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
                Route route = mRoutes.get(position);
                String json = new Gson().toJson(route, Route.class);
                Intent intent = new Intent(RoutesActivity.this, RouteViewActivity.class);
                intent.putExtra(ROUTE_KEY, json);
                startActivity(intent);
            }
        });
    }
}
