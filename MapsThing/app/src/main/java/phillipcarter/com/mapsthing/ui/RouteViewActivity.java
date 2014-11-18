package phillipcarter.com.mapsthing.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.gson.Gson;

import phillipcarter.com.mapsthing.R;
import phillipcarter.com.mapsthing.model.Route;

public class RouteViewActivity extends ActionBarActivity {
    private Route mRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String json = extras.getString(RoutesActivity.ROUTE_KEY);
            mRoute = new Gson().fromJson(json, Route.class);
        }

        setUpUI();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.route_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpUI() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Route " + mRoute.Name);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#" + mRoute.Color)));
        ListView lv = (ListView) findViewById(R.id.stops_list);
        lv.setAdapter(new StopsAdapter(this, mRoute.Path));
    }
}
